package com.example.amplifyfiretv.presenter

import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.BaseCardView
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.amplifyfiretv.R
import com.example.amplifyfiretv.model.CardData
import com.example.amplifyfiretv.AuthStateManager
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Favorite

class VideoCardPresenter : Presenter() {
    companion object {
        private const val TAG = "VideoCardPresenter"
        private const val CARD_WIDTH = 313
        private const val CARD_HEIGHT = 176
    }

    private var authListener: ((Boolean) -> Unit)? = null
    private var selectedViewHolder: ViewHolder? = null
    private val favoriteStatusCache = mutableMapOf<String, Boolean>()
    private var isInitialLoad = true

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT)
            setMainImageScaleType(ImageView.ScaleType.CENTER_CROP)
            setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    selectedViewHolder = ViewHolder(this)
                    updateCardView(this, this.tag as? CardData)
                } else {
                    if (selectedViewHolder?.view == this) {
                        selectedViewHolder = null
                    }
                    updateCardView(this, this.tag as? CardData)
                }
            }
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val cardData = item as CardData
        val cardView = viewHolder.view as ImageCardView
        
        // Store the card data in the view's tag for later use
        cardView.tag = cardData
        
        // Set initial state
        updateCardView(cardView, cardData)
        
        // Load image with Glide
        loadImage(cardView, cardData)
        
        // Register auth state listener for this card
        authListener = { isSignedIn ->
            if (isSignedIn) {
                // Only check favorite status if we haven't already
                if (!favoriteStatusCache.containsKey(cardData.videoId)) {
                    checkFavoriteStatus(cardView, cardData)
                } else {
                    updateFavoriteIcon(cardView, favoriteStatusCache[cardData.videoId] ?: false)
                }
            } else {
                cardView.post {
                    cardView.setBadgeImage(null)
                }
                favoriteStatusCache.clear()
            }
        }
        AuthStateManager.getInstance().addListener(authListener!!)

        // Only check favorite status on initial load if not already cached
        if (isInitialLoad && !favoriteStatusCache.containsKey(cardData.videoId) && AuthStateManager.getInstance().isUserSignedIn()) {
            checkFavoriteStatus(cardView, cardData)
        } else if (favoriteStatusCache.containsKey(cardData.videoId)) {
            updateFavoriteIcon(cardView, favoriteStatusCache[cardData.videoId] ?: false)
        }
    }

    private fun checkFavoriteStatus(cardView: ImageCardView, cardData: CardData) {
        // Skip if already cached
        if (favoriteStatusCache.containsKey(cardData.videoId)) {
            updateFavoriteIcon(cardView, favoriteStatusCache[cardData.videoId] ?: false)
            return
        }

        val userId = AuthStateManager.getInstance().getCurrentUserId()
        if (userId == null) {
            Log.w(TAG, "No user ID available")
            return
        }

        val query = ModelQuery.list(
            Favorite::class.java,
            Favorite.VIDEO_ID.eq(cardData.videoId).and(Favorite.USER_ID.eq(userId))
        )

        Amplify.API.query(
            query,
            { response ->
                if (response.hasData()) {
                    val isFavorite = response.data.items.count() > 0
                    favoriteStatusCache[cardData.videoId] = isFavorite
                    cardView.post {
                        updateFavoriteIcon(cardView, isFavorite)
                    }
                }
            },
            { error -> 
                Log.e(TAG, "Error checking favorite status: ${error.message}", error)
            }
        )
    }

    private fun updateFavoriteIcon(cardView: ImageCardView, isFavorite: Boolean) {
        cardView.setBadgeImage(
            ContextCompat.getDrawable(
                cardView.context,
                if (isFavorite) R.drawable.ic_heart_filled
                else R.drawable.ic_heart_outline
            )
        )
    }

    private fun loadImage(cardView: ImageCardView, cardData: CardData) {
        cardView.mainImageView.setImageDrawable(null)
        Glide.with(cardView.context)
            .load(cardData.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(cardView.mainImageView)
    }

    private fun updateCardView(cardView: ImageCardView, cardData: CardData?) {
        if (cardData == null) {
            Log.w(TAG, "CardData is null, skipping update")
            return
        }
        
        cardView.titleText = cardData.title
        cardView.contentText = ""
        
        // Update favorite icon visibility based on auth state and cached status
        if (AuthStateManager.getInstance().isUserSignedIn()) {
            if (favoriteStatusCache.containsKey(cardData.videoId)) {
                updateFavoriteIcon(cardView, favoriteStatusCache[cardData.videoId] ?: false)
            }
        } else {
            cardView.setBadgeImage(null)
        }

        loadImage(cardView, cardData)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        cardView.mainImageView.setImageDrawable(null)
        cardView.titleText = ""
        cardView.contentText = ""
        cardView.setBadgeImage(null)
        cardView.tag = null
        
        authListener?.let { AuthStateManager.getInstance().removeListener(it) }
        authListener = null
    }
} 