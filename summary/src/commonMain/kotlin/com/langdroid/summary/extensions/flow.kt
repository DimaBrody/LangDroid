package com.langdroid.summary.extensions

import com.langdroid.summary.SummaryState
import com.langdroid.summary.isFinished
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

public suspend fun Flow<SummaryState>.collectUntilFinished(onEach: (SummaryState) -> Unit): Unit =
    takeUntilFinished(onEach).collect()

public fun Flow<SummaryState>.takeUntilFinished(onEach: (SummaryState) -> Unit): Flow<SummaryState> =
    this.onEach(onEach).takeWhile { !it.isFinished() }
