package com.langdroid.core

@JvmInline
public value class ChatRole(public val role: String) {
    public companion object {
        public val System: ChatRole = ChatRole("system")
        public val User: ChatRole = ChatRole("user")
        public val Assistant: ChatRole = ChatRole("assistant")
        public val Function: ChatRole = ChatRole("function")
        public val Tool: ChatRole = ChatRole("tool")
        public val Model: ChatRole = ChatRole("model")
    }
}


public typealias ChatPrompt = Pair<ChatRole, String?>
