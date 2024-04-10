package com.langdroid.core

import com.langdroid.core.models.GenerativeModel
import com.langdroid.core.models.GenerativeModelApiActions
import com.langdroid.core.models.request.config.GenerativeConfig
import kotlinx.coroutines.flow.Flow

public class LangDroidModelActions<M : GenerativeModel>(
    private val model: M,
    config: GenerativeConfig<M>?
) : GenerativeModelApiActions {

    init {
        model.config = config
    }

    public override suspend fun generateText(prompt: String): Result<String> =
        generateText(createSimplePrompt(prompt))

    public override suspend fun generateText(prompts: List<ChatPrompt>): Result<String> =
        model.actions.generateText(prompts)

    public override suspend fun generateTextStream(prompt: String): Result<Flow<String?>> =
        generateTextStream(createSimplePrompt(prompt))

    public override suspend fun generateTextStream(prompts: List<ChatPrompt>): Result<Flow<String?>> =
        model.actions.generateTextStream(prompts)

    public override suspend fun sanityCheck(): Boolean =
        model.actions.sanityCheck()

    public override suspend fun calculateTokens(prompts: List<ChatPrompt>): Result<Int> =
        model.actions.calculateTokens(prompts)

    private fun createSimplePrompt(prompt: String): List<ChatPrompt> =
        listOf(ChatRole.User to prompt)
}
