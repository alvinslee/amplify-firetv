package com.example.amplifyfiretv

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.amplifyfiretv.data.CardData
import com.example.amplifyfiretv.data.CardDataProvider
import android.view.View

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupUIElements()
        loadContent()
    }

    private fun setupUIElements() {
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
    }

    private fun loadContent() {
        val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        val cardPresenter = CardPresenter()
        val listRowAdapter = ArrayObjectAdapter(cardPresenter)

        // Add all cards to the adapter
        CardDataProvider.getCards().forEach { card ->
            listRowAdapter.add(card)
        }

        // Create a header for our row
        val header = HeaderItem(0, "Featured Content")
        rowsAdapter.add(ListRow(header, listRowAdapter))

        // Set the adapter
        adapter = rowsAdapter

        // Set up click listener
        onItemViewClickedListener = OnItemViewClickedListener { itemViewHolder, item, rowViewHolder, row ->
            if (item is CardData) {
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
                    putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, item.videoUrl)
                }
                startActivity(intent)
            }
        }
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
