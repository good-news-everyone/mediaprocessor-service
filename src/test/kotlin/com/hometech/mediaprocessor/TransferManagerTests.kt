package com.hometech.mediaprocessor

import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.transfer.Download
import com.amazonaws.services.s3.transfer.Upload
import com.hometech.mediaprocessor.configuration.AppProperties
import com.hometech.mediaprocessor.processor.component.AmazonTransferManager
import com.hometech.mediaprocessor.processor.model.UploadedVideoInfo
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.mockk.every
import io.mockk.mockkClass
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class TransferManagerTests : AbstractIntegrationTest() {

    @Autowired
    lateinit var s3TransferManager: AmazonTransferManager

    @Autowired
    lateinit var appProperties: AppProperties

    @Test
    fun `should invoke download fun and create directory with new file`() {
        val info = UploadedVideoInfo(UUID.randomUUID(), key = "sample/test.mp4")
        every {
            awsTransferManager.download(GetObjectRequest(appProperties.aws.bucket, info.key), any())
        } returns mockkClass(Download::class, relaxed = true)

        s3TransferManager.download(info)

        Path.of("${appProperties.tempFilesPath}/${info.id}/test.mp4").toFile().exists().shouldBeTrue()
    }

    @Test
    fun `should invoke upload fun and return key`() {
        every { awsTransferManager.upload(any()) } returns mockkClass(Upload::class, relaxed = true)
        val info = UploadedVideoInfo(UUID.randomUUID(), key = "sample/test.mp4")
        val basePath = "${appProperties.tempFilesPath}/${info.id}/out"
        val file = File("$basePath/test.mpd").also {
            Files.createDirectories(Path.of(basePath))
            it.createNewFile()
        }

        s3TransferManager.upload(output = file.toPath(), info) shouldBe "sample/mpd/test.mpd"
    }
}
