package com.exozet.sequentialimage.player

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import java.lang.ref.WeakReference
import kotlin.math.roundToInt
import kotlin.math.roundToLong


class SequentialImagePlayerActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequentialimage_player)

        busy()

        val files = intent?.extras?.getStringArray(Uri::class.java.canonicalName)
        if (files == null || files.isEmpty()) {
            finish()
            return
        }

        // sequentialImagePlayer
    }


    class Builder {

        private lateinit var context: WeakReference<Context>

        private var uris: Array<Uri>? = null

        private var autoPlay: Boolean = true

        private var playBackwards: Boolean = false

        private var zoom: Boolean = false

        private var controls: Boolean = false

        private var swipeSpeed: Float = 1f

        private var fps: Int = 30

        fun swipeSpeed(swipeSpeed: Float): Builder {
            this.swipeSpeed = swipeSpeed
            return this
        }

        fun zoom(zoom: Boolean): Builder {
            this.zoom = zoom
            return this
        }

        fun controls(controls: Boolean): Builder {
            this.controls = controls
            return this
        }

        fun autoPlay(autoPlay: Boolean): Builder {
            this.autoPlay = autoPlay
            return this
        }

        fun playBackwards(playBackwards: Boolean): Builder {
            this.playBackwards = playBackwards
            return this
        }

        fun fps(@IntRange(from = 1, to = 60) fps: Int): Builder {
            this.fps = fps
            return this
        }

        fun startActivity() = context.get()!!.startActivity(Intent(context.get(), SequentialImagePlayerActivity::class.java)
                .apply {
                    putExtra(Uri::class.java.canonicalName, uris?.map { it.toString() }?.toTypedArray())
                    putExtra(FPS, fps)
                    putExtra(ZOOM, zoom)
                    putExtra(PLAY_BACKWARDS, playBackwards)
                    putExtra(AUTO_PLAY, autoPlay)
                    putExtra(SHOW_CONTROLS, controls)
                    putExtra(SWIPE_SPEED, swipeSpeed / 10f)
                })

        companion object {

            internal const val FPS = "FPS"
            internal const val ZOOM = "ZOOM"
            internal const val PLAY_BACKWARDS = "PLAY_BACKWARDS"
            internal const val AUTO_PLAY = "AUTO_PLAY"
            internal const val SHOW_CONTROLS = "SHOW_CONTROLS"
            internal const val SWIPE_SPEED = "SWIPE_SPEED"

            fun with(context: Context): Builder = Builder().also { it.context = WeakReference(context) }
        }
    }
}