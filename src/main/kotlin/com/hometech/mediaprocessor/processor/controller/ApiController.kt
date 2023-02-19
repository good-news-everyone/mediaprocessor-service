package com.hometech.mediaprocessor.processor.controller

import com.hometech.mediaprocessor.processor.component.RequestHandler
import com.hometech.mediaprocessor.processor.model.JobInfo
import com.hometech.mediaprocessor.processor.model.ProcessingRequest
import com.hometech.mediaprocessor.processor.model.ProcessingResponse
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiController(private val requestHandler: RequestHandler) {

    @PostMapping("/files/process")
    fun process(@RequestBody request: ProcessingRequest): ProcessingResponse {
        return requestHandler.process(request)
    }

    @GetMapping("/jobs/{id}")
    fun getJobInfo(@PathVariable id: UUID): JobInfo {
        return requestHandler.getJobInfo(id)
    }
}
