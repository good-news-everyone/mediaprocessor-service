package com.hometech.mediaprocessor.helper

import com.hometech.mediaprocessor.processor.model.ConversionStatus
import com.hometech.mediaprocessor.processor.model.UploadedVideo
import java.time.Instant
import java.util.UUID

fun createUploadedVideo(
    id: UUID = UUID.randomUUID(),
    key: String = randomString(),
    status: ConversionStatus = ConversionStatus.QUEUED,
    processedObjectKey: String? = null
): UploadedVideo {
    return UploadedVideo.new(id) {
        this.filename = randomString()
        this.objectKey = key
        this.createdAt = Instant.now()
        this.processedObjectKey = processedObjectKey
        this.status = status
    }
}
