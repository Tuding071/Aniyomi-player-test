package `is`.xyz.mpv

import android.app.Activity
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import is.xyz.mpv.MPVLib
import is.xyz.mpv.MPVLib.EventObserver
import kotlin.math.roundToInt

class MinimalPlayerActivity : Activity(), EventObserver {

    private lateinit var surfaceView: SurfaceView
    private lateinit var gestureText: TextView
    private var audioManager: AudioManager? = null

    private val eventHandler = Handler(Looper.getMainLooper())
    private var initialSeek = 0f
    private var initialBright = 0f
    private var initialVolume = 0
    private var maxVolume = 0
    private var pausedForSeek = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).hide(WindowInsetsCompat.Type.systemBars())

        // --- Layout ---
        surfaceView = SurfaceView(this)
        gestureText = TextView(this).apply {
            textSize = 18f
            visibility = View.GONE
        }

        val layout = FrameLayout(this)
        layout.addView(surfaceView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout.addView(gestureText, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        ))

        setContentView(layout)

        // Init audio + player
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        MPVLib.create(surfaceView.holder.surface)
        MPVLib.addObserver(this)

        // --- Load video ---
        intent?.data?.let { uri ->
            val path = uri.toString()
            MPVLib.command(arrayOf("loadfile", path))
        }

        // --- Gesture handler ---
        surfaceView.setOnTouchListener(createGestureHandler())
    }

    private fun createGestureHandler(): View.OnTouchListener {
        var startX = 0f
        var startY = 0f
        var diffX = 0f
        var diffY = 0f
        val displayMetrics = resources.displayMetrics

        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    initialSeek = MPVLib.getPropertyDouble("time-pos").toFloat()
                    initialBright = window.attributes.screenBrightness.takeIf { it >= 0 } ?: 0.5f
                    initialVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    pausedForSeek = 0
                    gestureText.visibility = View.VISIBLE
                    gestureText.text = ""
                }

                MotionEvent.ACTION_MOVE -> {
                    diffX = (event.x - startX) / displayMetrics.widthPixels
                    diffY = (event.y - startY) / displayMetrics.heightPixels

                    if (kotlin.math.abs(diffX) > kotlin.math.abs(diffY)) {
                        // Seek gesture
                        val seekDiff = diffX * 100 // seconds scale
                        MPVLib.command(arrayOf("seek", seekDiff.toString(), "relative"))
                        gestureText.text = "Seek: ${seekDiff.toInt()}s"
                    } else {
                        // Brightness or volume
                        if (startX < displayMetrics.widthPixels / 2) {
                            val newBright = (initialBright - diffY).coerceIn(0f, 1f)
                            val lp = window.attributes
                            lp.screenBrightness = newBright
                            window.attributes = lp
                            gestureText.text = "Brightness: ${(newBright * 100).toInt()}%"
                        } else {
                            val newVol = (initialVolume - diffY * maxVolume).toInt().coerceIn(0, maxVolume)
                            audioManager!!.setStreamVolume(AudioManager.STREAM_MUSIC, newVol, 0)
                            val percent = 100 * newVol / maxVolume
                            gestureText.text = "Volume: ${percent}%"
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    gestureText.visibility = View.GONE
                }
            }
            true
        }
    }

    override fun event(id: Int) {
        if (id == MPVLib.MpvEvent.MPV_EVENT_SHUTDOWN)
            finish()
    }

    override fun eventProperty(property: String, value: String) {
        if (property == "pause") {
            eventHandler.post {
                gestureText.text = if (value == "yes") "Paused" else "Playing"
                gestureText.visibility = View.VISIBLE
                eventHandler.postDelayed({ gestureText.visibility = View.GONE }, 600)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MPVLib.removeObserver(this)
        MPVLib.destroy()
    }
}
