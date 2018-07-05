package com.exozet.mobile.imageplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import kotlin.math.roundToLong

class MainActivity : AppCompatActivity() {

    private var swapImage: SwapImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        swapImage = SwapImage(this)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val range = loopRange(progress)
                viewHolder.setImageBitmap(getBitmapFromAsset(formatFileName(range)))
                swapImage?.index = range
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                stopAutoPlay()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startAutoPlay()
            }
        })

        with(fps) {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, (1 until 61).map { "$it" }.toList())
            setSelection(29)
        }

        with(videos) {
            adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listOf("Default", "Stablized"))
            setSelection(1)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    imageVideo = when (p2) {
                        1 -> ImageVideo.Stabilized
                        else -> ImageVideo.Default
                    }
                }
            }
        }

        viewHolder.setImageBitmap(getBitmapFromAsset(formatFileName(imageVideo.min)))
    }

    private fun getBitmapFromAsset(filePath: String): Bitmap? {
        val assetManager = assets

        val istr: InputStream
        var bitmap: Bitmap? = null
        try {
            istr = assetManager.open(filePath)
            bitmap = BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bitmap
    }

    override fun onResume() {
        super.onResume()
        startAutoPlay()
    }

    override fun onPause() {
        super.onPause()
        stopAutoPlay()
    }

    private fun startAutoPlay() {
        viewHolder.post(swapImage)
    }

    private fun stopAutoPlay() {
        viewHolder.removeCallbacks(swapImage)
    }

    internal class SwapImage(var activity: MainActivity) : Runnable {

        internal var index: Int

        init {
            index = imageVideo.min
            activity.seekBar.max = imageVideo.max
        }

        override fun run() {

            index = if (!activity.speed.isChecked) index + 1 else index - 1

            index = loopRange(index)

            Log("run with ${formatFileName(index % imageVideo.max)}.png")

            activity.viewHolder.setImageBitmap(activity.getBitmapFromAsset(formatFileName(index % imageVideo.max)))

            activity.viewHolder.postDelayed(this, (1000 / activity.fps.selectedItemPosition.toFloat()).roundToLong())

            activity.seekBar.progress = index
        }
    }

    companion object {

        private const val TAG: String = "MainActivity"

        private var imageVideo: ImageVideo = ImageVideo.Stabilized

        fun formatFileName(index: Int): String = String.format(imageVideo.fileFormat, index)

        fun Log(message: String) {
            Log.v(TAG, message)
        }

        fun loopRange(value: Int, min: Int = imageVideo.min, max: Int = imageVideo.max): Int = when {
            value > max -> min
            value < min -> max
            else -> value
        }
    }

    enum class ImageVideo(val fileFormat: String, val min: Int, val max: Int) {
        Stabilized("stabilized/out%03d.png", 1, 192),
        Default("default/out%d.png", 1, 317);
    }
}