package com.langdroid.core.models.gemini

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerationConfig
import com.google.ai.client.generativeai.type.TextPart
import com.langdroid.core.ChatPrompt
import com.langdroid.core.actionWithResult
import com.langdroid.core.models.GenerativeModelActions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
public actual class GeminiModelActions actual constructor(
    internal actual val model: GeminiModel
) : GenerativeModelActions {
    private val geminiModel: GenerativeModel by lazy {
        GenerativeModel(modelName = model.id,
            apiKey = model.apiKey.orEmpty(),
            generationConfig = model.config?.let {
                GenerationConfig.builder().apply {
                    topK = it.topK
                    topP = it.topP
                    temperature = it.temperature
                    maxOutputTokens = it.maxOutputTokens
                }.build()
            })
    }

    actual override suspend fun generateText(prompts: List<ChatPrompt>): Result<String> =
        actionWithResult {
            val generateContentResponse = geminiModel.generateContent(*createPrompts(prompts))

            generateContentResponse.text.orEmpty()
        }

    actual override suspend fun generateTextStream(prompts: List<ChatPrompt>): Result<Flow<String?>> =
        actionWithResult {
            val generateContentStreamResponse =
                geminiModel.generateContentStream(*createPrompts(prompts))

            generateContentStreamResponse.map { if (it.promptFeedback?.blockReason != null) null else it.text }
        }

    // We need to reduce it because Gemini not work for system messages, they are transformed to Model Role by
    // this library, and then need to be folded because two+ ChatRole.Model messages repeatedly cause a crash
    private fun createPrompts(prompts: List<ChatPrompt>): Array<Content> {
        val reducedPrompts = prompts.fold(mutableListOf<ChatPrompt>()) { acc, current ->
            val currentRole = current.first
            if (acc.isNotEmpty() && acc.last().first.toGeminiRole() == currentRole.toGeminiRole()) {
                // If the last added prompt has the same role as the current, merge their texts
                val mergedText = acc.last().second.orEmpty() + "\n" + current.second.orEmpty()
                // Remove the last prompt and add a new merged prompt
                acc.removeAt(acc.size - 1)
                acc.add(currentRole to mergedText)
            } else {
                // If the current prompt has a different role, add it as is
                acc.add(current)
            }
            acc
        }

        // Convert the reduced list of prompts to Content objects
        return reducedPrompts.map {
            Content(
                role = it.first.toGeminiRole().role, parts = listOf(
                    TextPart(it.second.orEmpty())
                )
            )
        }.toTypedArray()
    }


    actual override suspend fun sanityCheck(): Boolean = try {
        geminiModel.generateContent("a")
        true
    } catch (e: Exception) {
        false
    }

    public actual override suspend fun calculateTokens(prompts: List<ChatPrompt>): Result<Int> =
        actionWithResult {
            geminiModel.countTokens(*createPrompts(prompts)).totalTokens
        }

}
