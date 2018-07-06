package com.exozet.sequentialimage.player

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sequentialimage_player.*
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class SequentialImagePlayerActivity : AppCompatActivity() {

    private var swapImage: SwapImage? = null

    private var uris: Array<Uri> = arrayOf()

    private var swipeSpeed: Float = 1f

    var autoPlayEnabled: Boolean = false
        get() = autoplaySwitch.isChecked

    private var max: Int = 0
        get() = uris.size - 1

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequentialimage_player)

        busy()

        val files = intent?.extras?.getStringArray(Uri::class.java.canonicalName)
        if (files == null || files.isEmpty()) {
            finish()
            return
        }

        uris = files.map { Uri.parse(it) }.toTypedArray()

        swapImage = SwapImage(this)

        initSeekBar()

        initFpsSelector(fps = intent?.extras?.getInt(SequentialImagePlayer.FPS) ?: 30)

        playDirectionSwitch.isChecked = intent?.extras?.getBoolean(SequentialImagePlayer.PLAY_BACKWARDS) ?: false
        autoplaySwitch.isChecked = intent?.extras?.getBoolean(SequentialImagePlayer.AUTO_PLAY) ?: true
        viewHolder.isZoomable = intent?.extras?.getBoolean(SequentialImagePlayer.ZOOM) ?: true
        showControls(intent?.extras?.getBoolean(SequentialImagePlayer.SHOW_CONTROLS) ?: false)
        swipeSpeed = intent?.extras?.getFloat(SequentialImagePlayer.SWIPE_SPEED) ?: 1f

        loadImage(uris.first())

        autoplaySwitch.setOnCheckedChangeListener { _, isChecked -> if (isChecked) startAutoPlay() else stopAutoPlay() }

        cancelBusy()

        addSwipeGesture()
    }

    private fun addSwipeGesture() {

        val gestureDetector = GestureDetector(this@SequentialImagePlayerActivity, object : GestureDetector.SimpleOnGestureListener() {

            private val SWIPE_MAX_OF_PATH_X = 100
            private val SWIPE_MAX_OF_PATH_Y = 100

            /** Rotation threshold for scroll (X axis direction)  */
            val THRESHOLD_SCROLL_X = 0.02
            /** Rotation threshold for scroll (Y axis direction)  */
            val THRESHOLD_SCROLL_Y = 0.02

            /** Rotation amount derivative parameter for scroll (X axis direction)  */
            val ON_SCROLL_DIVIDER_X = 400.0f
            /** Rotation amount derivative parameter for scroll (Y axis direction)  */
            val ON_SCROLL_DIVIDER_Y = 400.0f

            override fun onDown(e: MotionEvent?): Boolean = true

            override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean =
                    if (Math.abs(distanceX) > SWIPE_MAX_OF_PATH_X || Math.abs(distanceY) > SWIPE_MAX_OF_PATH_Y) {
                        false
                    } else {
                        var diffX = distanceX / ON_SCROLL_DIVIDER_X
                        var diffY = distanceY / ON_SCROLL_DIVIDER_Y

                        if (Math.abs(diffX) < THRESHOLD_SCROLL_X) {
                            diffX = 0.0f
                        }
                        if (Math.abs(diffY) < THRESHOLD_SCROLL_Y) {
                            diffY = 0.0f
                        }

                        onScroll(distanceX, -distanceY)

                        true
                    }

            private fun onScroll(dX: Float, dY: Float) {
                onHorizontalScroll(dX)
                onVerticalScroll(dY)
            }

            private fun onVerticalScroll(deltaY: Float) {
                // Log("onVerticalScroll deltaY=$deltaY")
            }

            private fun onHorizontalScroll(deltaX: Float) {
                val delta = deltaX * swipeSpeed
                // Log("onHorizontalScroll deltaX=$deltaX / speed=$swipeSpeed -> delta=$delta")
                swapImage?.swapImage((swapImage?.index ?: 0) + delta.roundToInt())
            }
        })

        viewHolder.setOnTouchListener { _, motionEvent ->

            viewHolder.isZoomable = true
            viewHolder.isTranslatable = true

            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_UP -> {
                    super.onTouchEvent(motionEvent)
                }
                MotionEvent.ACTION_DOWN -> {
                    if (motionEvent.pointerCount <= 1) {
                        viewHolder.isZoomable = false
                        viewHolder.isTranslatable = false
                        gestureDetector.onTouchEvent(motionEvent)
                    } else
                        super.onTouchEvent(motionEvent)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (motionEvent.pointerCount <= 1) {
                        viewHolder.isZoomable = false
                        viewHolder.isTranslatable = false
                        gestureDetector.onTouchEvent(motionEvent)
                    } else
                        super.onTouchEvent(motionEvent)
                }
                else -> {
                    super.onTouchEvent(motionEvent)
                }
            }
        }
    }

    private fun showControls(isShown: Boolean) {
        seekBar.goneUnless(!isShown)
        playDirectionSwitch.goneUnless(!isShown)
        autoplaySwitch.goneUnless(!isShown)
        fpsSpinner.goneUnless(!isShown)
    }

    private fun initFpsSelector(@IntRange(from = 1, to = 60) fps: Int) {
        Log("FPS $fps")
        with(fpsSpinner) {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, (1 until 61).map { "$it" }.toList())
            setSelection(fps - 1)
        }
    }

    private fun initSeekBar() {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                swapImage?.swapImage(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (autoPlayEnabled) stopAutoPlay()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (autoPlayEnabled) startAutoPlay()
            }
        })
    }

    private fun cancelBusy() {
        progress.visibility = View.GONE
    }

    private fun busy() {
        progress.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (autoPlayEnabled) startAutoPlay()
    }


    override fun onPause() {
        super.onPause()
        if (autoPlayEnabled) stopAutoPlay()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        viewHolder.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    private fun startAutoPlay() {
        viewHolder.post(swapImage)
    }

    private fun stopAutoPlay() {
        viewHolder.removeCallbacks(swapImage)
    }

    internal class SwapImage(var activity: SequentialImagePlayerActivity) : Runnable {

        internal var index: Int

        init {
            index = 0
        }

        fun swapImage(index: Int) {
            this.index = loopRange(index, max = activity.max)
            activity.loadImage(activity.uris[this.index % activity.max])
            activity.seekBar.progress = this.index
            activity.seekBar.max = activity.max
        }

        override fun run() {

            index = if (!activity.playDirectionSwitch.isChecked) index + 1 else index - 1

            swapImage(index)

            val fps = (1000f / activity.fpsSpinner.selectedItem.toString().toInt()).roundToLong()
            // Log("playing ${activity.uris[index % activity.max]} aiming for fps=$fps")

            activity.viewHolder.postDelayed(this, fps)
        }
    }

    private fun loadImage(uri: Uri?) {

        // Log("Load $uri with GL_MAX_TEXTURE_SIZE size:")

        viewHolder.setImageBitmap(loadBitmap(uri))
    }

    private fun loadBitmap(uri: Uri?): Bitmap? {

        var bitmap: Bitmap? = null
        try {

            val istr = if (uri.toString().startsWith("file:///android_asset/"))
                assets.open(uri.toString().removePrefix("file:///android_asset/"))
            else
                contentResolver.openInputStream(uri)

            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
    }


    companion object {

        private val TAG = SequentialImagePlayerActivity::class.java.simpleName

        fun Log(message: String) {
            if (enableLogging) android.util.Log.v(TAG, message)
        }

        var enableLogging = true

        fun loopRange(value: Int, min: Int = 0, max: Int): Int = when {
            value > max -> min
            value < min -> max
            else -> value
        }
    }

    fun View.goneUnless(isGone: Boolean = true) {
        visibility = if (isGone) View.GONE else View.VISIBLE
    }
}