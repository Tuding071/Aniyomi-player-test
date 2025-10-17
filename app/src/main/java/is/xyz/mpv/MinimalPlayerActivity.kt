package `is`.xyz.mpv

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import `is`.xyz.mpv.MPVLib.MpvEvent
import `is`.xyz.mpv.MPVLib.EventObserver

class MinimalPlayerActivity : Activity(), EventObserver {

    private lateinit var mpvView: MPVView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen setup
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // Create the MPV view
        mpvView = MPVView(this)
        mpvView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // Container
        val container = FrameLayout(this)
        container.addView(mpvView)
        setContentView(container)

        // Initialize player
        MPVLib.create(this)
        MPVLib.setOptionString("vo", "gpu")
        MPVLib.setOptionString("hwdec", "auto")
        MPVLib.init()
        MPVLib.attachSurface(mpvView.holder.surface)
        MPVLib.observeProperty("pause", 1)
        MPVLib.addObserver(this)

        // Load file from intent
        intent?.data?.let { uri ->
            MPVLib.command(arrayOf("loadfile", uri.toString()))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MPVLib.removeObserver(this)
        MPVLib.destroy()
    }

    // Event callbacks
    override fun event(property: MpvEvent?) {
        // Handle MPV events here if needed
    }

    override fun eventProperty(property: String?, value: String?) {
        // Handle property changes here
    }
}
