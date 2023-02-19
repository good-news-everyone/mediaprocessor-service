package com.hometech.mediaprocessor.processor.component

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.hometech.mediaprocessor.configuration.AppProperties
import com.hometech.mediaprocessor.processor.model.ConversionStatus
import com.hometech.mediaprocessor.processor.model.JobInfo
import com.hometech.mediaprocessor.processor.model.UploadedVideoInfo
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange

@Component
class CallBackSender(
    private val restTemplate: RestTemplate,
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper
) {

    fun sendSuccessCallback(info: UploadedVideoInfo, key: String) {
        JobInfo(id = info.id, processedObjectKey = key).also { sendCallback(it) }
    }

    fun sendFailedCallback(info: UploadedVideoInfo, state: ConversionStatus) {
        JobInfo(id = info.id, status = state).also { sendCallback(it) }
    }

    fun sendCallback(info: JobInfo) {
        restTemplate.exchange<Unit>(
            url = appProperties.callback.url,
            method = HttpMethod.POST,
            requestEntity = HttpEntity(objectMapper.convertValue<ObjectNode>(info))
        )
    }
}
