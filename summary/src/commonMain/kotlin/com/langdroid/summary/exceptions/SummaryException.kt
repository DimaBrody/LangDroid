package com.langdroid.summary.exceptions

public class SummaryException(override val message: String? = "Something went wrong during summary") :
    Exception(message)

public fun <T> Result<T>.createException(): Throwable = exceptionOrNull() ?: SummaryException()
