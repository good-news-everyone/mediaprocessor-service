package com.hometech.mediaprocessor.processor.model

import java.util.UUID

data class ProcessingRequest(val filename: String, val key: String)

data class ProcessingResponse(val id: UUID)
