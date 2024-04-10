package com.langdroid.core.models.openai

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.core.RequestOptions
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.langdroid.core.ChatPrompt
import com.langdroid.core.actionWithResult
import com.langdroid.core.models.GenerativeModelActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Duration.Companion.seconds

public class OpenAiModelActions(
    private val model: OpenAiModel
) : GenerativeModelActions {

    override suspend fun generateText(prompts: List<ChatPrompt>): Result<String> =
        actionWithResult {
            openAi.chatCompletion(
                request = createChatRequest(prompts),
                requestOptions = RequestOptions(
                    timeout = Timeout(socket = 180.seconds)
                )
            ).choices.first().message.content.orEmpty()
        }

    override suspend fun generateTextStream(prompts: List<ChatPrompt>): Result<Flow<String?>> =
        actionWithResult {
            openAi.chatCompletions(
                request = createChatRequest(prompts)
            ).map {
                it.choices.first().let { chunk ->
                    if (chunk.finishReason != null) null
                    else chunk.delta.content
                }
            }
        }

    private fun createChatRequest(prompts: List<ChatPrompt>): ChatCompletionRequest {
        return ChatCompletionRequest(model = modelId,
            temperature = temperature,
            topP = topP,
            maxTokens = maxTokens,
            messages = prompts.map { it.toOpenAIPrompt() })
    }

    override suspend fun sanityCheck(): Boolean = try {
        openAi.models()
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun calculateTokens(prompts: List<ChatPrompt>): Result<Int> =
        actionWithResult {
            // Android-specific implementation
            tokensCount(prompts, modelId.id)
        }

    private val openAi: OpenAI by lazy {
        OpenAI(model.apiKey.orEmpty())
    }

    private val modelId: ModelId by lazy {
        ModelId(model.id)
    }

    private val temperature: Double? by lazy {
        model.config?.temperature?.toDouble()
    }

    private val topP: Double? by lazy {
        model.config?.topP?.toDouble()
    }

    private val maxTokens: Int? by lazy {
        model.config?.maxOutputTokens
    }

}
