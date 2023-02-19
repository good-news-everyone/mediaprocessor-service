# mediaprocessor-service
___

The purpose of this microservice - convert uploaded videos to MDP format and send callback to another microservice
To start and test service you should install `ffmpeg` first.

### Windows:

1. Download archive https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z
2. Unzip content into the root directory of your `C:` drive
3. Rename directory to `ffmpeg`
4. Run PowerShell with admin privileges and run following command: `setx /m PATH "C:\ffmpeg\bin;%PATH%"`
5. Restart PowerShell and Intellij IDEA

### MacOS:

1. Just run `brew install ffmpeg`

### Linux:

1. Use your favorite package manager.

___

### Проверка окружения
For test installation you can use next command: `ffmpeg -version`. Output should be similar to listing below:
```
> ffmpeg -version
ffmpeg version 2022-05-23-git-6076dbcb55-full_build-www.gyan.dev Copyright (c) 2000-2022 the FFmpeg developers
built with gcc 11.3.0 (Rev1, Built by MSYS2 project)
configuration: --enable-gpl --enable-version3 --enable-static --disable-w32threads --disable-autodetect --enable-fontconfig --enable-iconv --enable-gnutls --enable-libxml2 --enable-gmp --enable-bzlib --enable-lzma --enable-libsnappy --enable-zlib --enable-librist --enable-libsrt --enable-libssh --enable-libzmq --enable-avisynth --enable-libbluray --enable-libcaca --enable-sdl2 --enable-libdav1d --enable-libdavs2 --enable-libuavs3d --enable-libzvbi --enable-librav1e --enable-libsvtav1 --enable-libwebp --enable-libx264 --enable-libx265 --enable-libxavs2 --enable-libxvid --enable-libaom --enable-libjxl --enable-libopenjpeg --enable-libvpx --enable-mediafoundation --enable-libass --enable-frei0r --enable-libfreetype --enable-libfribidi --enable-liblensfun --enable-libvidstab --enable-libvmaf --enable-libzimg --enable-amf --enable-cuda-llvm --enable-cuvid --enable-ffnvcodec --enable-nvdec --enable-nvenc --enable-d3d11va --enable-dxva2 --enable-libmfx --enable-libshaderc --enable-vulkan --enable-libplacebo --enable-opencl --enable-libcdio --enable-libgme --enable-libmodplug --enable-libopenmpt --enable-libopencore-amrwb --enable-libmp3lame --enable-libshine --enable-libtheora --enable-libtwolame --enable-libvo-amrwbenc --enable-libilbc --enable-libgsm --enable-libopencore-amrnb --enable-libopus --enable-libspeex --enable-libvorbis --enable-ladspa --enable-libbs2b --enable-libflite --enable-libmysofa --enable-librubberband --enable-libsoxr --enable-chromaprint
libavutil      57. 25.100 / 57. 25.100
libavcodec     59. 28.100 / 59. 28.100
libavformat    59. 24.100 / 59. 24.100
libavdevice    59.  6.100 / 59.  6.100
libavfilter     8. 38.100 /  8. 38.100
libswscale      6.  6.100 /  6.  6.100
libswresample   4.  6.100 /  4.  6.100
libpostproc    56.  5.100 / 56.  5.100
```
