package com.langdroid.sample.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.langdroid.sample.DEFAULT_ERROR_MESSAGE
import com.langdroid.sample.DEFAULT_SUMMARY_TEXT
import com.langdroid.sample.IS_MATERIAL_3_STYLE
import com.langdroid.sample.MainUiState
import com.langdroid.sample.ProcessingUiState
import com.langdroid.sample.data.WikipediaUiModel
import com.langdroid.sample.ui.theme.LangdroidTheme
import kotlinx.coroutines.flow.StateFlow

private val DefaultPadding = 16.dp
private val TextFieldHeight = 48.dp
private val CircularProgressIndicatorSize = 18.dp
private val CircularProgressIndicatorStrokeWidth = 2.dp
private val HorizontalDividerThickness = 1.dp
private const val HorizontalDividerAlpha = 0.1f

private val PaddingDefault = 8.dp
private val PaddingLarge = 16.dp

@Composable
fun MainScreen(
    onFetchData: (String) -> Unit,
    uiStateFlow: () -> StateFlow<MainUiState>,
    onWikipediaFetched: (WikipediaUiModel) -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    var articleUrl by remember { mutableStateOf("") }

    val uiState by uiStateFlow().collectAsStateWithLifecycle()
    val processingState = uiState.processingState

    val surfaceColor = getSurfaceColor()

//    Log.d("HELLO", processingState.toString())

    LaunchedEffect(processingState) {
        when (processingState) {
            is ProcessingUiState.Fetched -> onWikipediaFetched(processingState.model)
            is ProcessingUiState.Failed -> makeToast(context, processingState.t)
            else -> Unit
        }
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            MainScreenTopBar(
                articleUrl = articleUrl,
                onArticleUrlChange = { articleUrl = it },
                processingState = processingState,
                surfaceColor = surfaceColor,
                keyboardController = keyboardController,
                onFetchData = onFetchData
            )
        },
        bottomBar = {
            MainScreenBottomBar(
                articleUrl = articleUrl,
                processingState = processingState,
                surfaceColor = surfaceColor,
                keyboardController = keyboardController,
                onFetchData = onFetchData
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = PaddingLarge, end = PaddingLarge, top = PaddingLarge)
        ) {

            val processingText: String = when {
                processingState is ProcessingUiState.Summarizing -> {
                    processingState.summaryStateText
                }

                processingState is ProcessingUiState.Success -> {
                    processingState.outputText
                }

                processingState is ProcessingUiState.Fetching -> "Fetching..."
                !processingState.isIdle() -> "Processing..."
                else -> DEFAULT_SUMMARY_TEXT
            }

            if (!uiState.textTitle.isNullOrEmpty()) Text(
                text = "Article: ${uiState.textTitle}",
                modifier = Modifier.padding(bottom = PaddingLarge),
                style = MaterialTheme.typography.titleLarge
            )

            BulletPointText(text = processingText, modifier = Modifier.fillMaxSize())
//            Text(text = processingText, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun MainScreenTopBar(
    articleUrl: String,
    onArticleUrlChange: (String) -> Unit,
    processingState: ProcessingUiState,
    surfaceColor: Color,
    keyboardController: SoftwareKeyboardController?,
    onFetchData: (String) -> Unit
) {

    Surface(color = surfaceColor) {
        Column(Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top))) {
            OutlinedTextField(
                value = articleUrl,
                onValueChange = onArticleUrlChange,
                placeholder = { Text("Wikipedia Article or URL") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DefaultPadding),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    onFetchData(articleUrl)
                }),
                enabled = processingState.isIdle()
            )
            SimpleDivider()
        }
    }
}


@Composable
private fun MainScreenBottomBar(
    articleUrl: String,
    processingState: ProcessingUiState,
    surfaceColor: Color,
    keyboardController: SoftwareKeyboardController?,
    onFetchData: (String) -> Unit
) {
    Surface(color = surfaceColor) {
        Column(
            Modifier.windowInsetsPadding(
                WindowInsets.systemBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            )
        ) {
            SimpleDivider()
            Box(Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        if (processingState.isIdle()) {
                            keyboardController?.hide()
                            onFetchData(articleUrl)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(TextFieldHeight),
                    enabled = processingState.isIdle() && articleUrl.isNotEmpty(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (!processingState.isIdle()) {
                        Text(
                            text = if (processingState is ProcessingUiState.Summarizing) "Summarizing" else "Fetching",
                            modifier = Modifier.padding(end = PaddingDefault),
                            color = MaterialTheme.colorScheme.primary
                        )
                        CircularProgressIndicator(
                            modifier = Modifier.size(CircularProgressIndicatorSize),
                            strokeWidth = CircularProgressIndicatorStrokeWidth,
                            strokeCap = StrokeCap.Round
                        )
                    } else {
                        Text("Summarize")
                    }
                }
            }
        }
    }
}

@Composable
private fun SimpleDivider() {
    if (!IS_MATERIAL_3_STYLE)
        HorizontalDivider(
            thickness = HorizontalDividerThickness,
            color = MaterialTheme.colorScheme.primary.copy(alpha = HorizontalDividerAlpha)
        )
}

@Composable
private fun getSurfaceColor(): Color {
    return if (IS_MATERIAL_3_STYLE) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.background
}

private fun makeToast(context: Context, exception: Throwable?) {
    Toast.makeText(
        context, exception?.message ?: DEFAULT_ERROR_MESSAGE, Toast.LENGTH_SHORT
    ).show()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LangdroidTheme {

    }
}
