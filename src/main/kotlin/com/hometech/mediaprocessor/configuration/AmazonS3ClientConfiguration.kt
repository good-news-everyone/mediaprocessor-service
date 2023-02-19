package com.hometech.mediaprocessor.configuration

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.transfer.TransferManager
import com.amazonaws.services.s3.transfer.TransferManagerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AmazonS3ClientConfiguration {

    @Bean
    fun amazonS3client(appProperties: AppProperties): AmazonS3 {
        val credentials = BasicAWSCredentials(
            appProperties.aws.accessKeyId,
            appProperties.aws.secretAccessKey
        )
        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .withRegion(appProperties.aws.region)
            .build()
    }

    @Bean
    fun transferManager(s3Client: AmazonS3): TransferManager {
        return TransferManagerBuilder.standard().withS3Client(s3Client).build()
    }
}
