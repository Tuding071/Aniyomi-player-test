package `is`.xyz.mpv

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout

class MinimalPlayerActivity : AppCompatActivity(),
    GestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener {

    private lateinit var mpvView: MPVView
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create container for MPVView
        val container = FrameLayout(this)
        setContentView(container)

        // Initialize MPVView programmatically
        mpvView = MPVView(this, null, logLvl = 2)
        container.addView(mpvView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        // Gesture detector for tap/scroll
        gestureDetector = GestureDetector(this, this)
        gestureDetector.setOnDoubleTapListener(this)

        // Example: Load a video from Intent extra
        intent.getStringExtra("videoPath")?.let { path ->
            MPVLib.command(arrayOf("loadfile", path))
        }
    }

    // Pass touch events to gesture detector
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { gestureDetector.onTouchEvent(it) }
        return super.onTouchEvent(event)
    }

    /** Gesture overrides **/

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        // Toggle play/pause
        MPVLib.command(arrayOf("cycle", "pause"))
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        // Example: horizontal drag for seeking
        if (e1 != null && e2 != null) {
            val diff = e2.x - e1.x
            val seekTime = diff / 10f  // adjust sensitivity
            MPVLib.command(arrayOf("seek", seekTime.toString(), "relative"))
        }
        return true
    }

    // Unused gesture methods (required by interface)
    override fun onDown(e: MotionEvent?) = true
    override fun onShowPress(e: MotionEvent?) {}
    override fun onLongPress(e: MotionEvent?) {}
    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float) = false
    override fun onDoubleTap(e: MotionEvent?) = false
    override fun onDoubleTapEvent(e: MotionEvent?) = false
    override fun onSingleTapUp(e: MotionEvent?) = false
}
