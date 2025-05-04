package com.example.amplifyfiretv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.amplifyfiretv.model.CardData
import com.example.amplifyfiretv.model.CardDataProvider
import android.view.View
import android.util.Log
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.auth.cognito.AWSCognitoAuthSession
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult.CompleteSignOut
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult.PartialSignOut
import com.amplifyframework.auth.cognito.result.AWSCognitoAuthSignOutResult.FailedSignOut
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.amplifyframework.core.Consumer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.amplifyframework.auth.AuthUserAttributeKey
import androidx.leanback.widget.ImageCardView.CARD_TYPE_INFO_UNDER
import com.example.amplifyfiretv.presenter.VideoCardPresenter
import androidx.activity.OnBackPressedCallback

class MainActivity : FragmentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_browse_fragment, MainFragment())
                .commit()
        }
    }
}

class AuthStateManager private constructor() {
    private var isSignedIn = false
    private var currentUserId: String? = null
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
        // Immediately notify new listener of current state
        listener(isSignedIn && currentUserId != null)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        val isFullySignedIn = isSignedIn && currentUserId != null
        listeners.forEach { it(isFullySignedIn) }
    }

    fun checkAuthState() {
        Amplify.Auth.fetchAuthSession(
            { session ->
                val cognitoSession = session as AWSCognitoAuthSession
                isSignedIn = cognitoSession.isSignedIn
                if (isSignedIn) {
                    Log.d(TAG, "User is signed in, fetching user attributes")
                    Amplify.Auth.fetchUserAttributes(
                        { attributes ->
                            currentUserId = attributes.find { it.key == AuthUserAttributeKey.email() }?.value
                            Log.d(TAG, "User attributes fetched, userId: $currentUserId")
                            Handler(Looper.getMainLooper()).post {
                                notifyListeners()
                            }
                        },
                        { error -> 
                            Log.e(TAG, "Failed to fetch user attributes", error)
                            currentUserId = null
                            Handler(Looper.getMainLooper()).post {
                                notifyListeners()
                            }
                        }
                    )
                } else {
                    Log.d(TAG, "User is not signed in")
                    currentUserId = null
                    Handler(Looper.getMainLooper()).post {
                        notifyListeners()
                    }
                }
            },
            { error ->
                Log.e(TAG, "Failed to fetch session", error)
                isSignedIn = false
                currentUserId = null
                Handler(Looper.getMainLooper()).post {
                    notifyListeners()
                }
            }
        )
    }

    fun isUserSignedIn(): Boolean = isSignedIn && currentUserId != null

    fun getCurrentUserId(): String? = currentUserId

    companion object {
        private const val TAG = "AuthStateManager"
        private var instance: AuthStateManager? = null

        fun getInstance(): AuthStateManager {
            return instance ?: synchronized(this) {
                instance ?: AuthStateManager().also { instance = it }
            }
        }
    }
}

class MainFragment : BrowseSupportFragment() {
    companion object {
        private const val TAG = "MainFragment"
        private const val SIGN_IN_REQUEST = 1
    }

    private var rowsAdapter: ArrayObjectAdapter? = null
    private var listRowAdapter: ArrayObjectAdapter? = null
    private var authRowAdapter: ArrayObjectAdapter? = null
    private var isReturningFromDetails = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUIElements()
        setupContent()
        
        // Register auth state listener
        AuthStateManager.getInstance().addListener { isSignedIn ->
            updateAuthUI(isSignedIn)
        }
        
        // Initial auth check
        AuthStateManager.getInstance().checkAuthState()
        
        // Set up card loading listener
        CardDataProvider.setOnCardsLoadedListener {
            updateContent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove auth state listener
        AuthStateManager.getInstance().removeListener { isSignedIn ->
            updateAuthUI(isSignedIn)
        }
    }

    private fun setupUIElements() {
        title = "Amplify Fire TV"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = false  // Disable header transitions
        
        // Set the background color
        setBrandColor(requireContext().getColor(R.color.dark_orange))
        
        // Set up the search icon
        setSearchAffordanceColor(requireContext().getColor(R.color.dark_orange_light))

        // Enable the title by setting the headers state
        headersState = HEADERS_ENABLED
    }

