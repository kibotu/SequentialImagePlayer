# SequentialImagePlayer [![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15) [![Gradle Version](https://img.shields.io/badge/gradle-4.8.1-green.svg)](https://docs.gradle.org/current/release-notes)  [![Kotlin](https://img.shields.io/badge/kotlin-1.2.51-green.svg)](https://kotlinlang.org/)  

Native Sequential Image Player. Supports different fps, auto play and scrubbing for-/ and backwards.  

[![Screenshot](https://git.exozet.com/mobile-de/POC/android-walkthroug-player/blob/master/demo.gif)](https://git.exozet.com/mobile-de/POC/android-walkthroug-player/blob/master/demo.gif)


# How to use

Start 360 Degree Activity by passing bitmap file path for or an  bitmap

    var list = (1 until 192).map { String.format("stabilized/out%03d.png", it) }.toList()

    SequentialImagePlayer
            .with(this)
            // .internalStorageFiles(list)
            .assetFiles(list)
            // .externalStorageFiles(list)
            // .files(list)
            .fps(24) // default: 30 [1:60]
            .playBackwards(false) // default: false
            .autoPlay(true) // default: true
            .zoom(true) // default: true
            .controls(true) // default: false
            .startActivity()
     
# How to install (tbd)

Atm only as module
    
    dependencies {
        api project(':SequentialImagePlayer')
    }

## Stabilize video

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

# TODO

* Supports swiping  

## Contributors

[Jan Rabe](jan.rabe@exozet.com)