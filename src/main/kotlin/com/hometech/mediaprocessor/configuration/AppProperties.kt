package com.hometech.mediaprocessor.configuration

import com.amazonaws.regions.Regions
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
class AppProperties(
    val baseUrl: String,
    val aws: AmazonS3Properties,
    val callback: Callback,
    tempFilesPath: String
) {
    val tempFilesPath: String by lazy {
        if (hostOs == com.hometech.mediaprocessor.configuration.Os.WINDOWS) tempFilesPath.replace("/", "\\")
        else tempFilesPath.replace("\\", "/")
    }

    class AmazonS3Properties(
        region: String,
        val accessKeyId: String,
        val secretAccessKey: String,
        val bucket: String,
        val cloudFront: String
    ) {
        val region: Regions = Regions.fromName(region)
    }

    class Callback(val url: String)
}
