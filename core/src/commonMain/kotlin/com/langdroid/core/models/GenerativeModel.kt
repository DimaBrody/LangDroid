package com.langdroid.core.models

import com.langdroid.core.models.request.config.GenerativeConfig

public abstract class GenerativeModel {
    public abstract val apiKey: String?

    // Name of model
    public abstract val id: String

    // Max amount of tokens available for model to handle
    public abstract val tokenLimit: Int

    public abstract val outputTokenLimit: Int?

    internal var config: GenerativeConfig<*>? = null

    internal abstract val actions: GenerativeModelActions
}
