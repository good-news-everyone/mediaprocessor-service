package com.hometech.mediaprocessor.processor.helper

import com.github.kokorin.jaffree.LogLevel
import com.github.kokorin.jaffree.ffmpeg.ChannelInput
import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import com.github.kokorin.jaffree.ffmpeg.UrlOutput
import com.hometech.mediaprocessor.configuration.Os
import com.hometech.mediaprocessor.configuration.hostOs
import com.hometech.mediaprocessor.processor.model.FileMetadata
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption.READ
import mu.KotlinLogging
import kotlin.io.path.name
import kotlin.math.min

private val log = KotlinLogging.logger { }

object FFMpegHelper {

    fun check() {
        FFmpeg.atPath().setLogLevel(LogLevel.ERROR).addArgument("-version").execute()
    }

    /**
     * ffmpeg -y -i test.mp4
     * -c:v libx264 -x264opts "keyint=24:min-keyint=24:no-scenecut" -r 24
     * -c:a aac -b:a 128k
     * -bf 1 -b_strategy 0 -sc_threshold 0 -pix_fmt yuv420p
     * -map 0:v:0 -map 0:v:0 -map 0:v:0 -map 0:v:0 -map 0:a:0
     * -b:v:0 600k  -filter:v:0 "scale=-2:240,setdar=dar=16/9" -profile:v:0 baseline
     * -b:v:1 1250k -filter:v:1 "scale=-2:480,setdar=dar=16/9" -profile:v:1 main
     * -b:v:2 2500k -filter:v:2 "scale=-2:720,setdar=dar=16/9" -profile:v:2 high
     * -b:v:3 5000k -filter:v:3 "scale=-2:1080,setdar=dar=1920/1080" -profile:v:3 high10
     * -use_timeline 1 -use_template 1 -frag_duration 5
     * -adaptation_sets "id=0,streams=v id=1,streams=a"
     * -f dash out.mpd
     */

    fun convert(file: File, metadata: FileMetadata): Path {
        val output = Path.of("${file.toPath().parent}/out/${file.nameWithoutExtension}.mpd").also {
            Files.createDirectories(it.parent)
        }
        log.debug { "Video processing started: ${file.name}" }
        val targetFps = min(metadata.video.fps, TARGET_FPS)
        FFmpeg.atPath()
            .setLogLevel(LogLevel.ERROR)
            .addArgument("-y")
            .addInput(file.toChannelInput())
            .apply {
                addArguments("-c:v", "libx264")
                addArguments("-x264opts", "keyint=$targetFps:min-keyint=24:no-scenecut")
                addArguments("-r", "$targetFps")
                if (metadata.audio != null) {
                    addArguments("-c:a", "aac")
                    addArguments("-b:a", "${min(metadata.audio.bitrate, AUDIO_BITRATE)}k")
                }
                addArguments("-bf", "1")
                addArguments("-b_strategy", "0")
                addArguments("-sc_threshold", "0")
                addArguments("-pix_fmt", "yuv420p")
                addMappings(metadata)
                addFallbackResolution(metadata)
                VideoQuality.all.forEach { addResolutionArg(metadata, it) }
                addArguments("-use_timeline", "1")
                addArguments("-use_template", "1")
                addArguments("-frag_duration", "5")
                addArguments("-adaptation_sets", "id=0,streams=v id=1,streams=a")
                addArguments("-f", "dash")
            }
            .also {
                Files.createDirectories(file.toPath().parent)
                Path.of("${file.toPath()}/out")
            }
            .addOutput(output.toUrlOutput())
            .setProgressListener {
                val targetFrames = metadata.video.frames * targetFps / metadata.video.fps
                log.debug { calculateProgress(it.frame, targetFrames) }
            }
            .execute()
        return output
    }

    private fun File.toChannelInput() = ChannelInput.fromChannel(Files.newByteChannel(this.toPath(), READ))

    // Воркэраунд для винды https://stackoverflow.com/a/42249107
    private fun Path.toUrlOutput(): UrlOutput {
        return if (hostOs == Os.WINDOWS) {
            val outputString = "${toString().substringBeforeLast("\\")}/$name"
            UrlOutput.toUrl(outputString)
        } else {
            UrlOutput.toPath(this)
        }
    }

    private fun FFmpeg.addMappings(metadata: FileMetadata) {
        repeat(times = VideoQuality.countMatching(metadata).oneIfZero()) {
            addArguments("-map", "0:v:0")
        }
        if (metadata.audio != null) addArguments("-map", "0:a:0")
    }

    private fun FFmpeg.addResolutionArg(metadata: FileMetadata, quality: VideoQuality) {
        if (metadata.video.height >= quality.height) {
            addArguments("-b:v:${quality.ordinal}", "${min(quality.bitrate, metadata.video.bitrate)}k")
            addArguments("-filter:v:${quality.ordinal}", "scale=-2:${quality.height},setdar=dar=${metadata.video.width}/${metadata.video.height}")
            addArguments("-profile:v:${quality.ordinal}", quality.profile)
        }
    }

    private fun FFmpeg.addFallbackResolution(metadata: FileMetadata) {
        if (VideoQuality.countMatching(metadata) == 0) {
            this.addResolutionArg(metadata, quality = VideoQuality.P240)
        }
    }

    private fun Int.oneIfZero() = if (this == 0) 1 else this

    private enum class VideoQuality(val height: Int, val profile: String, val bitrate: Int) {
        P240(height = 240, "baseline", bitrate = 1000),
        P480(height = 480, "main", bitrate = 2500),
        P720(height = 720, "high", bitrate = 5000),
        P1080(height = 1080, "high10", bitrate = 8000);

        companion object {
            val all = VideoQuality.values()
            fun countMatching(metadata: FileMetadata) = all.count { it.height <= metadata.video.height }
        }
    }

    private fun calculateProgress(current: Long, total: Int): String {
        val progress = current.toDouble() / total * HUNDRED
        val percents = min(progress.toInt(), HUNDRED)
        return "[${"█".repeat(percents)}${" ".repeat(HUNDRED - percents)}] | $current of $total Frames"
    }
}

private const val HUNDRED = 100
private const val AUDIO_BITRATE = 128
private const val TARGET_FPS = 30
