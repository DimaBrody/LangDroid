package com.langdroid.summary.chains.states

internal interface SummaryChainState : ChainState {
    data object Connecting : SummaryChainState

    data object TextSplitting : SummaryChainState
    data class Reduce(val processedChunks: Int, val allChunks: Int) : SummaryChainState

    data object Summarizing: SummaryChainState
    data class Output(val text: String) : SummaryChainState

    data class Warning(val warning: String) : SummaryChainState
    data class Failure(val t: Throwable) : SummaryChainState
    data object Finished: SummaryChainState
}
