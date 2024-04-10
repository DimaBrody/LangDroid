package com.langdroid.core.models.openai

import com.aallam.ktoken.Tokenizer
import com.langdroid.core.ChatPrompt

public suspend fun tokensCount(messages: List<ChatPrompt>, model: String): Int {
    val (tokensPerMessage, tokensPerName) = when (model) {
        "gpt-3.5-turbo-0613", "gpt-3.5-turbo-16k-0613", "gpt-4-0314", "gpt-4-32k-0314", "gpt-4-0613", "gpt-4-32k-0613", "gpt-3.5-turbo-0125", "gpt-3.5-turbo-1106" -> 3 to 1
        "gpt-3.5-turbo-0301" -> 4 to -1 // every message follows <|start|>{role/name}\n{content}<|end|>\n
        "gpt-3.5-turbo" -> {
            println("Warning: gpt-3.5-turbo may update over time. Returning num tokens assuming gpt-3.5-turbo-0613.")
            return tokensCount(messages, "gpt-3.5-turbo-0613")
        }

        "gpt-4", "gpt-4-1106-preview", "gpt-4-32k", "gpt-4-0125-preview" -> {
            println("Warning: gpt-4 may update over time. Returning num tokens assuming gpt-4-0613.")
            return tokensCount(messages, "gpt-4-0613")
        }

        else -> error("unsupported model")
    }

    val tokenizer = Tokenizer.of(model)
    var numTokens = 0
    for (message in messages) {
        numTokens += tokensPerMessage
        message.run {
            numTokens += tokenizer.encode(first.role).size
//            name?.let { numTokens += tokensPerName + tokenizer.encode(it).size }
            second?.let { numTokens += tokenizer.encode(it).size }
        }
    }
    numTokens += 3 // every reply is primed with <|start|>assistant<|message|>
    return numTokens
}
