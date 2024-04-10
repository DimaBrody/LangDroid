package com.langdroid.core.models

import com.langdroid.core.ChatPrompt
import kotlinx.coroutines.flow.Flow

public interface GenerativeModelActions {
    public suspend fun generateText(prompts: List<ChatPrompt>): Result<String>

    public suspend fun generateTextStream(prompts: List<ChatPrompt>): Result<Flow<String?>>

    public suspend fun sanityCheck(): Boolean

    public suspend fun calculateTokens(prompts: List<ChatPrompt>): Result<Int>

}
