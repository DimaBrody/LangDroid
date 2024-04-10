package com.langdroid.core.models.openai

import com.langdroid.core.models.request.config.fields.RequestConfigFields

public class OpenAiRequestConfigFields(
    override val maxOutputTokens: String = "max_tokens",
    override val temperature: String = "temperature",
    override val topP: String? = "top_p",
    override val topK: String? = null,
    override val configObjectName: String? = null,
) : RequestConfigFields
