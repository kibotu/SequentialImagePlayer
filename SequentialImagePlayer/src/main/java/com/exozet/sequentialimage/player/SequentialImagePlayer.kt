package com.exozet.sequentialimage.player

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.annotation.IntRange
import java.lang.ref.WeakReference

class SequentialImagePlayer {

    private lateinit var context: WeakReference<Context>

    private var uris: Array<Uri>? = null

    private var autoPlay: Boolean = true

    private var playBackwards: Boolean = false

    private var zoom: Boolean = false

    private var controls: Boolean = false

    private var swipeSpeed: Float = 1f

    private var fps: Int = 30

    fun swipeSpeed(swipeSpeed: Float): SequentialImagePlayer {
        this.swipeSpeed = swipeSpeed
        return this
    }

    fun zoom(zoom: Boolean): SequentialImagePlayer {
        this.zoom = zoom
        return this
    }

    fun controls(controls: Boolean): SequentialImagePlayer {
        this.controls = controls
        return this
    }

    fun autoPlay(autoPlay: Boolean): SequentialImagePlayer {
        this.autoPlay = autoPlay
        return this
    }

    fun playBackwards(playBackwards: Boolean): SequentialImagePlayer {
        this.playBackwards = playBackwards
        return this
    }

    fun fps(@IntRange(from = 1, to = 60) fps: Int): SequentialImagePlayer {
        this.fps = fps
        return this
    }

    fun assetFiles(files: List<String>): SequentialImagePlayer {
        uris = files.map { Uri.parse("file:///android_asset/$it") }.toTypedArray()
        return this
    }

    fun internalStorageFiles(files: List<String>): SequentialImagePlayer {
        uris = files.map { Uri.parse("${context.get()?.filesDir?.absolutePath}/$it") }.toTypedArray()
        return this
    }

    fun externalStorageFiles(files: List<String>): SequentialImagePlayer {
        uris = files.map { Uri.parse("${Environment.getExternalStorageDirectory()}/$it") }.toTypedArray()
        return this
    }

    fun files(files: List<String>): SequentialImagePlayer {
        uris = files.map { Uri.parse(it) }.toTypedArray()
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

        fun with(context: Context): SequentialImagePlayer = SequentialImagePlayer().also { it.context = WeakReference(context) }
    }
}