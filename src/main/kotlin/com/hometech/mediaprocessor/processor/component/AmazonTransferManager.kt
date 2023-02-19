package com.hometech.mediaprocessor.processor.component

import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.PutObjectRequest
import com.amazonaws.services.s3.transfer.TransferManager
import com.hometech.mediaprocessor.configuration.AppProperties
import com.hometech.mediaprocessor.processor.model.UploadedVideoInfo
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger { }

@Component
class AmazonTransferManager(
    private val transferManager: TransferManager,
    private val appProperties: AppProperties
) {

    fun download(info: UploadedVideoInfo): File {
        log.debug { "Download started for file '${appProperties.aws.bucket}/${info.key}'" }
        return File("${createDirectory(info).toAbsolutePath()}/${info.key.filename()}")
            .also {
                it.createNewFile()
                transferManager.download(info.toRequest(), it).waitForCompletion()
            }
    }

    fun upload(output: Path, info: UploadedVideoInfo): String {
        return runBlocking(Dispatchers.IO) {
            output.parent.toFile()
                .also { log.debug { "Upload started for file set: ${output.parent}" } }
                .listFiles()
                .orEmpty()
                .map { async { upload(it, info) } }
                .awaitAll()
                .first { it.filename().extension() == MPD }
                .also { log.debug { "Upload finished for file set: ${output.parent}" } }
        }
    }

    suspend fun upload(file: File, info: UploadedVideoInfo): String {
        val key = "${info.key.dropFilename()}/$MPD/${file.toPath().fileName}"
        transferManager
            .upload(PutObjectRequest(appProperties.aws.bucket, key, file))
            .waitForCompletion()
        return key
    }

    private fun createDirectory(info: UploadedVideoInfo): Path {
        return Files.createDirectories(Path.of("${appProperties.tempFilesPath}/${info.id}"))
    }

    private fun UploadedVideoInfo.toRequest() = GetObjectRequest(appProperties.aws.bucket, this.key)

    private fun String.filename() = this.split("/").last()

    private fun String.dropFilename() = this.split("/").dropLast(1).joinToString(separator = "/")

    private fun String.extension() = this.substringAfterLast(".")
}

private const val MPD = "mpd"
