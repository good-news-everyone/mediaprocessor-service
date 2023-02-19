package com.hometech.mediaprocessor

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.verify
import com.hometech.mediaprocessor.configuration.AppProperties
import com.hometech.mediaprocessor.extension.exposed.findOrException
import com.hometech.mediaprocessor.helper.createUploadedVideo
import com.hometech.mediaprocessor.helper.shouldBeIgnoreMillis
import com.hometech.mediaprocessor.processor.component.AmazonTransferManager
import com.hometech.mediaprocessor.processor.component.QueueHandler
import com.hometech.mediaprocessor.processor.model.ConversionStatus
import com.hometech.mediaprocessor.processor.model.ConversionStatus.DONE
import com.hometech.mediaprocessor.processor.model.ConversionStatus.PROCESSING_ERROR
import com.hometech.mediaprocessor.processor.model.ConversionStatus.TRANSFER_ERROR
import com.hometech.mediaprocessor.processor.model.JobInfo
import com.hometech.mediaprocessor.processor.model.UploadedVideo
import com.hometech.mediaprocessor.processor.model.UploadedVideoInfo
import com.hometech.mediaprocessor.processor.model.UploadedVideoTable
import com.hometech.mediaprocessor.processor.model.toEntryInfo
import com.ninjasquad.springmockk.MockkBean
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.mockk.every
import java.io.File
import java.nio.file.Path
import java.time.Instant
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType

class QueueHandlerTests : AbstractIntegrationTest() {

    @Autowired
    lateinit var queueHandler: QueueHandler

    @Autowired
    lateinit var appProperties: AppProperties

    @MockkBean
    lateinit var transferManager: AmazonTransferManager

    @AfterEach
    fun cleanUp() {
        transaction { UploadedVideoTable.deleteAll() }
    }

    @Test
    fun `should convert vertical video without sound stream`() {
        val key = "vertical/without/sound/mpd/test.mpd"
        val file = File({}::class.java.getResource("/video/vertical-no-sound/test.mp4")!!.path)
        val info = transaction { createUploadedVideo(key = "vertical/without/sound/test.mp4").toEntryInfo() }
        successCase(key, file, info)
    }

    @Test
    fun `should convert horizontal video`() {
        val key = "horizontal/mpd/test.mpd"
        val file = File({}::class.java.getResource("/video/horizontal/test.mp4")!!.path)
        val info = transaction { createUploadedVideo(key = "horizontal/test.mp4").toEntryInfo() }
        successCase(key, file, info)
    }

    @Test
    fun `should convert vertical iphone video`() {
        val key = "vertical/iphone/mpd/test.mpd"
        val file = File({}::class.java.getResource("/video/vertical-iphone/test.mov")!!.path)
        val info = transaction { createUploadedVideo(key = "vertical/iphone/test.mov").toEntryInfo() }
        successCase(key, file, info)
    }

    @Test
    fun `should convert vertical android video`() {
        val key = "vertical/android/mpd/test.mpd"
        val file = File({}::class.java.getResource("/video/vertical-android/test.mp4")!!.path)
        val info = transaction { createUploadedVideo(key = "vertical/android/test.mp4").toEntryInfo() }
        successCase(key, file, info)
    }

    @Test
    fun `should set state TRANSFER_ERROR if transfer manager throws exception`() {
        val info = transaction { createUploadedVideo(key = "invalid/video/test.mp4").toEntryInfo() }
        every { transferManager.download(info) } throws RuntimeException()
        every { transferManager.upload(any<Path>(), info) } returns "any"
        stub(JobInfo(id = info.id, status = TRANSFER_ERROR))

        queueHandler.process()

        verify(JobInfo(id = info.id, status = TRANSFER_ERROR))
        transaction { UploadedVideo.findOrException(info.id).verify(state = TRANSFER_ERROR) }
    }

    @Test
    fun `should set state PROCESSING_ERROR if ffmpeg throws exception`() {
        val file = File({}::class.java.getResource("/video/invalid/test.mp4")!!.path)
        val info = transaction { createUploadedVideo(key = "invalid/video/test.mp4").toEntryInfo() }
        every { transferManager.download(info) } returns file
        every { transferManager.upload(any<Path>(), info) } returns "any"
        stub(JobInfo(id = info.id, status = TRANSFER_ERROR))

        queueHandler.process()

        verify(JobInfo(id = info.id, status = PROCESSING_ERROR))
        transaction { UploadedVideo.findOrException(info.id).verify(state = PROCESSING_ERROR) }
    }

    private fun successCase(key: String, file: File, info: UploadedVideoInfo) {
        every { transferManager.download(info) } returns file
        every { transferManager.upload(any<Path>(), info) } returns key
        stub(JobInfo(id = info.id, processedObjectKey = key))

        queueHandler.process()

        verify(JobInfo(id = info.id, processedObjectKey = key))
        transaction { UploadedVideo.findOrException(info.id).verify(key) }
    }

    private fun UploadedVideo.verify(key: String? = null, state: ConversionStatus = DONE) {
        this.processedObjectKey shouldBe key
        this.status shouldBe state
        this.workerId.shouldNotBeNull()
        if (state == DONE) {
            this.processedAt shouldBeIgnoreMillis Instant.now()
            this.workerId.shouldNotBeNull()
            this.processedObjectUrl shouldBe "${appProperties.aws.cloudFront}/${appProperties.aws.bucket}/$processedObjectKey"
        } else {
            this.processedAt.shouldBeNull()
        }
    }

    private fun stub(info: JobInfo) {
        stubFor(
            post("/callback")
                .willReturn(
                    aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(info.asJson())
                )
        )
    }

    private fun verify(info: JobInfo) {
        verify(
            postRequestedFor(urlPathEqualTo("/callback"))
                .withRequestBody(equalToJson(info.asJson()))
        )
    }
}
