package com.hometech.mediaprocessor.processor.model

data class FileMetadata(val video: VideoMetadata, val audio: AudioMetadata?)

class VideoMetadata(
    val width: Int,
    val height: Int,
    val frames: Int,
    val fps: Int,
    bitrate: Int
) {
    val bitrate = bitrate / 1000
}

class AudioMetadata(bitrate: Int) {
    val bitrate = bitrate / 1000
}
