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

    private var fps: Int = 30

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
            })

    companion object {

        const val FPS = "FPS"

        fun with(context: Context): SequentialImagePlayer = SequentialImagePlayer().also { it.context = WeakReference(context) }
    }
}