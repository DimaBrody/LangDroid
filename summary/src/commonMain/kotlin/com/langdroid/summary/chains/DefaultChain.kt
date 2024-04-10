package com.langdroid.summary.chains

import com.langdroid.core.LangDroidModel
import com.langdroid.summary.chains.base.DEFAULT_TEXT_PLACEHOLDER
import com.langdroid.summary.chains.base.TextProcessingChain
import com.langdroid.summary.chains.states.SummaryChainState
import com.langdroid.summary.exceptions.createException
import com.langdroid.summary.prompts.PromptTemplate
import kotlinx.coroutines.flow.takeWhile

internal class DefaultChain(
    private val langDroidModel: LangDroidModel<*>,
    private val prompt: PromptTemplate,
    private val isStream: Boolean = false
) : TextProcessingChain() {

    override suspend fun invoke(text: String) {
        val prompts = prompt.createPrompts(
            DEFAULT_TEXT_PLACEHOLDER to text
        )

        if (isStream) {
            val generateStreamResult = langDroidModel.generateTextStream(prompts)
            if (generateStreamResult.isSuccess)
                generateStreamResult.getOrThrow().takeWhile { output ->
                    // When output is null - it's finished its work
                    output != null // Continue collecting until text is null
                }.collect { output ->
                    if (output != null) {
                        processText(output)
                    }
                }
            else generateStreamResult.createException().let(::processFailure)
        } else {
            updateState(SummaryChainState.Summarizing)

            val textResult = langDroidModel.generateText(prompts)
            if (textResult.isSuccess) {
                processText(textResult.getOrThrow())
            } else textResult.createException().let(::processFailure)
        }

        updateState(SummaryChainState.Finished)
    }

}
