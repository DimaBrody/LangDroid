package com.langdroid.core.models.gemini

import com.langdroid.core.models.request.config.fields.RequestConfigFields

public class GeminiRequestConfigFields(
    override val maxOutputTokens: String = "maxOutputTokens",
    override val temperature: String = "temperature",
    override val topP: String? = "topP",
    override val topK: String? = "topK",
    override val configObjectName: String? = "generationConfig"
) : RequestConfigFields
