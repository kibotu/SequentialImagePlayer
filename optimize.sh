#!/usr/bin/env bash

# resize
mogrify -resize 1024x app/src/main/assets/default/*

# optimize
pngquant app/src/main/assets/default/**.png --ext .png --force