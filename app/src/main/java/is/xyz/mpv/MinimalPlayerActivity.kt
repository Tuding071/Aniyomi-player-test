package `is`.xyz.mpv

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout

class MinimalPlayerActivity : AppCompatActivity() {

    private lateinit var mpvView: MPVView
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fullscreen container
        val container = FrameLayout(this)
        setContentView(container)

        // MPVView: no AttributeSet or logLvl needed
        mpvView = MPVView(this)
        container.addView(
            mpvView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Initialize gesture detector
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                // Toggle play/pause on tap
                MPVLib.command(arrayOf("cycle", "pause"))
                return true
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                // Horizontal drag seeking
                if (e1 != null && e2 != null) {
                    val diff = e2.x - e1.x
                    val seekTime = diff / 10f // Adjust this sensitivity
                    MPVLib.command(arrayOf("seek", seekTime.toString(), "relative"))
                }
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        mpvView.destroy()
    }
}
