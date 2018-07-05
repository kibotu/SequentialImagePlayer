#!/usr/bin/env bash

# https://gist.github.com/maxogden/43219d6dcb9006042849

# stabilize
ffmpeg -i example_walkaround.mov -vf vidstabdetect=shakiness=10:accuracy=15 -f null -
ffmpeg -i example_walkaround.mov -vf vidstabtransform=smoothing=30:input="transforms.trf" example_walkaround_stabilized.mp4

# merge for comparison
ffmpeg -i example_walkaround.mov -i example_walkaround_stabilized.mp4 -filter_complex "[0:v:0]pad=iw*2:ih[bg]; [bg][1:v:0]overlay=w" merged.mp4