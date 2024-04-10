package com.langdroid.core.models.gemini

import com.langdroid.core.models.GenerativeModel
import com.langdroid.core.models.GenerativeModelActions

public sealed class GeminiModel(
    override val id: String,
    override val tokenLimit: Int,
    override val outputTokenLimit: Int? = null
) : GenerativeModel() {

    private val modelActions: GenerativeModelActions by lazy { GeminiModelActions(this) }
    public override val actions: GenerativeModelActions = modelActions

    public data class Pro(override val apiKey: String?) :
        GeminiModel(
            id = "models/gemini-pro",
            tokenLimit = GEMINI_TOKEN_LIMIT,
            outputTokenLimit = GEMINI_TOKEN_OUTPUT_LIMIT
        )

    public data class Ultra(override val apiKey: String?) :
        GeminiModel(
            id = "models/gemini-ultra",
            tokenLimit = GEMINI_TOKEN_LIMIT,
            outputTokenLimit = GEMINI_TOKEN_OUTPUT_LIMIT
        )

    public data class Custom(
        override val id: String,
        override val apiKey: String?,
        override val tokenLimit: Int = GEMINI_TOKEN_LIMIT,
        override val outputTokenLimit: Int? = GEMINI_TOKEN_OUTPUT_LIMIT,
    ) : GeminiModel(id, tokenLimit, outputTokenLimit)
}

private const val GEMINI_TOKEN_OUTPUT_LIMIT = 2048
private const val GEMINI_TOKEN_LIMIT = 30720 + GEMINI_TOKEN_OUTPUT_LIMIT
