# SequentialImagePlayer [![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15) [![Gradle Version](https://img.shields.io/badge/gradle-4.9-green.svg)](https://docs.gradle.org/current/release-notes)  [![Kotlin](https://img.shields.io/badge/kotlin-1.2.60-green.svg)](https://kotlinlang.org/)  

Native Sequential Image Player. Supports different fps, auto play and scrubbing for-/ and backwards.  

[![Screenshot](demo.gif)](demo.gif)

[![Screenshot](screenshot.png)](screenshot.png)

# How to use

## As view

    <androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    
        <com.exozet.sequentialimage.player.SequentialImagePlayer
            android:id="@+id/sequentialImagePlayer"
            android:layout_width="0dp"
            android:layout_height="0dp"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintTop_toTopOf="parent" />
        
    </androidx.constraintlayout.widget.ConstraintLayout>

        
    private fun startSequentialPlayer(list: List<Uri>) = with(sequentialImagePlayer) {
        imageUris = list.toTypedArray()
        autoPlay = true
        fps = 30
        playBackwards = false
        zoomable = true
        translatable = true
        showControls = false
        swipeSpeed = 0.7f
        blurLetterbox = true
    }

## As Standalone Activity    

Start 360 Degree Activity by passing bitmap file path for or an  bitmap

    (1 until 192).map { parseAssetFile(String.format("stabilized/out%03d.png", it)) }.toTypedArray()

    SequentialImagePlayerActivity.Builder
                  .with(this)
                  .uris(list)
                  .fps(24) // default: 30
                  .playBackwards(false) // default: false
                  .autoPlay(false) // default: true
                  .zoomable(true) // default: true
                  .translatable(true) // default: true
                  .showControls(true) // default: false
                  .swipeSpeed(0.8f) // default: 1
                  .blurLetterbox() // default: true
                  .startActivity()
     
# How to install (tbd)

Atm only as module
    
    dependencies {
        api project(':SequentialImagePlayer')
    }

## Stabilize video

(Sauce: https://scottlinux.com/2016/09/17/video-stabilization-using-vidstab-and-ffmpeg-on-linux/)

    ffmpeg -i example_walkaround.mov -vf vidstabdetect=shakiness=10:accuracy=15 -f null -
    ffmpeg -i example_walkaround.mov -vf vidstabtransform=smoothing=30:input="transforms.trf" example_walkaround_stabilized.mp4
    
## Merge 2 videos horizontally

    ffmpeg -i example_walkaround.mov -i example_walkaround_stabilized.mp4 -filter_complex "[0:v:0]pad=iw*2:ih[bg]; [bg][1:v:0]overlay=w" merged.mp4
    
## Extract frames from video

    # 3 frames per second
    ffmpeg -i example_walkaround_stabilized.mp4 -r 3/1 app/src/main/assets/out%03d.png

## Resize images 

    mogrify -resize 1024x app/src/main/assets/default/*
    
## Optimize pngs

    pngquant app/src/main/assets/default/**.png --ext .png --force
    
# Changelog

* Supports blurry letterbox effect
* Supports being added as custom view
* Supports translatable
* Supports swipe speed
* Supports swiping  
* Supports showing controls
* Supports autoplay backwards
* Supports autoplay
* Supports custom FPS 
* Supports pinch zoom
* Supports orientation changes
* Supports loading indicator
* Supports loading from asset-folder
* Supports loading from internal storage
* Supports loading from external storage
* Supports loading from file 

## Contributors

[Jan Rabe](jan.rabe@exozet.com)