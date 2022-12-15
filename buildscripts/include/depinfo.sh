#!/bin/bash -e

## Dependency versions
# Make sure to keep v_ndk and v_ndk_n in sync, the numeric version can be found in source.properties
# also remember to update path.sh

v_sdk=9123335_latest
v_ndk=r25b
v_ndk_n=25.1.8937393
v_sdk_build_tools=33.0.1

v_lua=5.2.4
v_harfbuzz=5.3.1
v_fribidi=1.0.12
v_freetype=2-12-1
v_mbedtls=2.28.2
v_libxml2=2.10.3


## Dependency tree
# I would've used a dict but putting arrays in a dict is not a thing

dep_mbedtls=()
dep_libxml2=()
dep_dav1d=()
dep_ffmpeg=(mbedtls dav1d libxml2)
dep_freetype2=()
dep_fribidi=()
dep_harfbuzz=()
dep_libass=(freetype2 fribidi harfbuzz)
dep_lua=()
dep_mpv=(ffmpeg libass lua)
dep_mpv_android=(mpv)


## Travis-related

# pinned ffmpeg commit used by travis-ci
v_travis_ffmpeg=f55c91497d4d16d393ae9c034bd3032a683802ca

# filename used to uniquely identify a build prefix
travis_tarball="prefix-ndk-${v_ndk}-lua-${v_lua}-harfbuzz-${v_harfbuzz}-fribidi-${v_fribidi}-freetype-${v_freetype}-mbedtls-${v_mbedtls}libxml2-${v_libxml2}-ffmpeg-${v_travis_ffmpeg}.tgz"
