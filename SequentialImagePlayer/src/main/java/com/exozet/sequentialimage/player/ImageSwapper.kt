package com.exozet.sequentialimage.player

import kotlinx.android.synthetic.main.sequentialimageplayer_view.view.*
import kotlin.math.roundToLong

class ImageSwapper(var player: SequentialImagePlayer) {

    internal var index: Int

    init {
        index = 0
    }

    fun swapImage(index: Int) {
        this.index = SequentialImagePlayer.loopRange(index, max = player.max)
        player.loadImage(player.uris[this.index % player.max])
        player.seekBar.progress = this.index
        player.seekBar.max = player.max
    }

    fun run() {

        index = if (!player.playDirectionSwitch.isChecked) index + 1 else index - 1

        swapImage(index)

        val fps = (1000f / player.fpsSpinner.selectedItem.toString().toInt()).roundToLong()
        // Log("playing ${activity.uris[index % activity.max]} aiming for fps=$fps")

        player.viewHolder.postDelayed(this, fps)
    }
}