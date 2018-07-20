package com.exozet.sequentialimage.player.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.exozet.sequentialimage.player.RemoveFishEye
import com.exozet.sequentialimage.player.SequentialImagePlayerActivity
import com.exozet.sequentialimage.player.parseAssetFile
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        default_video.setOnClickListener {
            startSequentialPlayer((1 until 317).map { parseAssetFile(String.format("default/out%d.png", it)) }.toTypedArray())
        }
        stabilized_video.setOnClickListener {
            startSequentialPlayer((1 until 192).map { parseAssetFile(String.format("stabilized/out%03d.png", it)) }.toTypedArray())
        }

        val bitmap = ((image.drawable) as BitmapDrawable).bitmap

        fish_eye.setOnClickListener {
            defished.setImageBitmap(RemoveFishEye(bitmap, 3.5))
            defished.visibility = View.VISIBLE
        }

        val vids = (1 until 92).map { parseAssetFile(String.format("fish_eye/out%03d.png", it)) }.toTypedArray()
        var index = 0

        glview.addBackgroundImages(listOf(loadBitmap(vids[0])!!))

        glview.setStrength(1.5f)
        fish_eye_gl.setOnClickListener {

            fixedRateTimer(
                    "bla",
                    false,
                    0.toLong(),
                    period = (1000.toFloat() / 30.toFloat()).toLong(),
                    action = {
                        glview.setBackground(loadBitmap(vids[(++index) % vids.size - 1])!!)
                    }
            )
            glview.visibility = View.VISIBLE
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val s = p1 / 10f
                Log.v(MainActivity::class.java.simpleName, "strength: $s")
                glview.setStrength(s)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })
    }

    private fun loadBitmap(uri: Uri?): Bitmap? {

        Log.v(this::class.java.simpleName, "loadingBitmap $uri")

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

    private fun startSequentialPlayer(list: Array<Uri>) {
        SequentialImagePlayerActivity.Builder
                .with(this)
                .uris(list)
                .fps(30) // default: 30
                .playBackwards(false) // default: false
                .autoPlay() // default: true
                .zoomable() // default: true
                .translatable() // default: true
                .showControls() // default: false
                .swipeSpeed(0.8f) // default: 1
                .blurLetterbox() // default: true
                .startActivity()
    }
}