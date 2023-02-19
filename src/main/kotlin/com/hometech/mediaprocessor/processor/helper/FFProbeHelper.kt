package com.hometech.mediaprocessor.processor.helper

import com.github.kokorin.jaffree.LogLevel
import com.github.kokorin.jaffree.StreamType
import com.github.kokorin.jaffree.ffprobe.FFprobe
import com.hometech.mediaprocessor.processor.model.AudioMetadata
import com.hometech.mediaprocessor.processor.model.FileMetadata
import com.hometech.mediaprocessor.processor.model.VideoMetadata
import java.io.File
import kotlin.math.round

object FFProbeHelper {

    fun probe(file: File): FileMetadata {
        val result = FFprobe.atPath()
            .setLogLevel(LogLevel.ERROR)
            .setShowStreams(true)
            .setInput(file.toPath())
            .execute()
        val audio = result.streams.firstOrNull { it.codecType == StreamType.AUDIO }?.let { AudioMetadata(it.bitRate) }
        val video = result.streams.first { it.codecType == StreamType.VIDEO }.let {
            val isRotated = it.sideDataList?.firstOrNull()?.getLong("rotation")?.let { angle -> angle.mod(ROTATION) == 0 } ?: false
            VideoMetadata(
                width = if (isRotated) it.height else it.width,
                height = if (isRotated) it.width else it.height,
                bitrate = it.bitRate,
                frames = it.nbFrames,
                fps = round(it.nbFrames / it.duration).toInt()
            )
        }
        return FileMetadata(audio = audio, video = video)
    }
}

private const val ROTATION = 90
