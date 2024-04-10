package com.langdroid.core.models.openai

import com.langdroid.core.models.GenerativeModel
import com.langdroid.core.models.GenerativeModelActions

public sealed class OpenAiModel(
    override val id: String,
    override val tokenLimit: Int,
    override val outputTokenLimit: Int? = null
) : GenerativeModel() {

    private val modelActions: GenerativeModelActions by lazy { OpenAiModelActions(this) }
    public override val actions: GenerativeModelActions = modelActions


    /*
    gpt-3.5-turbo-0125 - The latest GPT-3.5 Turbo model with higher
    accuracy at responding in requested formats and a fix for a bug
    which caused a text encoding issue for non-English language function calls.
     */
    public data class Gpt3_5(override val apiKey: String?) : OpenAiModel(
        // Same as gpt-3.5-turbo
        id = "gpt-3.5-turbo-0125",
        tokenLimit = GPT3_5_TOKEN_LIMIT,
        outputTokenLimit = GPT_TOKEN_OUTPUT_LIMIT
    )

    /*
    gpt-4 - Currently points to gpt-4-0613. Snapshot of GPT-4 from
    June 13th 2023 with improved function calling support.
    Training data up to Sep 2021.
    */
    public data class Gpt4(override val apiKey: String?) : OpenAiModel(
        id = "gpt-4", tokenLimit = GPT4_TOKEN_LIMIT, outputTokenLimit = GPT_TOKEN_OUTPUT_LIMIT
    )


    // OTHER MODELS

    /*
    gpt-3.5-turbo-1106 - GPT-3.5 Turbo model with improved instruction following,
    JSON mode, reproducible outputs, parallel function calling, and more.
    */
    public data class Gpt3_5Plus(override val apiKey: String?) : OpenAiModel(
        id = "gpt-3.5-turbo-1106",
        tokenLimit = GPT3_5_TOKEN_LIMIT,
        outputTokenLimit = GPT_TOKEN_OUTPUT_LIMIT
    )

    /*
    gpt-4-0125-preview - The latest GPT-4 model intended to reduce
    cases of “laziness” where the model doesn’t complete a task.
    Training data up to Dec 2023.
     */
    public data class Gpt4_128kNoLazy(override val apiKey: String?) : OpenAiModel(
        id = "gpt-4-0125-preview",
        tokenLimit = GPT_128K_TOKEN_LIMIT,
        outputTokenLimit = GPT_TOKEN_OUTPUT_LIMIT
    )

    /*
    gpt-4-1106-preview - GPT-4 Turbo model featuring improved instruction
    following, JSON mode, reproducible outputs, parallel function calling, and more.
    Training data up to Apr 2023.
     */
    public data class Gpt4_128kPlus(override val apiKey: String?) : OpenAiModel(
        id = "gpt-4-1106-preview",
        tokenLimit = GPT_128K_TOKEN_LIMIT,
        outputTokenLimit = GPT_TOKEN_OUTPUT_LIMIT
    )

    /*
    gpt-4-32k - Currently points to gpt-4-32k-0613.
    Snapshot of gpt-4-32k from June 13th 2023 with improved function calling support.
    This model was never rolled out widely in favor of GPT-4 Turbo.
    Training data up to Sep 2021.
     */
    public data class Gpt4_32k(override val apiKey: String?) : OpenAiModel(
        id = "gpt-4-32k",
        tokenLimit = GPT_32K_TOKEN_LIMIT,
        outputTokenLimit = GPT_TOKEN_OUTPUT_LIMIT
    )

    public data class Custom(
        override val id: String,
        override val apiKey: String?,
        override val tokenLimit: Int = CUSTOM_MODEL_TOKEN_LIMIT,
        override val outputTokenLimit: Int? = GPT_TOKEN_OUTPUT_LIMIT,
    ) : OpenAiModel(id, tokenLimit, outputTokenLimit)
}

private const val CUSTOM_MODEL_TOKEN_LIMIT = 4096
private const val GPT4_TOKEN_LIMIT = 8192
private const val GPT3_5_TOKEN_LIMIT = 16385

private const val GPT_TOKEN_OUTPUT_LIMIT = 4096
private const val GPT_128K_TOKEN_LIMIT = 128000
private const val GPT_32K_TOKEN_LIMIT = 32768
