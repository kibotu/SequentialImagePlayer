package com.exozet.sequentialimage.app

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.math.MathUtils.clamp
import com.exozet.sequentialimageplayer.RemoveFishEye
import com.exozet.sequentialimageplayer.SequentialImagePlayerActivity
import com.exozet.sequentialimageplayer.parseAssetFile
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.activity_main.*
import net.kibotu.logger.LogcatLogger
import net.kibotu.logger.Logger
import net.kibotu.logger.Logger.logw
import java.io.File
import java.io.IOException
import kotlin.concurrent.fixedRateTimer

class MainActivity : AppCompatActivity() {

    var subscription: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.addLogger(LogcatLogger())

        checkWriteExternalStoragePermission()
    }

    protected fun checkWriteExternalStoragePermission() {
        RxPermissions(this)
            .requestEachCombined(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe({
                if (it.granted)
                    onWritePermissionGranted()
            }, {
                logw { "permission $it" }
            })
            .addTo(subscription)
    }

    private fun onWritePermissionGranted() {

        val localTest = (1 until 360).map {

            val uri =  Uri.fromFile(File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/postProcess/11113/1565785803706/image_%03d.jpg".format(it)))

            Log.v(MainActivity::class.java.simpleName, "uri.path=${uri.path} exists=${File(uri.path).exists()}")

            uri
        }.toTypedArray()

        default_video.setOnClickListener {

            val list = (1 until 317).map {
                parseAssetFile(
                    String.format(
                        "default/out%d.png",
                        it
                    )
                )
            }.toTypedArray()

            startSequentialPlayer(list)
        }
        stabilized_video.setOnClickListener {
            startSequentialPlayer((1 until 192).map {
                parseAssetFile(
                    String.format(
                        "stabilized/out%03d.png",
                        it
                    )
                )
            }.toTypedArray())
        }

        val bitmap = ((image.drawable) as BitmapDrawable).bitmap

        fish_eye.setOnClickListener {
            defished.setImageBitmap(RemoveFishEye(bitmap, 3.5))
            defished.visibility = View.VISIBLE
        }

        val vids = (1 until 92).map {
            parseAssetFile(
                String.format(
                    "fish_eye/out%03d.png",
                    it
                )
            )
        }.toTypedArray()
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
                    glview.setBackground(loadBitmap(vids[clamp((++index) % vids.size - 1, 0, vids.size - 1)])!!)
                }
            )
            glview.visibility = View.VISIBLE
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val s = p1 / 10f
                Log.v(MainActivity::class.java.simpleName, "strength: $s")
                glview.setStrength(s - 5f)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }
        })

        seekBar2.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val s = p1 / 10f
                Log.v(MainActivity::class.java.simpleName, "zoom: $s")
                glview.setZoom(s - 5f)
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
                contentResolver.openInputStream(uri!!)

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
//            .duration(10000) // default: 30000
            .playBackwards(false) // default: false
            .autoPlay(true) // default: true
            .zoomable(true) // default: true
            .translatable(true) // default: true
            .showControls() // default: false
            .swipeSpeed(0.75f) // default: 1
            .blurLetterbox() // default: true
            .startActivity()
    }

    override fun onDestroy() {
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
        super.onDestroy()
    }
}