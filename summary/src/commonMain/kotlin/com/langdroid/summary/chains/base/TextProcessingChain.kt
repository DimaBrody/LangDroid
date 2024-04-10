package com.langdroid.summary.chains.base

import com.langdroid.summary.chains.states.SummaryChainState

internal abstract class TextProcessingChain : BaseChain<SummaryChainState>() {

    protected fun processText(text: String) =
        processingState.tryEmit(SummaryChainState.Output(text))

    protected fun processFailure(t: Throwable) =
        processingState.tryEmit(SummaryChainState.Failure(t))

    protected fun processWarning(warning: String) =
        processingState.tryEmit(SummaryChainState.Warning(warning))
}
