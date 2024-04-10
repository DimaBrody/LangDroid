package com.langdroid.sample.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun BulletPointText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // Split the text into lines
        val lines = text.split("\n")

        lines.forEach { line ->
            // Check if the line starts with a dash and optionally some leading spaces
            if (line.trimStart().startsWith("-")) {
                // Remove the dash and any spaces around it, then trim the result
                val trimmedLine = line.trimStart().removePrefix("-").trim()

                // Build a bullet point string
                val bulletPointText = buildAnnotatedString {
                    // Append a bullet point and a space before the actual text
                    append("• $trimmedLine")
                }

                // Display the line with a bullet point
                Text(
                    text = bulletPointText,
                    modifier = Modifier.padding(start = 4.dp, top = 0.dp, bottom = 12.dp),
                )
            } else {
                // Line does not start with a dash, display it as is
                Text(
                    text = line
                )
            }
        }
    }
}
