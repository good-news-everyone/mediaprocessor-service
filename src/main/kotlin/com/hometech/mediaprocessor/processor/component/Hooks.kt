package com.hometech.mediaprocessor.processor.component

import com.hometech.mediaprocessor.configuration.AppProperties
import com.hometech.mediaprocessor.configuration.Os
import com.hometech.mediaprocessor.configuration.hostOs
import com.hometech.mediaprocessor.processor.helper.FFMpegHelper
import java.io.File
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class Hooks(private val appProperties: AppProperties) {

    @PostConstruct
    fun checkRequirements() {
        // On macOS fails ffprobe
        if (hostOs != Os.MAC) check()
    }

    private fun check() {
        log.info { "Checking requirements: ffmpeg" }
        FFMpegHelper.check()
    }

    @PreDestroy
    fun cleanUp() {
        log.info { "Cleaning up temp file directory: ${appProperties.tempFilesPath}" }
        File(appProperties.tempFilesPath).deleteRecursively()
    }
}
