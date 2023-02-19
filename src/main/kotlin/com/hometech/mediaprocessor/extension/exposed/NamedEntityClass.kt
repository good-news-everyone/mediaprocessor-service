package com.hometech.mediaprocessor.extension.exposed

import com.hometech.mediaprocessor.configuration.advice.exception.ResourceNotFoundException
import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.IdTable

abstract class NamedEntityClass<out E : UUIDEntity>(
    table: IdTable<UUID>,
    val name: String,
    entityType: Class<E>? = null,
) : UUIDEntityClass<E>(table, entityType)

@Suppress("DEPRECATED_SMARTCAST")
fun <E : UUIDEntity> NamedEntityClass<E>.findOrException(id: UUID): E {
    return this.find { table.id eq id }.firstOrNull()
        ?: throw ResourceNotFoundException("For element '$name' no entries found for ID '$id'")
}
