package com.langdroid.core.models.openai

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.core.Role
import com.langdroid.core.ChatPrompt
import com.langdroid.core.ChatRole

internal fun ChatPrompt.toOpenAIPrompt(): ChatMessage = ChatMessage(
    role = first.toOpenAIChatRole(),
    content = second
)

private fun ChatRole.toOpenAIChatRole() = Role(this.role)
