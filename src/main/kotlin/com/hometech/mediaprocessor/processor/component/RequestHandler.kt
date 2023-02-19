package com.hometech.mediaprocessor.processor.component

import com.hometech.mediaprocessor.extension.exposed.findOrException
import com.hometech.mediaprocessor.processor.model.ConversionStatus
import com.hometech.mediaprocessor.processor.model.JobInfo
import com.hometech.mediaprocessor.processor.model.ProcessingRequest
import com.hometech.mediaprocessor.processor.model.ProcessingResponse
import com.hometech.mediaprocessor.processor.model.UploadedVideo
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Service

@Service
class RequestHandler {

    fun process(request: ProcessingRequest): ProcessingResponse {
        return transaction {
            UploadedVideo.new {
                filename = request.filename
                objectKey = request.key
                status = ConversionStatus.QUEUED
                createdAt = Instant.now()
            }.id.value
        }.let { ProcessingResponse(id = it) }
    }

    fun getJobInfo(id: UUID): JobInfo {
        return transaction {
            UploadedVideo
                .findOrException(id)
                .let { JobInfo(id = it.id.value, status = it.status, processedObjectKey = it.processedObjectKey) }
        }
    }
}
