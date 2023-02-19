package com.hometech.mediaprocessor.configuration.advice.exception

import com.hometech.mediaprocessor.processor.model.ConversionStatus
import com.hometech.mediaprocessor.processor.model.ConversionStatus.PROCESSING_ERROR
import com.hometech.mediaprocessor.processor.model.ConversionStatus.TRANSFER_ERROR

abstract class BaseException(msg: String = "Unexpected error", cause: Throwable? = null) : RuntimeException(msg, cause) {
    override val message: String
        get() = super.message ?: ""
}

class ResourceNotFoundException(msg: String) : BaseException(msg)
open class BaseProcessingException(cause: Throwable) : BaseException(cause = cause) {

    open val status: ConversionStatus = PROCESSING_ERROR
}
class TransferException(cause: Throwable) : BaseProcessingException(cause = cause) {
    override val status = TRANSFER_ERROR
}
