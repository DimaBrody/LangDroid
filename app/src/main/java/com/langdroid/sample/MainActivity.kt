package com.langdroid.sample

//import com.langdroid.core.ChatRole
//import com.langdroid.core.LangDroidModel
//import com.langdroid.core.models.gemini.GeminiModel
//import com.langdroid.core.models.openai.OpenAiModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.langdroid.BuildConfig
import com.langdroid.core.LangDroidModel
import com.langdroid.core.models.openai.OpenAiModel
import com.langdroid.core.models.request.config.GenerativeConfig
import com.langdroid.sample.data.WikipediaApi
import com.langdroid.sample.data.WikipediaRepository
import com.langdroid.sample.data.WikipediaUiModel
import com.langdroid.sample.ui.MainScreen
import com.langdroid.sample.ui.theme.LangdroidTheme
import com.langdroid.summary.SummaryChain
import com.langdroid.summary.SummaryState
import com.langdroid.summary.extensions.collectUntilFinished
import com.langdroid.summary.liveData
import com.langdroid.summary.prompts.PromptsAndMessage

// IMPORTANT! Use {text} in your prompts for places where prompt has to be pasted during processing
private const val WIKIPEDIA_FINAL_PROMPT = """
Write a very detailed summary of the Wikipedia page, the following text delimited by triple backquotes.
Return your response with bullet points which covers the most important key points of the text, sequentially and coherently.
```{text}```
BULLET POINT SUMMARY:
"""

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val wikipediaApi = WikipediaApi()
        val repository = WikipediaRepository(wikipediaApi)
        val viewModel: MainViewModel by viewModels { Factory(repository) }

        val openAiKey = BuildConfig.OPENAI_API_KEY

        val openAiModel =
            LangDroidModel(
                OpenAiModel.Gpt3_5Plus(openAiKey),
                GenerativeConfig.create {
                    // Set temperature = 0f to minimize literature text and make concise summaries
                    temperature = 0f
                })

        // Default prompts are used if "null" or nothing passed
        val promptsAndMessage = PromptsAndMessage(
            // System message added before all the messages and always noticed by LLM
            systemMessage = "You are the Wikipedia oracle",
            // Prompt for final chunk mapping and overall summarization
            finalPrompt = WIKIPEDIA_FINAL_PROMPT,
        )

        val summaryChain = SummaryChain(
            model = openAiModel,

            // Optional values below
            isStream = true,
            promptsAndMessage = promptsAndMessage
        )


        val onWikipediaFetched: (WikipediaUiModel) -> Unit = { model ->
            val textToSummarize = model.content

            // Good practice to have outputBuilder to use summarized output later

            val outputBuilder = StringBuilder()
            // We launch scope as new job and in default/io coroutine to collect summary chain and don't stop UI
            viewModel.launchScope {
                val summaryChainFlow = summaryChain.invokeAndGetFlow(textToSummarize)

                summaryChainFlow.collectUntilFinished {
                    if (it is SummaryState.Output) outputBuilder.append(it.text)

                    viewModel.updateSummaryState(it, outputBuilder.toString())
                }
            }
        }

        setContent {
            LangdroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        onFetchData = viewModel::fetchData,
                        uiStateFlow = viewModel::uiState::get,
                        onWikipediaFetched = onWikipediaFetched
                    )
                }
            }
        }
    }
}


const val DEFAULT_ERROR_MESSAGE = "Something went wrong"
const val IS_MATERIAL_3_STYLE = true
