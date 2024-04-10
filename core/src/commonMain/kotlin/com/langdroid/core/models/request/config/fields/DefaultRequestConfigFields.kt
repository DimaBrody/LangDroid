package com.langdroid.core.models.request.config.fields

// Maybe make all fields equal to null
public class DefaultRequestConfigFields(
    override val maxOutputTokens: String = "max_tokens",
    override val temperature: String = "temperature",
    override val topP: String? = "top_p",
    override val topK: String? = "top_k",
    override val configObjectName: String? = null
) : RequestConfigFields
