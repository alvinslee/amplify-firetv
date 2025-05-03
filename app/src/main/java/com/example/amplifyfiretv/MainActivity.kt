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
import com.example.amplifyfiretv.data.CardData
import com.example.amplifyfiretv.data.CardDataProvider
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

class MainFragment : BrowseSupportFragment() {
    companion object {
        private const val TAG = "MainFragment"
        private const val SIGN_IN_REQUEST = 1
    }

    private var rowsAdapter: ArrayObjectAdapter? = null
    private var listRowAdapter: ArrayObjectAdapter? = null
    private var authRowAdapter: ArrayObjectAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainFragment onCreate")
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "MainFragment onViewCreated")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "MainFragment onActivityCreated")
        setupUIElements()
        setupContent()
        checkAuthSession()
        
        // Set up card loading listener
        CardDataProvider.setOnCardsLoadedListener {
            Log.d(TAG, "Cards loaded, updating UI")
            updateContent()
        }
    }

    private fun setupUIElements() {
        Log.d(TAG, "Setting up UI elements")
        title = "Amplify Fire TV"
        headersState = HEADERS_DISABLED
        isHeadersTransitionOnBackEnabled = true
        
        // Set the background color
        setBrandColor(requireContext().getColor(R.color.dark_orange))
        
        // Show the title
        setHeadersTransitionOnBackEnabled(true)
        
        // Set up the search icon
        setSearchAffordanceColor(requireContext().getColor(R.color.dark_orange_light))

        // Enable the title by setting the headers state
        headersState = HEADERS_ENABLED
        Log.d(TAG, "UI elements setup complete")
    }

    private fun setupContent() {
        Log.d(TAG, "Setting up content structure")
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        
        // Set up auth row
        val authPresenter = AuthPresenter()
        authRowAdapter = ArrayObjectAdapter(authPresenter)
        rowsAdapter?.add(ListRow(null, authRowAdapter))
        
        // Set up content row
        val cardPresenter = CardPresenter()
        listRowAdapter = ArrayObjectAdapter(cardPresenter)
        val contentHeader = HeaderItem(0, "Featured Content")
        rowsAdapter?.add(ListRow(contentHeader, listRowAdapter))

        // Set the adapter
        adapter = rowsAdapter

        // Set up click listener
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            when (item) {
                is CardData -> {
                    Log.d(TAG, "Card clicked: ${item.title}")
                    val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
                        putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, item.videoUrl)
                    }
                    startActivity(intent)
                }
                is AuthAction -> {
                    when (item) {
                        is AuthAction.SignIn -> {
                            val intent = Intent(requireContext(), SignInActivity::class.java)
                            startActivityForResult(intent, SIGN_IN_REQUEST)
                        }
                        is AuthAction.SignOut -> {
                            Log.d(TAG, "Sign out clicked")
                            Amplify.Auth.signOut { signOutResult ->
                                when(signOutResult) {
                                    is CompleteSignOut -> {
                                        Log.i(TAG, "Signed out successfully")
                                        Handler(Looper.getMainLooper()).post {
                                            updateAuthUI(false)
                                        }
                                    }
                                    is PartialSignOut -> {
                                        Log.e(TAG, "Partial sign out occurred")
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
                                            updateAuthUI(false)
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

    private fun checkAuthSession() {
        Amplify.Auth.fetchAuthSession(
            { session ->
                val cognitoSession = session as AWSCognitoAuthSession
                if (cognitoSession.isSignedIn) {
                    handleSignedInState()
                } else {
                    Log.d(TAG, "User is not signed in")
                    Handler(Looper.getMainLooper()).post {
                        updateAuthUI(false)
                    }
                }
            },
            { error ->
                Log.e(TAG, "Failed to fetch session", error)
                Handler(Looper.getMainLooper()).post {
                    updateAuthUI(false)
                }
            }
        )
    }

    private fun handleSignedInState() {
        Amplify.Auth.fetchUserAttributes(
            { attributes ->
                val email = attributes.find { it.key == AuthUserAttributeKey.email() }?.value
                Log.d(TAG, "User is signed in with email: $email")
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

    private fun updateAuthUI(isSignedIn: Boolean) {
        authRowAdapter?.clear()
        if (isSignedIn) {
            handleSignedInState()
        } else {
            authRowAdapter?.add(AuthAction.SignIn("Sign In"))
            authRowAdapter?.notifyArrayItemRangeChanged(0, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_REQUEST) {
            checkAuthSession()
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
        }

        val signOutText = TextView(parent.context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setTextColor(Color.WHITE)
            textSize = 18f
            isFocusable = true
            isFocusableInTouchMode = true
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
        setMainImageDimensions(313, 176)
        setInfoAreaBackgroundColor(context.getColor(R.color.dark_orange))
        setMainImageScaleType(ImageView.ScaleType.CENTER_CROP)
        setCardType(CARD_TYPE_INFO_UNDER)
    }
}

class CardPresenter : Presenter() {
    companion object {
        private const val TAG = "CardPresenter"
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = CustomImageCardView(parent.context)
        cardView.isFocusable = true
        cardView.isFocusableInTouchMode = true
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val cardData = item as CardData
        val cardView = viewHolder.view as CustomImageCardView
        
        cardView.titleText = cardData.title
        cardView.contentText = cardData.subtitle
        
        // Load image with Glide
        loadImage(cardView, cardData)
        
        cardView.mainImageView.visibility = View.VISIBLE
        cardView.setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as CustomImageCardView
        cardView.mainImageView.setImageDrawable(null)
        cardView.titleText = ""
        cardView.contentText = ""
    }

    private fun loadImage(cardView: ImageCardView, cardData: CardData) {
        // Clear any existing image
        cardView.mainImageView.setImageDrawable(null)
        
        // Load new image with Glide
        Glide.with(cardView.context)
            .load(cardData.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(cardView.mainImageView)
    }
}
