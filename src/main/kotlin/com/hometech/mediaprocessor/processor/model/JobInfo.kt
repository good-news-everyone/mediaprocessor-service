package com.hometech.mediaprocessor.processor.model

import java.util.UUID

data class JobInfo(
    val id: UUID,
    val status: ConversionStatus = ConversionStatus.DONE,
    val processedObjectKey: String? = null
)
