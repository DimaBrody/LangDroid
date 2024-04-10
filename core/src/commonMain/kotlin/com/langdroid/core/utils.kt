package com.langdroid.core

internal inline fun <T> actionWithResult(
    noinline specificExceptionHandler: ((Exception) -> Result<T>)? = null,
    block: () -> T
): Result<T> {
    return try {
        Result.success(block())
    } catch (e: Exception) {
        // Check if a specific exception handler is provided and use it
        specificExceptionHandler?.invoke(e) ?: Result.failure(e)
    }
}
