package com.langdroid.core.models.gemini

import com.langdroid.core.ChatRole

internal fun ChatRole.toGeminiRole(): ChatRole =
    if (role == "user" || role == "system") ChatRole.User else ChatRole.Model
