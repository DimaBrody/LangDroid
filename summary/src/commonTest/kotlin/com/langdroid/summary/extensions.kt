package com.langdroid.summary

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val DEFAULT_DELAY = 250L

internal fun stringToStreamFlowGenerator(
    text: String,
    chunkSize: Int
): (isDelay: Boolean) -> Flow<String> = { isDelay ->
    flow {
        text.chunkedSequence(chunkSize).forEachIndexed { i, chunk ->
            emit(chunk)
            if (isDelay && i < chunkSize) delay(DEFAULT_DELAY)
        }
    }
}

private fun String.chunkedSequence(pieces: Int): Sequence<String> = sequence {
    val chunkSize = length / pieces
    var remainder = length % pieces
    var start = 0

    for (i in 1..pieces) {
        val end = start + chunkSize + if (remainder > 0) 1 else 0
        if (remainder > 0) remainder-- // Decrement remainder until it's distributed

        yield(substring(start, end))

        start = end
    }
}
