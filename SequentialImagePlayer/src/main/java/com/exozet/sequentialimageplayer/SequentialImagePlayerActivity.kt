package com.exozet.sequentialimageplayer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.appcompat.app.AppCompatActivity
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.AUTO_PLAY
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.BLUR_LETTERBOX
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.DURATION
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.FPS
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.PLAY_BACKWARDS
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.SHOW_CONTROLS
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.SWIPE_SPEED
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.TRANSLATABLE
import com.exozet.sequentialimageplayer.SequentialImagePlayer.Companion.ZOOMABLE
import com.exozet.sequentialimageplayer.databinding.ActivitySequentialimagePlayerBinding
import java.lang.ref.WeakReference


class SequentialImagePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySequentialimagePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySequentialimagePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.extras?.let { arguments ->

            val files = arguments.getParcelableArray(Uri::class.java.canonicalName)
            if (files == null || files.isEmpty()) {
                finish()
                return
            }

            with(binding.sequentialImagePlayer) {

                imageUris = files.map { it as Uri }.toTypedArray()
                if (arguments.containsKey(FPS)) {
                    fps = arguments.getInt(FPS)
                }
                if (arguments.containsKey(DURATION)) {
                    duration = arguments.getInt(DURATION)
                }
                playBackwards = arguments.getBoolean(PLAY_BACKWARDS) == true
                autoPlay = arguments.getBoolean(AUTO_PLAY) ?: true
                zoomable = arguments.getBoolean(ZOOMABLE) ?: true
                translatable = arguments.getBoolean(TRANSLATABLE) ?: true
                showControls = arguments.getBoolean(SHOW_CONTROLS) ?: false
                swipeSpeed = arguments.getFloat(SWIPE_SPEED) ?: 1f
                blurLetterbox = arguments.getBoolean(BLUR_LETTERBOX) ?: true
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

        @FloatRange(from = -1.0, to = 1.0)
        private var swipeSpeed: Float = 1f

        @IntRange(from = 1)
        private var fps: Int? = null

        @IntRange(from = 0)
        private var duration: Int? = null

        private var blurLetterbox: Boolean = true

        fun blurLetterbox(blurLetterbox: Boolean = true): Builder {
            this.blurLetterbox = blurLetterbox
            return this
        }

        /**
         * percentage of swiped screen to total duration, e.g. swiping from 0 to screen width seeks from 0 to frame count
         */
        fun swipeSpeed(@FloatRange(from = -1.0, to = 1.0) swipeSpeed: Float): Builder {
            this.swipeSpeed = swipeSpeed
            return this
        }

        fun translatable(translatable: Boolean = true): Builder {
            this.translatable = translatable
            return this
        }

        fun zoomable(zoomable: Boolean = true): Builder {
            this.zoomable = zoomable
            return this
        }

        fun showControls(showControls: Boolean = true): Builder {
            this.showControls = showControls
            return this
        }

        fun autoPlay(autoPlay: Boolean = true): Builder {
            this.autoPlay = autoPlay
            return this
        }

        fun playBackwards(playBackwards: Boolean = true): Builder {
            this.playBackwards = playBackwards
            return this
        }

        fun fps(@IntRange(from = 1) fps: Int): Builder {
            this.fps = fps
            return this
        }

        fun duration(@IntRange(from = 0) duration: Int): Builder {
            this.duration = duration
            return this
        }

        fun uris(uris: Array<Uri>): Builder {
            this.uris = uris
            return this
        }

        fun startActivity() =
            context.get()!!.startActivity(Intent(context.get(), SequentialImagePlayerActivity::class.java)
                .apply {
                    putExtra(Uri::class.java.canonicalName, uris)
                    fps?.let { putExtra(FPS, it) }
                    duration?.let { putExtra(DURATION, it) }
                    putExtra(ZOOMABLE, zoomable)
                    putExtra(TRANSLATABLE, translatable)
                    putExtra(PLAY_BACKWARDS, playBackwards)
                    putExtra(AUTO_PLAY, autoPlay)
                    putExtra(SHOW_CONTROLS, showControls)
                    putExtra(SWIPE_SPEED, swipeSpeed)
                    putExtra(BLUR_LETTERBOX, blurLetterbox)
                })

        companion object {
            fun with(context: Context): Builder = Builder().also { it.context = WeakReference(context) }
        }
    }
}