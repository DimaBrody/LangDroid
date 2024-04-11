# Langdroid | Summaries for Kotlin and Android

[![Maven Central](https://img.shields.io/maven-central/v/com.aallam.openai/openai-client?color=blue&label=Download)](https://central.sonatype.com/namespace/com.aallam.openai)
[![License](https://img.shields.io/github/license/Aallam/openai-kotlin?color=yellow)](LICENSE.md)

This **Kotlin Multiplatform** library is motivated by [🦜 LangChain](https://www.langchain.com/).

Main idea is to use general model `LangDroidModel` which implements functionality of selected LLMs (Large Language Models, currently [OpenAI](https://openai.com/) and [Gemini](https://gemini.google.com/app) are available) under the hood.


Example of summary of [Wikipedia article about Immanuel Kant](https://en.wikipedia.org/wiki/Immanuel_Kant) written by [OpenAI GPT 3.5](https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates):

![Sample App Gif](https://media.giphy.com/media/v1.Y2lkPTc5MGI3NjExMTFjamxocGRib3VoNjdjeHQ5YzV0bW50dWZhazA3cTFxbXdiemt2ZCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/L2iGGb3fyBRQGpO53o/giphy.gif)

This is a `sample` app which has example of Langdroid Summary implementation.

## ⚙️ Setup
Install Langdroid summary by adding the following dependency to your `build.gradle` file:
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "com.arxiv.summary"
}
```

If you need only text output and token calcuation functionality, you can use `:core` module instead of `:summary`:

```groovy
dependencies {
	// :summary module contains it and shares its functionality by default
    implementation "com.arxiv.core"
}
```
#### Multiplatform
`:core` and `:summary` implementations are available for Kotlin (Gemini is not implemented for JVM).

## 💻 Getting Started

> [!NOTE]
> It's best practice to use environment variables for storing API keys. For guidance, refer to this [Kotlin tutorial on reading environment variables](https://www.baeldung.com/kotlin/read-env-variables) and learn how to implement the [Google Secrets Gradle plugin](https://www.baeldung.com/kotlin/read-env-variables) for Android.

#### 1. Langdroid Model
You have to create `LangDroidModel<*>`, which requires API key and contains text completion functionality:
```kotlin
// This variable set with google secrets plugin
val openAiKey = BuildConfig.OPENAI_API_KEY

val model = LangDroidModel(
	OpenAiModel.Gpt3_5(openAiKey)
)

// Google Gemini models are also available: 
// GeminiModel.Pro(geminiApiKey) 
```
You can update `GenerativeConfig` for selected model ([more](https://www.promptingguide.ai/introduction/settings) about LLM configuration):
```kotlin
val model = LangDroidModel(
	OpenAiModel.Gpt3_5(openAiKey),
	GenerativeConfig.create {
		temperature = 0.2f
		topP = 0.8f,
		maxOutputTokens = 1024
	}
)
```

<details>
<summary>Available functionality of Langdroid model now</summary>

- `generateText(String | List<ChatPrompt>)` : `Result<String>`
- `generateTextStream(String | List<ChatPrompt>)` : `Result<Flow<String>>` - flow of chat outputs
- `calculateTokens(List<ChatPrompt>)` : `Result<Int>`
- `sanityCheck()` : `Boolean` - returns `true` if  API key is valid and there is no problem with model

</details>

#### 2. Summary chain
Create a chain which consumes `text` and produces output states about text summarization.

<details>
<summary>How it works</summary>

*summary prompt* = input prompt + input text to summary + expected `maxOutputTokens`.

- If *summary prompt* is larger then [Context Window](https://community.openai.com/t/what-does-context-window-mean-in-the-documentation/566158) available for selected model, the whole text is being split by smaller chunks which thus will fit the context window and summarized. Then these summarized chunks are map-reduced to create final summary ([LangChain Map Reduce](https://js.langchain.com/docs/modules/chains/document/map_reduce)).
- If *summary prompt* is small enough to fit the Context Window, then it will be summarized directly ([LangChain Stuff Chain](https://js.langchain.com/docs/modules/chains/document/stuff)).

</details>

To implement the summary chain functionality, you have to:
1. Connect to chain state producer
2. Invoke the chain and pass the `text`

```kotlin
val summaryChain = SummaryChain(model)

// You can invoke and get flow of states:
val summaryFlow = summaryChain.invokeAndGetFlow(text)

// Function is suspend until summary task is completed
summaryFlow.collectUntilFinished { state ->
    when(state){
        is SummaryState.Idle -> { /* Nothing happens */ }
        is SummaryState.TextSplitting -> { /* When text is too large and being split */ }
        is SummaryState.Reduce -> { /* Reducing text by summarizing chunks of it; `state.processedChunks, state.allChunks`*/ }
        is SummaryState.Summarizing -> { /* Can be returned when content is being summarized and isStream = false */ }
        is SummaryState.Output -> { /* `state.text`;
			isStream = true: returns pieces of outputs like ... Output("Hel"), Output("lo, how"), Output(" are you?");
			isStream = false: returns the whole text Output("...")
		*/ }
        is SummaryState.Success -> { /* Summary is finished successfully */ }
        is SummaryState.Failure -> { /* Summary is failed; `state.t as Throwable` */ }
    }
}
```

<details>
<summary>There are also other ways connect and invoke summary chain</summary>

```kotlin
// Chain can be invoked and observed directly:
summaryChain.invokeAndObserve(text){ state -> 
	...
}

// Or you can separate invoke and state consuming (pay attenion to not create 2+ observers)
// Create live data if you are using Android:
val liveData = summaryChain.liveData()
liveData.observe { state ->
	...
}
// Or access flow of chain directly
summaryChain.processingState.collect { state ->
	...
}
// (!) But don't forget to call suspending summaryChain invoke() to start process:
summaryChain(text)
```
</details>

(Optional) You can also set you own prompts and other settings to chain:
```koltin
// IMPORTANT! Use {text} in your prompts for places where prompt has to be pasted during processing
// otherwise summary chain won't understand which text to summarize
private const val WIKIPEDIA_FINAL_PROMPT = """
Write a very detailed summary of Wikipedia page, the following text delimited by triple backquotes.
Return your response with bullet points which covers the most important key points of the text, sequentially and coherently.
```{text}```
BULLET POINT SUMMARY:
"""

// Default prompts are used if "null" or nothing passed
val promptsAndMessage = PromptsAndMessage(
    // System message added before all the messages and always noticed by LLM
    systemMessage = "You are the Wikipedia oracle",
    // Prompt for final chunk mapping and overall summarization
    finalPrompt = WIKIPEDIA_FINAL_PROMPT,
	// We ignore chunkPrompt here, therefore default will be used
)

val summaryChain = SummaryChain(
    model = model,
    isStream = false,
    promptsAndMessage = promptsAndMessage
)
```

## 🛠️ Library Development
Initially there were 2 modules for my Science App, which summarizes [arxiv](https://arxiv.org/) scientific papers. As this library worked fine for me, I've decided to publish it to GitHub, in case someone needs same functionality, even though there is a lot to develop to cover the majority of topics the LLM can do for user, especially as it's done by [🦜 LangChain](https://www.langchain.com/) or Facebook [LlamaIndex](https://docs.llamaindex.ai/en/stable/examples/index_structs/knowledge_graph/KnowledgeGraphDemo/).

There are a lot of functionality to develop, the priority is:
- Give possibility to use different models for chunk summary and final summary
- Extend settings to set chunk size and splitter type
- Add [Anthropic Claude](https://www.anthropic.com/claude) model
- Add [HuggingFace API](https://huggingface.co/inference-api/serverless) and Custom Models setup
- Improve Logging and Error exceptions clarity
- Extend it to iOS system

In case this library will be useful to developers, I will look for time to implement above functionality and fix issues if they emerged. You can star/fork to show your interest and write me on [@Dima_Brody](https://t.me/Dima_Brody) Telegram to suggest your ideas.