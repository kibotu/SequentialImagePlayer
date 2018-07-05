# SequentialImagePlayer [![API](https://img.shields.io/badge/API-15%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=15) [![Gradle Version](https://img.shields.io/badge/gradle-4.8.1-green.svg)](https://docs.gradle.org/current/release-notes)  [![Kotlin](https://img.shields.io/badge/kotlin-1.2.51-green.svg)](https://kotlinlang.org/)  

Native Sequential Image Player. Supports different fps, auto play and scrubbing for-/ and backwards.  

[![Screenshot](https://git.exozet.com/mobile-de/POC/android-walkthroug-player/blob/master/demo.gif)](https://git.exozet.com/mobile-de/POC/android-walkthroug-player/blob/master/demo.gif)

# How to use

TODO
     
# How to install (tbd)

TODO

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

## Contributors

[Jan Rabe](jan.rabe@exozet.com)