package com.langdroid.summary

import com.langdroid.core.ChatPrompt
import com.langdroid.core.LangDroidModel
import com.langdroid.core.models.openai.OpenAiModel
import com.langdroid.core.models.request.config.GenerativeConfig
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList

internal object Mocks {
    private const val chunks = 3
    private const val tokensTest = 1000

    const val text1 = "Hello, can I help you?"
    const val text2 = "Hello, weather is good today"

    const val MOCK_TEXT = "Wow!"

    suspend fun createModelNormalWithList(
        text: String = text1,
        isStream: Boolean = true,
        isTestCompleteness: Boolean = false,
        isReduceText: Boolean = false
    ): Pair<LangDroidModel<*>, List<SummaryState>> {
        val model: LangDroidModel<*> = mockk()
        val openAiModel: OpenAiModel.Gpt3_5 = mockk()
        val config: GenerativeConfig<*> = mockk()

        every { openAiModel.tokenLimit } returns tokensTest * (if (isReduceText) 1 else 3)
        every { openAiModel.outputTokenLimit } returns tokensTest
        every { config.maxOutputTokens } returns tokensTest
        every { model.model } returns openAiModel
        every { model.config } returns config

        val flowGenerator = stringToStreamFlowGenerator(text, chunks)
        coEvery { model.generateText(any<String>()) } coAnswers {
            delay(DEFAULT_DELAY)
            Result.success(text)
        }
        coEvery { model.generateTextStream(any<String>()) } returns Result.success(
            flowGenerator(true)
        )
        coEvery { model.generateText(any<List<ChatPrompt>>()) } coAnswers {
            delay(DEFAULT_DELAY)
            Result.success(text)
        }
        coEvery { model.generateTextStream(any<List<ChatPrompt>>()) } returns Result.success(
            flowGenerator(true)
        )
        coEvery { model.sanityCheck() } returns true
        coEvery { model.calculateTokens(any<List<ChatPrompt>>()) } returns Result.success(42)

        return model to generateListForText(
            text,
            isStream,
            isReduceText,
            isTestCompleteness,
            flowGenerator
        )
    }

    private suspend fun generateListForText(
        text: String,
        isStream: Boolean,
        isReduceText: Boolean,
        isTestCompleteness: Boolean,
        flowGenerator: (Boolean) -> Flow<String>
    ): List<SummaryState> {
        val outputItems =
            if (isStream) flowGenerator(false).take(count = chunks).toList() else listOf(text)

        return defaultStatesBefore(isStream, isReduceText) + outputItems
            .map { SummaryState.Output(it) } + defaultStatesAfter(isTestCompleteness)
    }

    private fun defaultStatesBefore(isStream: Boolean, isReduceText: Boolean): List<SummaryState> =
        if (isReduceText) {
            listOf(SummaryState.TextSplitting, SummaryState.Reduce(1, 1))
        } else {
            listOf()
        } + if (!isStream) {
            listOf(SummaryState.Summarizing)
        } else {
            listOf()
        }

    private fun defaultStatesAfter(isTestCompleteness: Boolean): List<SummaryState> =
        if (isTestCompleteness) listOf() else listOf(SummaryState.Finished)

}
