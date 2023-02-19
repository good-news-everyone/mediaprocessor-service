package com.hometech.mediaprocessor

import com.hometech.mediaprocessor.extension.exposed.findOrException
import com.hometech.mediaprocessor.helper.createUploadedVideo
import com.hometech.mediaprocessor.helper.shouldBeIgnoreMillis
import com.hometech.mediaprocessor.processor.model.ConversionStatus
import com.hometech.mediaprocessor.processor.model.UploadedVideo
import com.hometech.mediaprocessor.processor.model.UploadedVideoTable
import io.kotlintest.matchers.types.shouldBeNull
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import java.util.UUID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QueuePollingTests : AbstractIntegrationTest() {

    @AfterEach
    fun cleanUp() {
        transaction { UploadedVideoTable.deleteAll() }
    }

    @BeforeEach
    fun restartSequence() {
        transaction { exec("alter sequence uploaded_videos_order_seq restart with 1") }
    }

    @Test
    fun `should poll from queue with status = QUEUED`() {
        val queue = transaction { createUploadedVideo() }
        transaction { UploadedVideo.pollOne() compareWith queue }
    }

    @Test
    fun `should poll entry from queue with minimal order`() {
        val id = UUID.randomUUID()
        transaction {
            createUploadedVideo(id = id)
            createUploadedVideo()
        }
        transaction {
            UploadedVideo.pollOne().also {
                it compareWith UploadedVideo.findOrException(id)
                it!!.order shouldBe 1
            }
        }
    }

    @Test
    fun `should not with status != QUEUED`() {
        transaction {
            createUploadedVideo(status = ConversionStatus.PROCESSING)
        }
        transaction {
            UploadedVideo.pollOne().shouldBeNull()
        }
    }

    @Test
    fun `should not poll if queue is empty`() {
        transaction {
            UploadedVideo.pollOne().shouldBeNull()
        }
    }

    private infix fun UploadedVideo?.compareWith(expected: UploadedVideo) {
        shouldNotBeNull()
        id.value shouldBe expected.id.value
        filename shouldBe expected.filename
        createdAt shouldBeIgnoreMillis expected.createdAt
        objectKey shouldBe expected.objectKey
        status shouldBe ConversionStatus.PROCESSING
    }
}
