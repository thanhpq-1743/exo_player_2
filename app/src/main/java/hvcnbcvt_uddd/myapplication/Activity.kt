package hvcnbcvt_uddd.myapplication

import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.SimpleExoPlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.exo_playback_control_view.*

class Activity : AppCompatActivity() {
    private val STATE_RESUME_WINDOW = "resumeWindow"
    private val STATE_RESUME_POSITION = "resumePosition"
    private val STATE_PLAYER_FULLSCREEN = "playerFullscreen"
    private val STATE_CHECK_PLAY = "checkPlay"

    private var mExoPlayerView: SimpleExoPlayerView? = null
    private var mVideoSource: MediaSource? = null
    private var mExoPlayerFullscreen = false
    private var isPlay = false
    private lateinit var mFullScreenDialog: Dialog
    private var thumbnail = "https://i.vimeocdn.com/filter/overlay?src0=https%3A%2F%2Fi.vimeocdn.com%2Fvideo%2F7361" +
            "94690_640x360.jpg&src1=http%3A%2F%2Ff.vimeocdn.com%2Fp%2Fimages%2Fcrawler_play.png"
    private var video =
        "https://player.vimeo.com/external/297263152.m3u8?s=380434b08b1aefaa38ea686b2d498c8441c8c0b5&oauth2_token_id=1068158303"

    private var mResumeWindow: Int = 0
    private var mResumePosition: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState != null) {
            mResumeWindow = savedInstanceState.getInt(STATE_RESUME_WINDOW)
            mResumePosition = savedInstanceState.getLong(STATE_RESUME_POSITION)
            mExoPlayerFullscreen = savedInstanceState.getBoolean(STATE_PLAYER_FULLSCREEN)
            isPlay = savedInstanceState.getBoolean(STATE_CHECK_PLAY)
        }

        setVideoPreview()
    }


    public override fun onSaveInstanceState(outState: Bundle) {

        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow)
        outState.putLong(STATE_RESUME_POSITION, mResumePosition)
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen)
        outState.putBoolean(STATE_CHECK_PLAY, isPlay)

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        if (mExoPlayerView == null) {

            mExoPlayerView = findViewById(R.id.exo_player) as SimpleExoPlayerView

            initFullscreenDialog()
            initFullscreenButton()

            val userAgent = Util.getUserAgent(this@Activity, applicationContext.applicationInfo.packageName)
            val httpDataSourceFactory = DefaultHttpDataSourceFactory(
                userAgent,
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
            )
            val dataSourceFactory = DefaultDataSourceFactory(this@Activity, null, httpDataSourceFactory)

            mVideoSource = HlsMediaSource(Uri.parse(video), dataSourceFactory, 1, null, null)
        }

        initExoPlayer()

        if (mExoPlayerFullscreen) {
            (exo_player.parent as ViewGroup).removeView(exo_player)
            mFullScreenDialog.addContentView(
                exo_player,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            mFullScreenDialog.show()
        }
    }

    private fun initFullscreenDialog() {

        mFullScreenDialog = object : Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog()
                super.onBackPressed()
            }
        }
    }

    private fun initFullscreenButton() {
        exo_fullscreen_button.setOnClickListener {
            if (!mExoPlayerFullscreen)
                openFullscreenDialog()
            else
                closeFullscreenDialog()
        }
    }


    private fun openFullscreenDialog() {

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        (exo_player.parent as ViewGroup).removeView(exo_player)
        mFullScreenDialog.addContentView(
            exo_player,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )

        mExoPlayerFullscreen = true
        mFullScreenDialog.show()
    }


    private fun closeFullscreenDialog() {

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        (mExoPlayerView!!.parent as ViewGroup).removeView(mExoPlayerView)
        main_media_frame.addView(mExoPlayerView)

        mExoPlayerFullscreen = false
        mFullScreenDialog.dismiss()
    }


    private fun initExoPlayer() {
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val player =
            ExoPlayerFactory.newSimpleInstance(DefaultRenderersFactory(this), trackSelector, DefaultLoadControl())
        exo_player.player = player

        if (mResumeWindow != C.INDEX_UNSET) {
            exo_player.player.seekTo(mResumeWindow, mResumePosition)
        }

        exo_player.player.prepare(mVideoSource)
        exo_player.player.playWhenReady = false
    }


    private fun setVideoPreview() {
        img_preview.setOnClickListener {
            img_preview.visibility = View.GONE
            exo_player.visibility = View.VISIBLE
            exo_player.player.playWhenReady = true
            isPlay = true
        }


        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            exo_fullscreen_icon.setImageDrawable(resources.getDrawable(R.drawable.ic_fullscreen_skrink))
        }

        if (isPlay) {
            exo_player.visibility = View.VISIBLE
            img_preview.visibility = View.GONE
        } else {
            Glide.with(this).load(thumbnail).into(img_preview)
        }
    }


    override fun onPause() {

        super.onPause()

        if (exo_player != null && exo_player.player != null) {
            mResumeWindow = exo_player.player.currentWindowIndex
            mResumePosition = Math.max(0, exo_player.player.contentPosition)

            exo_player.player.release()
        }

        if (mFullScreenDialog != null)
            mFullScreenDialog.dismiss()
    }

}