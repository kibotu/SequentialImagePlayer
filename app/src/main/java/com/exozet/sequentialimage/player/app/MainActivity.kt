package com.exozet.sequentialimage.player.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.exozet.sequentialimage.player.SequentialImagePlayer
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        default_video.setOnClickListener {
            startSequentialPlayer((1 until 317).map { String.format("default/out%d.png", it) }.toList())
        }
        stabilized_video.setOnClickListener {
            startSequentialPlayer((1 until 192).map { String.format("stabilized/out%03d.png", it) }.toList())
        }
    }

    private fun startSequentialPlayer(list: List<String>) {
        SequentialImagePlayer
                .with(this)
                // .internalStorageFiles(list)
                .assetFiles(list)
                // .externalStorageFiles(list)
                // .files(list)
                .fps(24) // default: 30
                .playBackwards(false) // default: false
                .autoPlay(false) // default: true
                .zoom(true) // default: true
                .controls(true) // default: false
                .swipeSpeed(0.8f) // default: 1
                .startActivity()
    }
}