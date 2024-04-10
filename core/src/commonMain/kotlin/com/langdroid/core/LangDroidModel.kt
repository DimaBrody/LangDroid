package com.langdroid.core

import com.langdroid.core.models.GenerativeModel
import com.langdroid.core.models.GenerativeModelApiActions
import com.langdroid.core.models.request.config.GenerativeConfig

public class LangDroidModel<M : GenerativeModel>(
    public val model: M,
    public val config: GenerativeConfig<M>? = null
) : GenerativeModelApiActions by LangDroidModelActions(model, config)
