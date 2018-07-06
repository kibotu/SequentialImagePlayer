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
                .assetFiles(list)
                .fps(30)
                .startActivity()
    }
}