package com.langdroid.summary.chains.base

import com.langdroid.summary.chains.states.ChainState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

public const val DEFAULT_TEXT_PLACEHOLDER: String = "text"

internal interface Chain<S> {

    suspend operator fun invoke(text: String)

    suspend fun invokeAndConnect(
        coroutineScope: CoroutineScope,
        chain: Chain<*>,
        text: String,
        onMap: ((S) -> ChainState?)? = null
    ) {
    }

    val processingState: Flow<S>
}
