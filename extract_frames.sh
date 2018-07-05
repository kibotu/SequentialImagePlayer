#!/usr/bin/env bash

# extract frames https://stackoverflow.com/a/10962408/1006741
ffmpeg -i example_walkaround_stabilized.mp4 -r 3/1 app/src/main/assets/out%03d.png

sh optimize.sh