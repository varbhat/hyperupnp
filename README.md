<h1 align="center">Hyper UPnP</h1> 
<p align="center">Android UPnP/DLNA client</p>

<br>

<p align="center">Stream Media from PC, NAS or any other device running UPnP/DLNA compliant media server to your Android Device.</p>

## Introduction
* Hyper UPnP is a free, simple and easy to use Android Application for browsing UPnP and DLNA media servers and Media on Local Network.
* Media can be streamed or downloaded to the device.
* It focuses on delivering a simple and lightweight experience.
* Supports Searching.
* Uses Modern Material You Design.

## Installation
[APK](https://github.com/varbhat/hyperupnp/releases/latest/download/app-debug.apk) can be downloaded from [Releases](https://github.com/varbhat/hyperupnp/releases/latest). It is built on each push by Github Actions. Note that it is debug build and Google Play Store may warn you while installation.

## Building Application
This Application can be built in Android Studio or Command Line.
Please Refer:
1. [Build and run Application](https://developer.android.com/studio/run)
2. [Building Application in Command Line](https://developer.android.com/studio/build/building-cmdline)

## DLNA Server
You can use [Universal Media Server](https://www.universalmediaserver.com), [Gerbara](https://gerbera.io/), [ReadyMedia](https://sourceforge.net/projects/minidlna/), [dms](https://github.com/anacrolix/dms) or other UPnP/DLNA Media Servers to setup DLNA Server on Device. Serving Device and HyperUPnP-installed Android Device must be on same network. Check that network of HyperUPnP is not tunneled through any VPN. Transcoding in DLNA Server is not at all necessary because modern media players like [mpv-android](https://github.com/mpv-android/mpv-android) and [VLC](https://www.videolan.org/vlc/download-android.html) can play Media of High Resoultion(or any resoultion) and supports most codecs you ever need.

```bash
# Serving Media with dms
dms -noTranscode -noProbe -path /path/to/serving/directory
```

## License
[GPL-v3](LICENSE)
