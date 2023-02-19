package com.hometech.mediaprocessor.processor.model

import com.hometech.mediaprocessor.configuration.advice.exception.BaseProcessingException
import com.hometech.mediaprocessor.configuration.hostAddress
import com.hometech.mediaprocessor.extension.exposed.NamedEntityClass
import com.hometech.mediaprocessor.extension.exposed.enum
import com.hometech.mediaprocessor.processor.model.ConversionStatus.DONE
import com.hometech.mediaprocessor.processor.model.ConversionStatus.PROCESSING
import com.hometech.mediaprocessor.processor.model.ConversionStatus.QUEUED
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.select

object UploadedVideoTable : UUIDTable(name = "uploaded_videos") {
    val filename = text("filename")
    val objectKey = text("object_key")
    val status = enum<ConversionStatus>("conversion_status")
    val createdAt = timestamp("created_at")
    val startedAt = timestamp("started_at").nullable()
    val processedAt = timestamp("processed_at").nullable()
    val order = long("order").autoIncrement("uploaded_videos_order_seq")
    val workerId = text("worker_id").nullable()
    val processedObjectKey = text("processed_object_key").nullable()
    val processedObjectUrl = text("processed_object_url").nullable()
}

class UploadedVideo(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : NamedEntityClass<UploadedVideo>(UploadedVideoTable, "Uploaded Video") {

        fun pollOne(): UploadedVideo? {
            return UploadedVideoTable
                .select { UploadedVideoTable.status eq QUEUED }
                .orderBy(UploadedVideoTable.order)
                .limit(1)
                .forUpdate()
                .firstOrNull()
                ?.let { UploadedVideo.wrapRow(it) }
                ?.apply { andStart() }
        }
    }

    var filename by UploadedVideoTable.filename
    var objectKey by UploadedVideoTable.objectKey
    var status by UploadedVideoTable.status
    var order by UploadedVideoTable.order

    var createdAt by UploadedVideoTable.createdAt
    var startedAt by UploadedVideoTable.startedAt
    var processedAt by UploadedVideoTable.processedAt
    var processedObjectKey by UploadedVideoTable.processedObjectKey
    var processedObjectUrl by UploadedVideoTable.processedObjectUrl
    var workerId by UploadedVideoTable.workerId

    fun andStart(): UploadedVideo {
        return this.apply {
            this.startedAt = Instant.now()
            this.status = PROCESSING
            this.workerId = hostAddress
        }
    }

    fun endProcessingExceptionally(exception: BaseProcessingException) {
        this.status = exception.status
    }

    fun endProcessing(bucket: String, key: String, cloudFront: String) {
        this.processedObjectUrl = "$cloudFront/$bucket/$key"
        this.processedObjectKey = key
        this.processedAt = Instant.now()
        this.status = DONE
    }
}

enum class ConversionStatus { QUEUED, PROCESSING, DONE, TRANSFER_ERROR, PROCESSING_ERROR }
