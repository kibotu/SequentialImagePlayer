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
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.TRANSLATABLE
import com.exozet.sequentialimage.player.SequentialImagePlayer.Companion.ZOOMABLE
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

            with(sequentialImagePlayer) {

                imageUris = files.map { Uri.parse(it) }.toTypedArray()
                fps = arguments.getInt(FPS) ?: 30
                playBackwards = arguments.getBoolean(PLAY_BACKWARDS) ?: false
                autoPlay = arguments.getBoolean(AUTO_PLAY) ?: true
                zoomable = arguments.getBoolean(ZOOMABLE) ?: true
                translatable = arguments.getBoolean(TRANSLATABLE) ?: true
                showControls = arguments.getBoolean(SHOW_CONTROLS) ?: false
                swipeSpeed = arguments.getFloat(SWIPE_SPEED) ?: 1f
            }
        }
    }

    class Builder {

        private lateinit var context: WeakReference<Context>

        private var uris: Array<Uri>? = null

        private var autoPlay: Boolean = true

        private var playBackwards: Boolean = false

        private var zoomable: Boolean = true

        private var translatable: Boolean = true

        private var showControls: Boolean = false

        private var swipeSpeed: Float = 1f

        private var fps: Int = 30

        fun swipeSpeed(swipeSpeed: Float): Builder {
            this.swipeSpeed = swipeSpeed
            return this
        }

        fun translatable(translatable: Boolean): Builder {
            this.translatable = translatable
            return this
        }

        fun zoomable(zoomable: Boolean): Builder {
            this.zoomable = zoomable
            return this
        }

        fun showControls(showControls: Boolean): Builder {
            this.showControls = showControls
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
                    putExtra(TRANSLATABLE, translatable)
                    putExtra(PLAY_BACKWARDS, playBackwards)
                    putExtra(AUTO_PLAY, autoPlay)
                    putExtra(SHOW_CONTROLS, showControls)
                    putExtra(SWIPE_SPEED, swipeSpeed)
                })

        companion object {
            fun with(context: Context): Builder = Builder().also { it.context = WeakReference(context) }
        }
    }
}