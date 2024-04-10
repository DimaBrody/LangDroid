package com.langdroid.summary.prompts

public data class PromptsAndMessage(
    val chunkPrompt: String? = null,
    val finalPrompt: String? = null,
    val systemMessage: String? = null
)

internal data class CompletedPromptsAndMessages(
    val chunkPrompt: String,
    val finalPrompt: String,
    val systemMessage: String
)
