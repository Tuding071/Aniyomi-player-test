package is.xyz.mpv

import android.app.Activity
import android.media.AudioManager
import android.net.Uri
import android.os.*
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlin.math.abs
import kotlin.math.roundToInt

class MinimalPlayerActivity : Activity(), MPVLib.EventObserver {

    private lateinit var surfaceView: SurfaceView
    private lateinit var gestureText: TextView
    private var audioManager: AudioManager? = null
    private val uiHandler = Handler(Looper.getMainLooper())

    private var initialSeek = 0f
    private var initialBright = 0f
    private var initialVolume = 0
    private var maxVolume = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        surfaceView = SurfaceView(this)
        gestureText = TextView(this).apply {
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            visibility = View.GONE
        }

        val layout = FrameLayout(this)
        layout.addView(surfaceView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        layout.addView(
            gestureText,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
        )
        setContentView(layout)

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        // Initialize mpv
        MPVLib.create(this, logLvl = 0)
        MPVLib.addObserver(this)

        // Attach surface
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                MPVLib.setSurface(holder.surface)
                intent?.data?.let { uri: Uri ->
                    MPVLib.command(arrayOf("loadfile", uri.toString()))
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                MPVLib.setSurface(null)
            }
        })

        surfaceView.setOnTouchListener(createGestureHandler())
    }

    private fun createGestureHandler(): View.OnTouchListener {
        var startX = 0f
        var startY = 0f
        val dm = resources.displayMetrics

        return View.OnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    initialSeek = MPVLib.getPropertyDouble("time-pos").toFloat()
                    initialBright = window.attributes.screenBrightness.takeIf { it >= 0 } ?: 0.5f
                    initialVolume = audioManager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
                    maxVolume = audioManager!!.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    gestureText.visibility = View.VISIBLE
                    gestureText.text = ""
                }

                MotionEvent.ACTION_MOVE -> {
                    val diffX = (event.x - startX) / dm.widthPixels
                    val diffY = (event.y - startY) / dm.heightPixels

                    if (abs(diffX) > abs(diffY)) {
                        val seekDiff = diffX * 100
                        MPVLib.command(arrayOf("seek", seekDiff.toString(), "relative"))
                        gestureText.text = "Seek: ${seekDiff.toInt()}s"
                    } else {
                        if (startX < dm.widthPixels / 2) {
                            val newBright = (initialBright - diffY).coerceIn(0f, 1f)
                            val lp = window.attributes
                            lp.screenBrightness = newBright
                            window.attributes = lp
                            gestureText.text = "Brightness: ${(newBright * 100).roundToInt()}%"
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
            uiHandler.post {
                gestureText.text = if (value == "yes") "Paused" else "Playing"
                gestureText.visibility = View.VISIBLE
                uiHandler.postDelayed({ gestureText.visibility = View.GONE }, 600)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MPVLib.removeObserver(this)
        MPVLib.destroy()
    }
}
