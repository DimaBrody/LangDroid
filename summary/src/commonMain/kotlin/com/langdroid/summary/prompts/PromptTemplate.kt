package com.langdroid.summary.prompts

import com.langdroid.core.ChatPrompt
import com.langdroid.core.ChatRole

internal const val DEFAULT_SYSTEM_MESSAGE = "You are expert in a discussed field"

public class PromptTemplate public constructor(
    private val prompt: String,
    private val systemMessage: String = DEFAULT_SYSTEM_MESSAGE
) {

    public fun createPrompts(vararg promptKeys: Pair<String, String>): List<ChatPrompt> {
        return createPrompts(promptKeys.toMap())
    }

    public fun createPrompts(promptKeys: Map<String, String>): List<ChatPrompt> {


        val pattern = "\\{(.*?)\\}".toRegex() // Regex to match {key} patterns
        val matches = pattern.findAll(prompt)

        val prompts = mutableListOf<ChatPrompt>()
        prompts.add(ChatPrompt(ChatRole.System, systemMessage))

        var latestPrompt = prompt


        matches.forEach { match ->
            val key = match.groups[1]?.value
                ?: throw PromptException("Something wrong with prompt ({} may be empty or invalid)")
            val replacement =
                promptKeys[key] ?: throw PromptException("Key '$key' not found in promptKeys.")

            latestPrompt = latestPrompt.replace("{$key}", replacement)
        }

        prompts.add(ChatPrompt(ChatRole.User, latestPrompt))

        return prompts
    }
}
