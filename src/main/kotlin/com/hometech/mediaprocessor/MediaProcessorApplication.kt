package com.hometech.mediaprocessor

import com.hometech.mediaprocessor.configuration.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.hometech.mediaprocessor"])
@ConfigurationPropertiesScan(
    basePackages = ["com.hometech.mediaprocessor.configuration"],
    basePackageClasses = [AppProperties::class]
)
class MediaProcessorApplication

fun main(args: Array<String>) {
    runApplication<MediaProcessorApplication>(*args)
}
