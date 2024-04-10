package com.langdroid.core.models.gemini

import com.langdroid.core.ChatPrompt
import com.langdroid.core.models.GenerativeModelActions
import kotlinx.coroutines.flow.Flow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
public expect class GeminiModelActions(model: GeminiModel) : GenerativeModelActions {
    internal val model: GeminiModel

    override suspend fun generateText(prompts: List<ChatPrompt>): Result<String>

    override suspend fun generateTextStream(prompts: List<ChatPrompt>): Result<Flow<String?>>

    override suspend fun sanityCheck(): Boolean

    override suspend fun calculateTokens(prompts: List<ChatPrompt>): Result<Int>
}
