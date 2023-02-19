package com.hometech.mediaprocessor.extension.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

inline fun <reified T : Enum<T>> Table.enum(name: String): Column<T> {
    return enumerationByName(name, MAX_ENUM_LENGTH, T::class)
}

const val MAX_ENUM_LENGTH = 50
