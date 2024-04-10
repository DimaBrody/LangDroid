package com.langdroid.core.models.gemini

import com.langdroid.core.ChatPrompt
import com.langdroid.core.models.GenerativeModelActions
import kotlinx.coroutines.flow.Flow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
public actual class GeminiModelActions actual constructor(
    internal actual val model: GeminiModel
) : GenerativeModelActions {
    private val modelName = "Gemini"
    private val notImplementedException = NotImplementedError(modelName)

    actual override suspend fun generateText(prompts: List<ChatPrompt>): Result<String> {
        throw notImplementedException
    }

    actual override suspend fun generateTextStream(prompts: List<ChatPrompt>): Result<Flow<String?>> {
        throw notImplementedException
    }

    actual override suspend fun sanityCheck(): Boolean {
        throw notImplementedException
    }

    actual override suspend fun calculateTokens(prompts: List<ChatPrompt>): Result<Int> {
        throw notImplementedException
    }
}
