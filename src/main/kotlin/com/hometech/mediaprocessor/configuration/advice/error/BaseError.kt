package com.hometech.mediaprocessor.configuration.advice.error

open class BaseError(val description: String)

class ValidationError(description: String) : BaseError(description)