    private fun setupContent() {
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        // Set up auth row with custom presenter
        val authPresenter = AuthPresenter()
        authRowAdapter = ArrayObjectAdapter(authPresenter)
        rowsAdapter?.add(ListRow(null, authRowAdapter))
        
        // Set up content row
        val cardPresenter = VideoCardPresenter()
        listRowAdapter = ArrayObjectAdapter(cardPresenter)
        val contentHeader = HeaderItem(0, "Featured Content")
        rowsAdapter?.add(ListRow(contentHeader, listRowAdapter))

        // Set the adapter
        adapter = rowsAdapter

        // Set up click listener
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            when (item) {
                is CardData -> {
                    if (!isReturningFromDetails) {
                        // Show details fragment
                        val fragment = VideoDetailsFragment().apply {
                            arguments = Bundle().apply {
                                putParcelable(VideoDetailsFragment.EXTRA_VIDEO_DATA, item)
                            }
                        }
                        parentFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.lb_details_enter,
                                R.anim.lb_details_exit,
                                R.anim.lb_details_enter,
                                R.anim.lb_details_exit
                            )
                            .replace(R.id.main_browse_fragment, fragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        isReturningFromDetails = false
                    }
                }
                is AuthAction -> {
                    when (item) {
                        is AuthAction.SignIn -> {
                            val intent = Intent(requireContext(), SignInActivity::class.java)
                            startActivityForResult(intent, SIGN_IN_REQUEST)
                        }
                        is AuthAction.SignOut -> {
                            Amplify.Auth.signOut { signOutResult ->
                                when(signOutResult) {
                                    is CompleteSignOut -> {
                                        Handler(Looper.getMainLooper()).post {
                                            AuthStateManager.getInstance().checkAuthState()
                                        }
                                    }
                                    is PartialSignOut -> {
                                        signOutResult.hostedUIError?.let {
                                            Log.e(TAG, "HostedUI Error", it.exception)
                                        }
                                        signOutResult.globalSignOutError?.let {
                                            Log.e(TAG, "GlobalSignOut Error", it.exception)
                                        }
                                        signOutResult.revokeTokenError?.let {
                                            Log.e(TAG, "RevokeToken Error", it.exception)
                                        }
                                        Handler(Looper.getMainLooper()).post {
                                            AuthStateManager.getInstance().checkAuthState()
                                        }
                                    }
                                    is FailedSignOut -> {
                                        Log.e(TAG, "Sign out Failed", signOutResult.exception)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Set up back stack listener
        parentFragmentManager.addOnBackStackChangedListener {
            if (parentFragmentManager.backStackEntryCount == 0) {
                isReturningFromDetails = true
            }
        }
    }

    private fun updateContent() {
        listRowAdapter?.clear()
        val cards = CardDataProvider.getCards()
        
        cards.forEach { card ->
            listRowAdapter?.add(card)
        }
        
        // Notify the adapter that the data has changed
        listRowAdapter?.notifyArrayItemRangeChanged(0, cards.size)
    }

    private fun updateAuthUI(isSignedIn: Boolean) {
        authRowAdapter?.clear()
        if (isSignedIn) {
            handleSignedInState()
        } else {
            authRowAdapter?.add(AuthAction.SignIn("Sign In"))
            authRowAdapter?.notifyArrayItemRangeChanged(0, 1)
        }
    }

    private fun handleSignedInState() {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                val email = attributes.find { it.key == AuthUserAttributeKey.email() }?.value
                Handler(Looper.getMainLooper()).post {
                    authRowAdapter?.clear()
                    authRowAdapter?.add(AuthAction.SignOut(email ?: "", "Sign out"))
                    authRowAdapter?.notifyArrayItemRangeChanged(0, 1)
                }
            },
            { error ->
                Log.e(TAG, "Failed to fetch user attributes", error)
                Handler(Looper.getMainLooper()).post {
                    authRowAdapter?.clear()
                    authRowAdapter?.add(AuthAction.SignOut("", "Sign out"))
                    authRowAdapter?.notifyArrayItemRangeChanged(0, 1)
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST) {
            AuthStateManager.getInstance().checkAuthState()
        }
    }
}

sealed class AuthAction {
    data class SignIn(val text: String) : AuthAction()
    data class SignOut(val email: String, val text: String) : AuthAction()
}

class AuthPresenter : Presenter() {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val layout = LinearLayout(parent.context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 16, 32, 16)
            // Disable focus highlighting
            isFocusable = false
            isFocusableInTouchMode = false
            background = null
        }

        val emailText = TextView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = 32
                weight = 1f
            }
            setTextColor(Color.WHITE)
            textSize = 18f
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            // Disable focus highlighting
            isFocusable = false
            isFocusableInTouchMode = false
        }

        val signOutText = TextView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.WHITE)
            textSize = 18f
            // Disable focus highlighting
            isFocusable = false
            isFocusableInTouchMode = false
        }

        layout.addView(emailText)
        layout.addView(signOutText)
        return ViewHolder(layout)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val layout = viewHolder.view as LinearLayout
        val emailText = layout.getChildAt(0) as TextView
        val signOutText = layout.getChildAt(1) as TextView

        when (val authAction = item as AuthAction) {
            is AuthAction.SignIn -> {
                emailText.visibility = View.GONE
                signOutText.text = authAction.text
            }
            is AuthAction.SignOut -> {
                emailText.visibility = View.VISIBLE
                emailText.text = authAction.email
                signOutText.text = authAction.text
            }
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val layout = viewHolder.view as LinearLayout
        val emailText = layout.getChildAt(0) as TextView
        val signOutText = layout.getChildAt(1) as TextView
        emailText.text = ""
        signOutText.text = ""
    }
}

class CustomImageCardView(context: Context) : ImageCardView(context) {
    init {
        isFocusable = true
        isFocusableInTouchMode = true
        setInfoAreaBackgroundColor(context.getColor(R.color.dark_orange))
        setCardType(CARD_TYPE_INFO_UNDER)
    }
}
