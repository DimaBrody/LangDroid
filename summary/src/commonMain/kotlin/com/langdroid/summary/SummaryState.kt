package com.langdroid.summary

import com.langdroid.summary.chains.states.ChainState

public sealed interface SummaryState : ChainState {
    public data object Idle : SummaryState

    public data object TextSplitting : SummaryState
    public data class Reduce(val processedChunks: Int, val allChunks: Int) : SummaryState

    // Used when isStream = false
    public data object Summarizing : SummaryState
    public data class Output(val text: String) : SummaryState
    public data object Success : SummaryState
    public data class Failure(val t: Throwable) : SummaryState
}

public fun SummaryState.isFinished(): Boolean =
    this is SummaryState.Success || this is SummaryState.Failure
