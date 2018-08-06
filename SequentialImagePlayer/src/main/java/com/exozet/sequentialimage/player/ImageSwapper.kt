package com.exozet.sequentialimage.player

import kotlinx.android.synthetic.main.sequentialimageplayer_view.view.*
import kotlin.math.roundToLong

class ImageSwapper(var player: SequentialImagePlayer) : Runnable {

    internal var index: Int

    init {
        index = 0
    }

    fun swapImage(index: Int) {
        if (player.max <= 0)
            return

        this.index = SequentialImagePlayer.loopRange(index, max = player.max)
        player.loadImage(player.imageUris[this.index % player.max])
        player.seekBar.progress = this.index
        player.seekBar.max = player.max

        player.onProgressChanged?.invoke(player.progress)
    }

    override fun run() {

        index = if (!player.playDirectionSwitch.isChecked) index + 1 else index - 1

        player.busy()
        swapImage(index)

        player.cancelBusy()

        val fps = (1000f / player.fps).roundToLong()
        // Log("playing ${activity.uris[index % activity.max]} aiming for fps=$fps")

        player.viewHolder.postDelayed(this, fps)
    }
}