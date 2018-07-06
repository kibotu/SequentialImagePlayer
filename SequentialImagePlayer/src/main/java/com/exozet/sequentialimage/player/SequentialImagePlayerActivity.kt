package com.exozet.sequentialimage.player

import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.SeekBar
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sequentialimage_player.*
import java.io.IOException
import kotlin.math.roundToLong


class SequentialImagePlayerActivity : AppCompatActivity() {

    private var swapImage: SwapImage? = null

    private var uris: Array<Uri> = arrayOf()

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

        loadImage(uris.first())

        autoplaySwitch.setOnCheckedChangeListener { _, isChecked -> if (isChecked) startAutoPlay() else stopAutoPlay() }

        cancelBusy()
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
                val range = loopRange(progress, max = max)
                loadImage(uris[range])
                swapImage?.index = range
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopAutoPlay()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startAutoPlay()
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
        startAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        stopAutoPlay()
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

        override fun run() {

            index = if (!activity.playDirectionSwitch.isChecked) index + 1 else index - 1

            index = loopRange(index, max = activity.max)

            val fps = (1000f / activity.fpsSpinner.selectedItem.toString().toInt()).roundToLong()

            Log("playing ${activity.uris[index % activity.max]} aiming for fps=$fps")

            activity.loadImage(activity.uris[index % activity.max])

            activity.viewHolder.postDelayed(this, fps)

            activity.seekBar.max = activity.max
            activity.seekBar.progress = index
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

        var enableLogging = false

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