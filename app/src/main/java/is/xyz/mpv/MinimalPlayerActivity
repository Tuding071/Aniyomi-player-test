package is.xyz.mpv

import android.app.Activity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout

class MinimalPlayerActivity : Activity() {

    private lateinit var mpvView: MPVView
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create MPVView (inherits BaseMPVView)
        mpvView = MPVView(this, null)
        val layout = FrameLayout(this)
        layout.addView(mpvView, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
        setContentView(layout)

        // Initialize MPV
        MPVLib.create(this)

        // Gesture detection
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                togglePlayPause()
                return true
            }

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                handleSeek(-distanceX)
                return true
            }
        })

        // Attach touch listener
        mpvView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        // Load video from Intent
        val uri = intent?.data
        val path = uri?.path
        path?.let {
            MPVLib.command(arrayOf("loadfile", it))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MPVLib.destroy()
    }

    // Pause/Play toggle
    private fun togglePlayPause() {
        MPVLib.command(arrayOf("cycle", "pause"))
    }

    // Horizontal seek
    private fun handleSeek(distanceX: Float) {
        // Adjust sensitivity (10 pixels = 1 second)
        val seconds = (distanceX / 10).toInt()
        if (seconds != 0) {
            MPVLib.command(arrayOf("seek", seconds.toString(), "relative"))
        }
    }
}
