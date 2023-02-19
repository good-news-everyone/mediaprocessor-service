package com.hometech.mediaprocessor.processor.model

import java.util.UUID

data class UploadedVideoInfo(
    val id: UUID,
    val key: String
)

fun UploadedVideo.toEntryInfo() = UploadedVideoInfo(id = id.value, key = objectKey)
