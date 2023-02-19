package com.hometech.mediaprocessor.helper

import io.kotlintest.matchers.shouldBeInRange
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.streams.asSequence

fun randomString(size: Int = 20, lowerCase: Boolean = false): String {
    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val str = java.util.Random().ints(size.toLong(), 0, source.length)
        .asSequence()
        .map(source::get)
        .joinToString("")
    return if (lowerCase) str.lowercase() else str
}

infix fun Instant?.shouldBeIgnoreMillis(expected: Instant?) {
    if (this === null && this === expected) return
    val actualMillis = this!!.truncatedTo(ChronoUnit.SECONDS).toEpochMilli()
    val expectedMillis = expected?.truncatedTo(ChronoUnit.SECONDS)?.toEpochMilli() ?: 0L
    val expectedMillisRange = LongRange(start = expectedMillis - INACCURACY_MILLIS, endInclusive = expectedMillis + INACCURACY_MILLIS)
    return actualMillis shouldBeInRange expectedMillisRange
}

private const val INACCURACY_MILLIS = 3000
