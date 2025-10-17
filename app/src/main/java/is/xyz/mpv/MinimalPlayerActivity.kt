package `is`.xyz.mpv

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MinimalPlayerActivity : AppCompatActivity() {

    private lateinit var mpvView: MPVView
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen container
        val container = FrameLayout(this)
        setContentView(container)

        // Initialize MPV view (pass both context and attrs)
        mpvView = MPVView(this, null)
        container.addView(
            mpvView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Initialize the MPV core
        mpvView.initialize()

        // Gesture detection
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            // Single tap = toggle play/pause
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                MPVLib.command(arrayOf("cycle", "pause"))
                return true
            }

            // Drag left/right = seek
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                if (e1 != null && e2 != null) {
                    val diff = e2.x - e1.x
                    val seekTime = diff / 10f // sensitivity
                    MPVLib.command(arrayOf("seek", seekTime.toString(), "relative"))
                }
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        mpvView.destroy()
    }
}
