package com.langdroid.core.models.request.config.fields

public interface RequestConfigFields {
    public val maxOutputTokens: String?

    /**
     * Defines the wrapping object name for LLM API request configurations.
     *
     * - `configObjectName`: The name of the outer object (e.g., "generationConfig"). If provided, configuration settings like
     * `temperature`, `topP`, `topK`, and `maxOutputTokens` are nested within. If `null`, settings are at the top level, adapting to APIs with different structuring needs.
     */
    public val configObjectName: String?

    public val temperature: String?
    public val topP: String?
    public val topK: String?
}
