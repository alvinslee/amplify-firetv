package com.example.amplifyfiretv

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.app.DetailsSupportFragment
import androidx.leanback.widget.*
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.generated.model.Favorite
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.amplifyfiretv.model.CardData
import com.amplifyframework.api.graphql.model.ModelQuery
import com.amplifyframework.api.graphql.model.ModelMutation

class VideoDetailsFragment : DetailsSupportFragment() {
    private lateinit var videoData: CardData
    private lateinit var detailsOverviewRow: DetailsOverviewRow
    private lateinit var detailsOverviewRowPresenter: FullWidthDetailsOverviewRowPresenter
    private var isFavorited = false

    companion object {
        private const val TAG = "VideoDetailsFragment"
        const val EXTRA_VIDEO_DATA = "video_data"
        private const val ACTION_PLAY = 0
        private const val ACTION_FAVORITE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        videoData = arguments?.getParcelable(EXTRA_VIDEO_DATA) ?: throw IllegalArgumentException("Video data is required")
        setupUI()
    }

    private fun setupUI() {
        detailsOverviewRow = DetailsOverviewRow(videoData)
        detailsOverviewRowPresenter = FullWidthDetailsOverviewRowPresenter(DetailsDescriptionPresenter())
            .apply {
                setInitialState(FullWidthDetailsOverviewRowPresenter.STATE_SMALL)
                setOnActionClickedListener { action ->
                    when (action.id.toInt()) {
                        ACTION_FAVORITE -> toggleFavorite()
                        ACTION_PLAY -> playVideo()
                    }
                }
            }

        val rowsAdapter = ArrayObjectAdapter(detailsOverviewRowPresenter)
        rowsAdapter.add(detailsOverviewRow)

        adapter = rowsAdapter

        loadVideoDetails()
        checkFavoriteStatus()
    }

    private fun loadVideoDetails() {
        Glide.with(requireContext())
            .load(videoData.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade())
            .centerCrop()
            .into(object : com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> {
                override fun onLoadStarted(placeholder: android.graphics.drawable.Drawable?) {
                    detailsOverviewRow.imageDrawable = placeholder
                }

                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    detailsOverviewRow.imageDrawable = errorDrawable
                }

                override fun onResourceReady(resource: android.graphics.drawable.Drawable, transition: com.bumptech.glide.request.transition.Transition<in android.graphics.drawable.Drawable>?) {
                    detailsOverviewRow.imageDrawable = resource
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    detailsOverviewRow.imageDrawable = placeholder
                }

                override fun getSize(cb: com.bumptech.glide.request.target.SizeReadyCallback) {
                    cb.onSizeReady(185, 185)
                }

                override fun removeCallback(cb: com.bumptech.glide.request.target.SizeReadyCallback) {}
                override fun setRequest(request: com.bumptech.glide.request.Request?) {}
                override fun getRequest(): com.bumptech.glide.request.Request? = null
                override fun onStart() {}
                override fun onStop() {}
                override fun onDestroy() {}
            })

        val actionsAdapter = SparseArrayObjectAdapter()
        
        val playIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_play_triangle)
        val playAction = Action(ACTION_PLAY.toLong(), "")
        playAction.icon = playIcon
        actionsAdapter.set(ACTION_PLAY, playAction)
        
        if (AuthStateManager.getInstance().isUserSignedIn()) {
            val favoriteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_heart_outline)
            val favoriteAction = Action(ACTION_FAVORITE.toLong(), "")
            favoriteAction.icon = favoriteIcon
            actionsAdapter.set(ACTION_FAVORITE, favoriteAction)
        }
        
        detailsOverviewRow.actionsAdapter = actionsAdapter
    }

    private fun checkFavoriteStatus() {
        if (!AuthStateManager.getInstance().isUserSignedIn()) {
            return
        }

        val userId = AuthStateManager.getInstance().getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "No user ID available")
            return
        }

        val query = ModelQuery.list(
            Favorite::class.java,
            Favorite.VIDEO_ID.eq(videoData.videoId).and(Favorite.USER_ID.eq(userId))
        )
        
        Amplify.API.query(
            query,
            { response ->
                if (response.hasData()) {
                    isFavorited = response.data.items.count() > 0
                    updateFavoriteIcon()
                } else {
                    isFavorited = false
                    updateFavoriteIcon()
                }
            },
            { error -> 
                Log.e(TAG, "Error checking favorite status: ${error.message}", error)
                isFavorited = false
                updateFavoriteIcon()
            }
        )
    }

    private fun updateFavoriteIcon() {
        if (!AuthStateManager.getInstance().isUserSignedIn()) return

        val actionsAdapter = detailsOverviewRow.actionsAdapter as? SparseArrayObjectAdapter ?: return
        val favoriteAction = actionsAdapter.get(ACTION_FAVORITE) as? Action ?: return

        val iconRes = if (isFavorited) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        favoriteAction.icon = ContextCompat.getDrawable(requireContext(), iconRes)
        actionsAdapter.notifyItemRangeChanged(ACTION_FAVORITE, 1)
    }

    private fun toggleFavorite() {
        if (!AuthStateManager.getInstance().isUserSignedIn()) {
            return
        }

        val userId = AuthStateManager.getInstance().getCurrentUserId()
        if (userId == null) {
            Log.e(TAG, "No user ID available")
            return
        }

        if (isFavorited) {
            val query = ModelQuery.list(
                Favorite::class.java,
                Favorite.VIDEO_ID.eq(videoData.videoId).and(Favorite.USER_ID.eq(userId))
            )
            
            Amplify.API.query(
                query,
                { response ->
                    if (response.hasData() && response.data.items.any()) {
                        val favorite = response.data.items.first()
                        val deleteMutation = ModelMutation.delete(favorite)
                        
                        Amplify.API.mutate(
                            deleteMutation,
                            { 
                                isFavorited = false
                                updateFavoriteIcon()
                            },
                            { error -> 
                                Log.e(TAG, "Error deleting favorite: ${error.message}", error)
                            }
                        )
                    }
                },
                { error -> 
                    Log.e(TAG, "Error querying favorite for deletion: ${error.message}", error)
                }
            )
        } else {
            val favorite = Favorite.builder()
                .userId(userId)
                .videoId(videoData.videoId)
                .build()

            val createFavorite = ModelMutation.create(favorite)

            Amplify.API.mutate(createFavorite,
              { response ->
                  checkFavoriteStatus()
              },
              { error -> 
                  Log.e(TAG, "Error creating favorite: ${error.message}", error)
              }
            )
        }
    }

    private fun playVideo() {
        val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
            putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, videoData.videoUrl)
        }
        startActivity(intent)
    }
}

class DetailsDescriptionPresenter : AbstractDetailsDescriptionPresenter() {
    override fun onBindDescription(vh: ViewHolder, item: Any) {
        val videoData = item as CardData
        vh.title.text = videoData.title
        vh.subtitle.text = videoData.subtitle
        vh.body.text = null
    }
}
