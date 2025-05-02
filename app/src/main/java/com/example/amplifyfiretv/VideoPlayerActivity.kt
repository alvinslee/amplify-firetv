package com.example.amplifyfiretv

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView

class VideoPlayerActivity : FragmentActivity() {
    companion object {
        private const val TAG = "VideoPlayerActivity"
        const val EXTRA_VIDEO_URL = "video_url"
    }

    private var player: ExoPlayer? = null
    private var videoUrl: String? = null
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL)
        if (videoUrl == null) {
            Log.e(TAG, "No video URL provided")
            finish()
            return
        }

        Log.d(TAG, "Loading video from URL: $videoUrl")

        // Set up the player view
        playerView = findViewById(R.id.player_view)
        playerView.useController = true

        // Create the player with software decoding support
        player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(this))
            .build()
            .also { exoPlayer ->
                // Create a media item from the video URL
                val mediaItem = MediaItem.fromUri(videoUrl!!)
                exoPlayer.setMediaItem(mediaItem)

                // Set the player to the view
                playerView.player = exoPlayer
                
                // Prepare and start playback
                exoPlayer.prepare()
                exoPlayer.play()
            }
    }

    override fun onStart() {
        super.onStart()
        player?.play()
    }

    override fun onResume() {
        super.onResume()
        playerView.onResume()
    }

    override fun onPause() {
        super.onPause()
        playerView.onPause()
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }
} 