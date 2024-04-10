package com.langdroid.sample

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.langdroid.sample.data.WikipediaRepository
import com.langdroid.sample.data.WikipediaUiModel
import com.langdroid.summary.SummaryState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

const val DEFAULT_SUMMARY_TEXT = "Here will be your summary..."

class MainViewModel(
    private val repository: WikipediaRepository,
) : ViewModel() {

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(MainUiState())

    val uiState: StateFlow<MainUiState>
        get() = _uiState

    private var currentSummaryJob: Job? = null

    fun fetchData(articleUrl: String) = defaultCoroutineScope.launch {
        clearTitle()
        updateUiState { it.copy(processingState = ProcessingUiState.Fetching) }

        val output = repository.getWikipediaContent(articleUrl)
        if (output.isSuccess) {
            updateUiState {
                it.copy(
                    textTitle = output.getOrNull()?.title,
                    processingState = ProcessingUiState.Fetched(output.getOrThrow())
                )
            }
        } else {
            clearTitle()
            updateUiState {
                it.copy(processingState = ProcessingUiState.Failed(output.exceptionOrNull()))
            }
        }
    }

    private fun clearTitle() {
        updateUiState { it.copy(textTitle = null) }
    }

    // Not best practice, made for demonstration
    fun launchScope(onProcess: suspend CoroutineScope.() -> Unit) {
        currentSummaryJob?.cancel()
        currentSummaryJob = defaultCoroutineScope.launch {
            onProcess(this)
        }
    }

    fun updateSummaryState(summaryState: SummaryState, outputText: String? = null) {
        val textToPrint = summaryState.toProcessingString(outputText)

        val processingState =
            if (summaryState is SummaryState.Finished) ProcessingUiState.Success(outputText.orEmpty())
            else ProcessingUiState.Summarizing(textToPrint)

        updateUiState { it.copy(processingState = processingState) }
    }

    private inline fun updateUiState(crossinline onUpdate: (MainUiState) -> MainUiState) {
        _uiState.update {
            onUpdate(it)
        }
    }


    private val defaultCoroutineScope: CoroutineScope by lazy {
        val defaultDispatcher = Dispatchers.Default
        viewModelScope.plus(defaultDispatcher + CoroutineExceptionHandler { _, throwable ->
            updateUiState { it.copy(processingState = ProcessingUiState.Failed(throwable)) }
        })
    }
}

@Stable
data class MainUiState(
    val textTitle: String? = null, val textOutput: String? = null,

    val processingState: ProcessingUiState = ProcessingUiState.Idle
)

@Stable
sealed interface ProcessingUiState {
    data object Idle : ProcessingUiState
    data object Fetching : ProcessingUiState
    data class Fetched(val model: WikipediaUiModel) : ProcessingUiState

    data class Summarizing(
        val summaryStateText: String
    ) : ProcessingUiState

    data class Success(val outputText: String) : ProcessingUiState
    data class Failed(val t: Throwable?) : ProcessingUiState

    fun isIdle() = this is Idle || this is Success || this is Failed
}


private fun SummaryState.toProcessingString(outputText: String? = null): String = when (this) {
    is SummaryState.Idle, is SummaryState.Failure -> DEFAULT_SUMMARY_TEXT
    is SummaryState.TextSplitting -> "Splitting text..."
    is SummaryState.Reduce -> "Processing chunks $processedChunks/$allChunks"
    is SummaryState.Summarizing -> "Summarizing text..."
    is SummaryState.Output -> outputText.orEmpty()
    is SummaryState.Finished -> outputText.orEmpty()
}


fun Factory(repository: WikipediaRepository): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return MainViewModel(repository) as T
        }
    }
