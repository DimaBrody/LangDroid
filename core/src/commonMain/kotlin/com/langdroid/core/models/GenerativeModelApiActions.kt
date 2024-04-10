package com.langdroid.core.models

import kotlinx.coroutines.flow.Flow

public interface GenerativeModelApiActions : GenerativeModelActions {
    public suspend fun generateText(prompt: String): Result<String>

    public suspend fun generateTextStream(prompt: String): Result<Flow<String?>>

}
