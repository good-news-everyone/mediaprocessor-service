package com.hometech.mediaprocessor.configuration.advice

import com.amazonaws.services.cloudsearchv2.model.BaseException
import com.hometech.mediaprocessor.configuration.advice.error.BaseError
import com.hometech.mediaprocessor.configuration.advice.error.ValidationError
import com.hometech.mediaprocessor.configuration.advice.exception.ResourceNotFoundException
import java.sql.SQLException
import mu.KotlinLogging
import org.springframework.beans.TypeMismatchException
import org.springframework.boot.context.properties.bind.BindException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.multipart.support.MissingServletRequestPartException

private val log = KotlinLogging.logger { }

@ControllerAdvice
class ControllerAdvice {

    @ExceptionHandler(SQLException::class, Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleDataWriteException(exception: Exception): ResponseEntity<out BaseError> {
        val error = if (exception is SQLException)
            BaseError(description = "Error while interaction with database")
        else
            BaseError(description = "Internal server error")
        log.error(exception) { exception.message }
        return ResponseEntity(error, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(
        value = [
            BindException::class,
            HttpMessageNotReadableException::class,
            MethodArgumentNotValidException::class,
            MissingServletRequestParameterException::class,
            MissingServletRequestPartException::class,
            TypeMismatchException::class
        ]
    )
    fun handleCommonBadRequestException(exception: Exception): ResponseEntity<out BaseError> {
        val error = when (exception) {
            is BaseException -> ValidationError(description = exception.message ?: "")
            is BindException -> ValidationError(description = "Invalid request parameter: ${exception.property}")
            is HttpMessageNotReadableException -> ValidationError(description = "Error while reading request body")
            is MethodArgumentNotValidException -> ValidationError(description = "Invalid request parameter: ${exception.parameter.parameterName}")
            is MissingServletRequestParameterException -> ValidationError(description = "Required parameter not present: ${exception.parameterName}")
            is MissingServletRequestPartException -> ValidationError(description = "Required parameter not present: ${exception.requestPartName}")
            is TypeMismatchException -> ValidationError(description = "Can't recognize type of parameter: '${exception.propertyName}'. Expected type name: ${exception.requiredType?.simpleName}")
            else -> ValidationError(description = exception.message ?: "Invalid request parameter(s)")
        }
        log.warn(exception) { exception.message }
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(value = [NoSuchElementException::class, ResourceNotFoundException::class])
    fun handleCommonNotFoundException(exception: RuntimeException): ResponseEntity<out BaseError> {
        log.warn(exception) { exception.message }
        return ResponseEntity(
            BaseError(description = exception.message ?: "Element not found"),
            HttpStatus.NOT_FOUND
        )
    }
}
