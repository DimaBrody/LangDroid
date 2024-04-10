package com.langdroid.summary.chains

import com.langdroid.core.LangDroidModel
import com.langdroid.summary.chains.base.DEFAULT_TEXT_PLACEHOLDER
import com.langdroid.summary.chains.base.TextProcessingChain
import com.langdroid.summary.chains.states.SummaryChainState
import com.langdroid.summary.prompts.PromptTemplate
import com.langdroid.summary.splitters.RecursiveTextSplitter
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


private const val DEFAULT_CHUNK_SIZE = 10000
private const val DEFAULT_OVERLAP_SIZE = 500
private const val DEFAULT_DOCUMENT_SEPARATOR = "\n\n"

internal class MapReduceChain(
    private val langDroidModel: LangDroidModel<*>,
    private val chunkPrompt: PromptTemplate,
    private val finalPrompt: PromptTemplate,
    private val isStream: Boolean
) : TextProcessingChain() {

    override suspend fun invoke(text: String) {
        updateState(SummaryChainState.TextSplitting)
        val textSplitter = RecursiveTextSplitter(
            chunkSize = DEFAULT_CHUNK_SIZE, chunkOverlap = DEFAULT_OVERLAP_SIZE
        )
        val outputChunks = textSplitter.splitText(text, onWarning = ::processWarning)


        //1. Map chunks
        val amountOfChunks = outputChunks.size
        val processedChunks = processAndTrackChunks(outputChunks) { processedChunks ->
            updateState(SummaryChainState.Reduce(processedChunks, amountOfChunks))
        }

        //2. Reduce chunks into final output
        val combinedChunks = processedChunks.joinToString(separator = DEFAULT_DOCUMENT_SEPARATOR)

        val defaultChain = DefaultChain(
            langDroidModel = langDroidModel, prompt = finalPrompt, isStream = isStream
        )

        coroutineScope {
            defaultChain.invokeAndConnect(
                coroutineScope = this,
                chain = this@MapReduceChain,
                text = combinedChunks
            )
        }
    }

    private suspend fun processAndTrackChunks(
        outputChunks: List<String>, onProcessedCountUpdated: (Int) -> Unit
    ): List<String> {
        val deferredChunks = mutableListOf<Deferred<String>>()
        val processedChunks = mutableListOf<String>()
        val mutex = Mutex() // For thread-safe operations on processedChunks
        var completedCount = 0 // Track completed deferred tasks

        coroutineScope {
            for (chunk in outputChunks) {
                val chunkChatPrompts = chunkPrompt.createPrompts(
                    DEFAULT_TEXT_PLACEHOLDER to chunk
                )

                val deferred = async {
                    val result = langDroidModel.generateText(chunkChatPrompts).getOrThrow()
                    mutex.withLock {
                        processedChunks.add(result) // Safely add result to processed list
                        completedCount++ // Increment completed counter
                        onProcessedCountUpdated(completedCount)
                    }
                    result
                }

                deferredChunks.add(deferred)
            }
        }

        return processedChunks // Return the processed chunks directly
    }

}
