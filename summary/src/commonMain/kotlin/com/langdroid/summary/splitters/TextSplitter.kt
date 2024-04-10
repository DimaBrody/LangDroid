package com.langdroid.summary.splitters

public interface TextSplitter {
    public fun splitText(
        text: String,
        separators: List<String> = listOf("\n\n", "\n", " ", ""),
        onWarning: (String) -> Unit
    ): List<String>
}
