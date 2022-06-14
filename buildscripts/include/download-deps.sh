#!/bin/bash -e

. ./include/depinfo.sh

[ -z "$TRAVIS" ] && TRAVIS=0
[ -z "$WGET" ] && WGET=wget

mkdir -p deps && cd deps

# mbedtls
if [ ! -d mbedtls ]; then
	mkdir mbedtls
	$WGET https://github.com/ARMmbed/mbedtls/archive/mbedtls-$v_mbedtls.tar.gz -O - | \
		tar -xz -C mbedtls --strip-components=1
fi

# dav1d
[ ! -d dav1d ] && git clone https://github.com/tanersener/dav1d
( cd dav1d; git checkout 0.9.2 )

# ffmpeg
if [ ! -d ffmpeg ]; then
	git clone https://github.com/tanersener/FFmpeg ffmpeg
	( cd ffmpeg; git checkout 30322ebe3c55d0fb18bea4ae04d0fcaf1f97d27f )
fi

# freetype2
[ ! -d freetype2 ] && git clone https://github.com/tanersener/freetype2
( cd freetype2; git checkout VER-2-11-1 )

# fribidi
if [ ! -d fribidi ]; then
	git clone https://github.com/tanersener/fribidi
	( cd fribidi; git checkout v1.0.11 )
fi

# harfbuzz
if [ ! -d harfbuzz ]; then
	git clone https://github.com/tanersener/harfbuzz
	( cd harfbuzz; git checkout 3.2.0 )
fi

# libass
[ ! -d libass ] && git clone https://github.com/tanersener/libass
( cd libass; git checkout 0.15.2 )

# lua
if [ ! -d lua ]; then
	mkdir lua
	$WGET http://www.lua.org/ftp/lua-$v_lua.tar.gz -O - | \
		tar -xz -C lua --strip-components=1
fi

# mpv
[ ! -d mpv ] && git clone https://github.com/mpv-player/mpv
( cd mpv; git checkout v0.34.1 )

cd ..
