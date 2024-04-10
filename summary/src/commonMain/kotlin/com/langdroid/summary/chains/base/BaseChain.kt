package com.langdroid.summary.chains.base

import com.langdroid.summary.chains.states.ChainState
import com.langdroid.summary.chains.states.SummaryChainState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal abstract class BaseChain<S : ChainState> : Chain<S> {
    override val processingState: MutableSharedFlow<S> =
        // Replay for cases when emit happened earlier than job creation completed
        MutableSharedFlow(replay = 2, extraBufferCapacity = 1000)

    override suspend fun invokeAndConnect(
        coroutineScope: CoroutineScope, chain: Chain<*>,
        text: String, onMap: ((S) -> ChainState?)?
    ) {
        val job = coroutineScope.launch {
            processingState.onEach { state ->
                val mappedState = onMap?.invoke(state)
                if (onMap != null && mappedState == null) {
                    // Ignore this update
                } else {
                    (chain.processingState as MutableSharedFlow).tryEmit(mappedState ?: state)
                }

                if (state is SummaryChainState.Finished || state is SummaryChainState.Failure) {
                    cancel()
                }
            }.collect()
        }
        invoke(text)

        job.join()
    }

    protected fun updateState(state: S) {
        processingState.tryEmit(state)
    }
}
