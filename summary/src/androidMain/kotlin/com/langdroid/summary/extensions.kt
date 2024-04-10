package com.langdroid.summary

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData

public fun SummaryChain<*>.liveData(): LiveData<SummaryState> =
    processingState.asLiveData()
