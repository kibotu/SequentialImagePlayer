# SequentialImagePlayer [![Android CI](https://github.com/kibotu/mobile-sequential-image-player/actions/workflows/android.yml/badge.svg)](https://github.com/kibotu/mobile-sequential-image-player/actions/workflows/android.yml) [![](https://jitpack.io/v/kibotu/mobile-sequential-image-player.svg)](https://jitpack.io/#kibotu/mobile-sequential-image-player) [![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21) [![Gradle Version](https://img.shields.io/badge/gradle-8.11.1-green.svg)](https://docs.gradle.org/current/release-notes) [![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-green.svg)](https://kotlinlang.org/) 

Native Sequential Image Player. Supports different FPS, auto play, and scrubbing forwards and backwards.

<!-- TOC -->
* [How to use](#how-to-use)
  * [As view](#as-view)
  * [As Standalone Activity](#as-standalone-activity)
* [How to install](#how-to-install)
* [Misc](#misc)
  * [Stabilize video](#stabilize-video)
  * [Merge 2 videos horizontally](#merge-2-videos-horizontally)
  * [Extract frames from video](#extract-frames-from-video)
  * [Resize images](#resize-images-)
  * [Optimize pngs](#optimize-pngs)
* [Changelog](#changelog)
* [Contributors](#contributors)
<!-- TOC -->

[![Screenshot](demo.gif)](demo.gif)

[![Screenshot](screenshot.png)](screenshot.png)

# How to use

## As view
```xml
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <com.exozet.sequentialimageplayer.SequentialImagePlayer
        android:id="@+id/sequentialImagePlayer"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

```kotlin
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
```

## As Standalone Activity

Start 360 Degree Activity by passing a bitmap file path or a bitmap

```kotlin
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
    .blurLetterbox() // default:true 
    .startActivity() 
```

# How to install

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.kibotu:SequentialImagePlayer:-SNAPSHOT'
}
```

# Misc

## Stabilize video

(Sauce: https://scottlinux.com/2016/09/17/video-stabilization-using-vidstab-and-ffmpeg-on-linux/)

```sh
ffmpeg -i example_walkaround.mov -vf vidstabdetect=shakiness=10:accuracy=15 -f null -
ffmpeg -i example_walkaround.mov -vf vidstabtransform=smoothing=30:input="transforms.trf" example_walkaround_stabilized.mp4
```

## Merge 2 videos horizontally

```sh
ffmpeg -i example_walkaround.mov -i example_walkaround_stabilized.mp4 -filter_complex "[0:v:0]pad=iw*2:ih[bg]; [bg][1:v:0]overlay=w" merged.mp4
```

## Extract frames from video

```sh
# 3 frames per second
ffmpeg -i example_walkaround_stabilized.mp4 -r 3/1 app/src/main/assets/out%03d.png
```

## Resize images 

```sh
mogrify -resize 1024x app/src/main/assets/default/*
```

## Optimize pngs

```sh
pngquant app/src/main/assets/default/**.png --ext .png --force
```

# Changelog

2.0.0
* Replaced kotlin extensions by view binding
* Android 15 Support
* Library Updates
  * Kotlin 2.1.0 
  * gradle wrapper update to 8.11.1
  * gradle build tools to 8.7.2  
  * gradle.settings overhaul
  
1.5.3
* Supports onProgressChanged events, e.g.: onProgressChanged = { degreeIndicator.rotation = it * 360 }
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

# Contributors

[Jan Rabe](jan.rabe@kibotu.net)

# LICENSE

```text
Copyright 2024 Jan Rabe

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
