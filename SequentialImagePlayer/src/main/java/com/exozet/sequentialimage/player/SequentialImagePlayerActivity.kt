package com.exozet.sequentialimage.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.AUTO_PLAY
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.FPS
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.PLAY_BACKWARDS
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.SHOW_CONTROLS
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.SWIPE_SPEED
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.ZOOM
import kotlinx.android.synthetic.main.activity_sequentialimage_player.*
import java.lang.ref.WeakReference


class SequentialImagePlayerActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sequentialimage_player)

        intent?.extras?.let { arguments ->

            val files = arguments.getStringArray(Uri::class.java.canonicalName)
            if (files == null || files.isEmpty()) {
                finish()
                return
            }

            sequentialImagePlayer.imageUris = files.map { Uri.parse(it) }.toTypedArray()

//        playDirectionSwitch.isChecked = intent?.extras?.getBoolean(SequentialImagePlayerActivity.Builder.PLAY_BACKWARDS) ?: false
//        autoplaySwitch.isChecked = intent?.extras?.getBoolean(SequentialImagePlayerActivity.Builder.AUTO_PLAY) ?: true
//        viewHolder.isZoomable = intent?.extras?.getBoolean(SequentialImagePlayerActivity.Builder.ZOOM) ?: true
//        showControls(intent?.extras?.getBoolean(SequentialImagePlayerActivity.Builder.SHOW_CONTROLS)
//                ?: false)
//        swipeSpeed = intent?.extras?.getFloat(SequentialImagePlayerActivity.Builder.SWIPE_SPEED) ?: 1f


            var fps = arguments.getInt(FPS)
                    ?: 30

            var playBackwards = arguments.getBoolean(PLAY_BACKWARDS)
                    ?: false
            var autoPlay = arguments.getBoolean(AUTO_PLAY)
                    ?: true
            var zoomable = arguments.getBoolean(ZOOM)
                    ?: true

            var showControls = arguments.getBoolean(SHOW_CONTROLS)
                    ?: false

            var swipeSpeed = arguments.getFloat(SWIPE_SPEED)
                    ?: 1f
        }
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
            fun with(context: Context): Builder = Builder().also { it.context = WeakReference(context) }
        }
    }
}