package com.hometech.mediaprocessor

import com.hometech.mediaprocessor.configuration.advice.error.BaseError
import com.hometech.mediaprocessor.configuration.advice.error.ValidationError
import com.hometech.mediaprocessor.helper.createUploadedVideo
import com.hometech.mediaprocessor.processor.model.ConversionStatus.DONE
import com.hometech.mediaprocessor.processor.model.JobInfo
import com.hometech.mediaprocessor.processor.model.ProcessingRequest
import com.hometech.mediaprocessor.processor.model.ProcessingResponse
import com.hometech.mediaprocessor.processor.model.UploadedVideo
import com.hometech.mediaprocessor.processor.model.UploadedVideoTable
import io.kotlintest.shouldBe
import java.util.UUID
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

class ApiControllerTests : AbstractIntegrationTest() {

    @AfterEach
    fun cleanUp() {
        transaction { UploadedVideoTable.deleteAll() }
    }

    @Test
    fun `should accept request`() {
        val request = ProcessingRequest(filename = "123.ext", key = "key")
        val response = mockMvc.post("/files/process") {
            contentType = MediaType.APPLICATION_JSON
            content = request.asJson()
        }
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andReturn()
            .asObject<ProcessingResponse>()
        transaction {
            UploadedVideo.all().toList()
                .also { it.size shouldBe 1 }
                .first()
                .also {
                    it.id.value shouldBe response.id
                    it.filename shouldBe request.filename
                    it.objectKey shouldBe request.key
                }
        }
    }

    @Test
    fun `should not accept request if body is invalid`() {
        mockMvc.post("/files/process") {
            contentType = MediaType.APPLICATION_JSON
            content = "{}".asJson()
        }
            .andDo { print() }
            .andExpect { status { isBadRequest() } }
            .andReturn()
            .asObject<ValidationError>()
            .also { it.description shouldBe "Error while reading request body" }
    }

    @Test
    fun `should get job info`() {
        val jobInfo = transaction {
            val entry = createUploadedVideo(status = DONE, processedObjectKey = "123")
            JobInfo(id = entry.id.value, status = entry.status, processedObjectKey = entry.processedObjectKey)
        }
        mockMvc.get("/jobs/${jobInfo.id}")
            .andDo { print() }
            .andExpect { status { isOk() } }
            .andReturn()
            .asObject<JobInfo>() shouldBe jobInfo
    }

    @Test
    fun `should not get job info if job does not exists`() {
        val id = UUID.randomUUID()
        mockMvc.get("/jobs/$id")
            .andDo { print() }
            .andExpect { status { isNotFound() } }
            .andReturn()
            .asObject<BaseError>()
            .also {
                it.description shouldBe "For element 'Uploaded Video' no entries found for ID '$id'"
            }
    }
}
