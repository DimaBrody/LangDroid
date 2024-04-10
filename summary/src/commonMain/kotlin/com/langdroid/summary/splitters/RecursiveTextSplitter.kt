package com.langdroid.summary.splitters

import java.util.regex.Pattern

public class RecursiveTextSplitter(
    private val chunkSize: Int,
    private val chunkOverlap: Int,
    private val keepSeparator: Boolean = false,
    private val isSeparatorRegex: Boolean = false,
) : TextSplitter {

    public override fun splitText(
        text: String,
        separators: List<String>,
        onWarning: (String) -> Unit
    ): List<String> {
        if (separators.isEmpty()) return listOf(text)

        val finalChunks = mutableListOf<String>()
        var separator = separators.last()
        var newSeparators = listOf<String>()
        for ((i, sep) in separators.withIndex()) {
            val currentSeparator = if (isSeparatorRegex) sep else Pattern.quote(sep)
            if (sep.isEmpty() || Regex(currentSeparator).containsMatchIn(text)) {
                separator = sep
                newSeparators = separators.drop(i + 1)
                break
            }
        }

        val splitRegex = if (isSeparatorRegex) separator else Pattern.quote(separator)
        val splits = splitTextWithRegex(text, splitRegex, keepSeparator)
        val goodSplits = mutableListOf<String>()
        val mergeSeparator = if (keepSeparator) "" else separator

        for (split in splits) {
            if (split.length < chunkSize) {
                goodSplits.add(split)
            } else {
                if (goodSplits.isNotEmpty()) {
                    val mergedText = mergeSplits(goodSplits, mergeSeparator, onWarning)
                    finalChunks.addAll(mergedText)
                    goodSplits.clear()
                }
                if (newSeparators.isEmpty()) {
                    finalChunks.add(split)
                } else {
                    val otherInfo = splitText(split, newSeparators, onWarning)
                    finalChunks.addAll(otherInfo)
                }
            }
        }

        if (goodSplits.isNotEmpty()) {
            val mergedText = mergeSplits(goodSplits, mergeSeparator, onWarning)
            finalChunks.addAll(mergedText)
        }

        return finalChunks
    }

    private fun mergeSplits(
        splits: Iterable<String>,
        separator: String,
        onWarning: (String) -> Unit
    ): List<String> {
        val separatorLength = lengthFunction(separator)

        val docs = mutableListOf<String>()
        var currentDoc = mutableListOf<String>()
        var totalLength = 0
        for (split in splits) {
            val splitLength = lengthFunction(split)
            if (totalLength + splitLength + (if (currentDoc.isNotEmpty()) separatorLength else 0) > chunkSize) {
                if (totalLength > chunkSize) {
                    onWarning("Created a chunk of size $totalLength, which is longer than the specified $chunkSize")
                }
                if (currentDoc.isNotEmpty()) {
                    val doc = joinDocs(currentDoc, separator)
                    docs.add(doc)
                    // Popping from the current document if it's larger than the chunk overlap
                    // or still has chunks and the length is too long.
                    while (totalLength > chunkOverlap || (totalLength + splitLength + (if (currentDoc.isNotEmpty()) separatorLength else 0) > chunkSize && totalLength > 0)) {
                        totalLength -= lengthFunction(currentDoc.first()) + (if (currentDoc.size > 1) separatorLength else 0)
                        currentDoc = currentDoc.drop(1).toMutableList()
                    }
                }
            }
            currentDoc.add(split)
            totalLength += splitLength + (if (currentDoc.size > 1) separatorLength else 0)
        }
        val doc = joinDocs(currentDoc, separator)
        docs.add(doc)
        return docs
    }

    private fun joinDocs(docs: List<String>, separator: String): String =
        docs.joinToString(separator)

    private fun lengthFunction(input: String): Int = input.length

    private fun splitTextWithRegex(
        text: String, separator: String, keepSeparator: Boolean
    ): List<String> {
        if (separator.isEmpty()) return text.map { it.toString() } // Split into characters if separator is empty.
        val pattern = if (keepSeparator) "($separator)" else separator
        val splits = Regex(pattern).split(text, 0).filterNot { it.isEmpty() }
        return if (keepSeparator) {
            val result = mutableListOf<String>()
            var i = 0
            while (i < splits.size) {
                if (i + 1 < splits.size && splits[i + 1] == separator) {
                    result.add(splits[i] + separator)
                    i += 2
                } else {
                    result.add(splits[i])
                    i++
                }
            }
            result
        } else {
            splits
        }
    }


}
