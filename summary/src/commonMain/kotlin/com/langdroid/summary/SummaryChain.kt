package com.langdroid.summary

import com.langdroid.core.LangDroidModel
import com.langdroid.core.models.GenerativeModel
import com.langdroid.core.models.request.config.GenerativeConfig
import com.langdroid.summary.chains.DefaultChain
import com.langdroid.summary.chains.MapReduceChain
import com.langdroid.summary.chains.base.Chain
import com.langdroid.summary.chains.base.DEFAULT_TEXT_PLACEHOLDER
import com.langdroid.summary.chains.states.SummaryChainState
import com.langdroid.summary.extensions.collectUntilFinished
import com.langdroid.summary.prompts.CompletedPromptsAndMessages
import com.langdroid.summary.prompts.DEFAULT_SYSTEM_MESSAGE
import com.langdroid.summary.prompts.PromptTemplate
import com.langdroid.summary.prompts.PromptsAndMessage
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

private const val MAX_DEFAULT_OUTPUT_TOKENS = 512

//In case some calculations are a bit wrong
private const val TOKENS_THRESHOLD = 10

private const val TAG = "SummaryChain"

private const val DEFAULT_CHUNK_PROMPT = """
Write a concise summary of the following:
"{text}"
CONCISE SUMMARY:
"""

private const val DEFAULT_FINAL_PROMPT = """
Write a concise summary of the following text delimited by triple backquotes.
Return your response in bullet points which covers the key points of the text.
```{text}```
BULLET POINT SUMMARY:
"""

public class SummaryChain<M : GenerativeModel> : Chain<SummaryState> {

    private val langDroidModel: LangDroidModel<M>
    private val isStream: Boolean

    private lateinit var coroutineScope: CoroutineScope
    private lateinit var promptsAndMessage: CompletedPromptsAndMessages

    private var summaryJob: Job? = null

    public constructor(
        model: LangDroidModel<M>,
        isStream: Boolean = true,
        coroutineScope: CoroutineScope? = null,
        promptsAndMessage: PromptsAndMessage? = null
    ) {
        this.langDroidModel = model
        this.isStream = isStream
        setupConstructor(coroutineScope, promptsAndMessage)
    }

    public constructor(
        model: M, config: GenerativeConfig<M>? = null,
        isStream: Boolean = true,
        coroutineScope: CoroutineScope? = null,
        promptsAndMessage: PromptsAndMessage? = null,
    ) {
        this.langDroidModel = LangDroidModel(model, config)
        this.isStream = isStream
        setupConstructor(coroutineScope, promptsAndMessage)
    }

    private fun setupConstructor(
        coroutineScope: CoroutineScope?, promptsAndMessage: PromptsAndMessage?
    ) {
        setupScope(coroutineScope)
        createPromptTemplates(promptsAndMessage)
    }

    private fun createPromptTemplates(
        promptsAndMessage: PromptsAndMessage?
    ) {
        this.promptsAndMessage = CompletedPromptsAndMessages(
            chunkPrompt = promptsAndMessage?.chunkPrompt ?: DEFAULT_CHUNK_PROMPT,
            finalPrompt = promptsAndMessage?.finalPrompt ?: DEFAULT_FINAL_PROMPT,
            systemMessage = promptsAndMessage?.systemMessage ?: DEFAULT_SYSTEM_MESSAGE
        )
    }

    private fun setupScope(coroutineScope: CoroutineScope?) {
        this.coroutineScope = CoroutineScope(
            (coroutineScope?.coroutineContext ?: Dispatchers.IO) + coroutineExceptionHandler
        )
    }

    public override suspend fun invoke(text: String): Unit = summaryScope {
        val outputTokenLimit = langDroidModel.model.outputTokenLimit ?: MAX_DEFAULT_OUTPUT_TOKENS

        var maxOutputTokens = langDroidModel.config?.maxOutputTokens ?: outputTokenLimit

        if (maxOutputTokens > outputTokenLimit) {
            warning("Your provided output tokens is too high for this model.\nSet default for ${langDroidModel.model.id}: $outputTokenLimit ")
            maxOutputTokens = maxOutputTokens.coerceAtMost(outputTokenLimit)
        }

        val modelMaxTokens = langDroidModel.model.tokenLimit

        val finalPromptTemplate = PromptTemplate(
            promptsAndMessage.finalPrompt, promptsAndMessage.systemMessage
        )

        val promptsTokensCount = langDroidModel.calculateTokens(
            finalPromptTemplate.createPrompts(
                DEFAULT_TEXT_PLACEHOLDER to text
            )
        ).getOrThrow()

        // If prompt input + expected output < max model context length - make summary from full prompt at once
        if (promptsTokensCount + maxOutputTokens < modelMaxTokens - TOKENS_THRESHOLD) {
            val defaultChain = DefaultChain(langDroidModel, finalPromptTemplate, isStream)

            defaultChain.invokeAndConnect(
                coroutineScope = this,
                chain = this@SummaryChain,
                text = text,
                onMap = ::mapToSummaryState
            )
        }
        //else - split text
        else {
            val chunkPromptTemplate = PromptTemplate(
                promptsAndMessage.chunkPrompt, promptsAndMessage.systemMessage
            )

            val mapReduceChain = MapReduceChain(
                langDroidModel, chunkPromptTemplate, finalPromptTemplate, isStream
            )

            mapReduceChain.invokeAndConnect(
                coroutineScope = this,
                chain = this@SummaryChain,
                text = text,
                onMap = ::mapToSummaryState
            )
        }

        processingState.tryEmit(SummaryState.Success)
    }

    public fun invokeAndGetFlow(text: String): SharedFlow<SummaryState> {
        workingScope { invoke(text) }
        return processingState
    }

    public fun invokeAndObserve(text: String, onCallback: (state: SummaryState) -> Unit) {
        workingScope {
            invoke(text)
            processingState.collectUntilFinished(onCallback)
        }
    }

    public fun cancel() {
        summaryJob?.cancel() // Cancel the job
    }

    private fun mapToSummaryState(state: SummaryChainState): SummaryState? {
        return when (state) {
            is SummaryChainState.Reduce -> SummaryState.Reduce(
                state.processedChunks, state.allChunks
            )

            is SummaryChainState.Connecting -> SummaryState.Idle
            is SummaryChainState.TextSplitting -> SummaryState.TextSplitting
            is SummaryChainState.Summarizing -> SummaryState.Summarizing
            is SummaryChainState.Output -> SummaryState.Output(state.text)
            is SummaryChainState.Failure -> SummaryState.Failure(state.t)

            else -> {
                if (state is SummaryChainState.Warning)
                // Add Logger
                    warning(state.warning)
                null
            }
        }
    }

    private fun warning(message: String) {
//        Log.w(TAG, message)
    }

    private fun error(throwable: Throwable) {
        processingState.tryEmit(SummaryState.Failure(throwable))
    }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        error(throwable)
    }

    private inline fun summaryScope(crossinline block: suspend CoroutineScope.() -> Unit) {
        cancel()
        summaryJob = coroutineScope.launch {
            block()
        }
    }

    private inline fun workingScope(crossinline block: suspend CoroutineScope.() -> Unit) {
        coroutineScope.launch {
            block()
        }
    }

    override val processingState: MutableSharedFlow<SummaryState> =
        MutableSharedFlow(extraBufferCapacity = 1000)
}
