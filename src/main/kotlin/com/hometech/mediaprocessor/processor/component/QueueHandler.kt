package com.hometech.mediaprocessor.processor.component

import com.hometech.mediaprocessor.configuration.AppProperties
import com.hometech.mediaprocessor.configuration.advice.exception.BaseProcessingException
import com.hometech.mediaprocessor.configuration.advice.exception.TransferException
import com.hometech.mediaprocessor.extension.exposed.findOrException
import com.hometech.mediaprocessor.processor.helper.FFMpegHelper
import com.hometech.mediaprocessor.processor.helper.FFProbeHelper
import com.hometech.mediaprocessor.processor.model.UploadedVideo
import com.hometech.mediaprocessor.processor.model.UploadedVideoInfo
import com.hometech.mediaprocessor.processor.model.toEntryInfo
import java.io.File
import java.nio.file.Path
import mu.KotlinLogging
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.concurrent.timer
import kotlin.reflect.full.primaryConstructor

private val log = KotlinLogging.logger { }
private const val POLLING_PERIOD = 1000L

@Component
class QueueHandler(
    private val s3TransferManager: AmazonTransferManager,
    private val callBackSender: CallBackSender,
    private val appProperties: AppProperties
) {

    private val cloudFront = appProperties.aws.cloudFront

    /**
     * FFmpeg use maximum available physical cores while video conversion
     * so no need to run more than one job at time
     */
    @EventListener(ApplicationReadyEvent::class)
    fun startDaemon() {
        timer(period = POLLING_PERIOD, initialDelay = 0) {
            try {
                process()
            } catch (ex: Exception) {
                log.error(ex) { }
            }
        }
    }

    internal fun process() {
        val info = transaction { UploadedVideo.pollOne()?.toEntryInfo() } ?: return
        try {
            val processedObjectKey = doProcessing(info)
            transaction {
                UploadedVideo
                    .findOrException(info.id)
                    .endProcessing(appProperties.aws.bucket, processedObjectKey, cloudFront)
            }
            callBackSender.sendSuccessCallback(info, processedObjectKey)
        } catch (ex: BaseProcessingException) {
            log.error(ex) { }
            transaction { UploadedVideo.findOrException(info.id).endProcessingExceptionally(ex) }
            callBackSender.sendFailedCallback(info, ex.status)
        } finally {
            cleanUp(info)
        }
    }

    private fun doProcessing(info: UploadedVideoInfo): String {
        log.debug { "Start processing for uploaded video with id '${info.id}'" }
        val file = download(info)
        val path = convert(file)
        return upload(path, info).also { log.debug { "End processing for uploaded video with id '${info.id}'" } }
    }

    private fun download(info: UploadedVideoInfo): File {
        return runTransferJob { s3TransferManager.download(info) }
    }

    private fun convert(file: File): Path {
        return runProcessingJob { FFMpegHelper.convert(file = file, metadata = FFProbeHelper.probe(file)) }
    }

    private fun upload(path: Path, info: UploadedVideoInfo): String {
        return runTransferJob { s3TransferManager.upload(path, info) }
    }

    private fun cleanUp(info: UploadedVideoInfo) {
        File("${appProperties.tempFilesPath}/${info.id}").deleteRecursively()
    }
}

private fun <T> runProcessingJob(block: () -> T): T {
    return runTranslating<T, BaseProcessingException>(block)
}

private fun <T> runTransferJob(block: () -> T): T {
    return runTranslating<T, TransferException>(block)
}

private inline fun <T, reified E : BaseProcessingException> runTranslating(block: () -> T): T {
    return try {
        block.invoke()
    } catch (e: Exception) {
        throw E::class.primaryConstructor!!.call(e)
    }
}
