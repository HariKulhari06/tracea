package com.hari.tracea.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.json.JSONArray
import org.json.JSONObject

object JsonSyntaxHighlighter {

    private val colorKey = Color(0xFFC586C0)       // VS Code magenta
    private val colorString = Color(0xFF4EC9B0)    // Teal-green
    private val colorNumber = Color(0xFF569CD6)    // Steel blue
    private val colorBoolean = Color(0xFFCE9178)   // Terracotta
    private val colorNull = Color(0xFFCE9178)      // Terracotta
    private val colorDelimiter = Color(0xFFE0E0E0) // OnBackground

    private val lenientJson = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    private val highlightCache = android.util.LruCache<String, AnnotatedString>(30)

    /**
     * Checks if the given string is valid, formattable JSON (object or array).
     */
    fun isValidJson(content: String?): Boolean {
        if (content.isNullOrBlank()) return false
        val trimmed = content.trim()
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) return false

        return try {
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed)
            } else {
                JSONArray(trimmed)
            }
            true
        } catch (_: Exception) {
            try {
                lenientJson.parseToJsonElement(trimmed)
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    /**
     * Formats a raw JSON string into pretty-printed 2-space indented JSON.
     * Returns the formatted string if valid, or the original raw string if formatting fails.
     */
    fun prettyPrint(rawJson: String): String {
        if (rawJson.length > 100_000) return rawJson
        val trimmed = rawJson.trim()
        if (trimmed.isEmpty()) return rawJson

        return try {
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(2)
            } else {
                val element = lenientJson.parseToJsonElement(trimmed)
                lenientJson.encodeToString(JsonElement.serializer(), element)
            }
        } catch (_: Exception) {
            try {
                val element = lenientJson.parseToJsonElement(trimmed)
                lenientJson.encodeToString(JsonElement.serializer(), element)
            } catch (_: Exception) {
                rawJson
            }
        }
    }

    /**
     * Pretty-prints and applies syntax highlighting to raw JSON.
     */
    fun formatAndHighlight(rawJson: String): AnnotatedString {
        if (rawJson.length > 100_000) {
            return buildAnnotatedString {
                withStyle(SpanStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)) {
                    append(rawJson)
                }
            }
        }

        highlightCache.get(rawJson)?.let { return it }

        val pretty = prettyPrint(rawJson)

        val result = buildAnnotatedString {
            var i = 0
            val length = pretty.length
            var isKey = false

            while (i < length) {
                val ch = pretty[i]

                when {
                    ch == '"' -> {
                        val start = i
                        i++
                        while (i < length && (pretty[i] != '"' || pretty[i - 1] == '\\')) {
                            i++
                        }
                        if (i < length) i++ // Include closing quote
                        val strVal = pretty.substring(start, i)

                        // Check if key (followed by optional spaces and colon)
                        var peek = i
                        while (peek < length && pretty[peek].isWhitespace()) peek++
                        isKey = peek < length && pretty[peek] == ':'

                        val style = if (isKey) colorKey else colorString
                        withStyle(SpanStyle(color = style)) {
                            append(strVal)
                        }
                    }
                    ch.isDigit() || (ch == '-' && i + 1 < length && pretty[i + 1].isDigit()) -> {
                        val start = i
                        while (i < length && (pretty[i].isDigit() || pretty[i] == '.' || pretty[i] == 'e' || pretty[i] == 'E' || pretty[i] == '-' || pretty[i] == '+')) {
                            i++
                        }
                        val numVal = pretty.substring(start, i)
                        withStyle(SpanStyle(color = colorNumber)) {
                            append(numVal)
                        }
                    }
                    pretty.startsWith("true", i) -> {
                        withStyle(SpanStyle(color = colorBoolean)) { append("true") }
                        i += 4
                    }
                    pretty.startsWith("false", i) -> {
                        withStyle(SpanStyle(color = colorBoolean)) { append("false") }
                        i += 5
                    }
                    pretty.startsWith("null", i) -> {
                        withStyle(SpanStyle(color = colorNull)) { append("null") }
                        i += 4
                    }
                    ch == '{' || ch == '}' || ch == '[' || ch == ']' || ch == ':' || ch == ',' -> {
                        withStyle(SpanStyle(color = colorDelimiter)) { append(ch) }
                        i++
                    }
                    else -> {
                        append(ch)
                        i++
                    }
                }
            }
        }
        highlightCache.put(rawJson, result)
        return result
    }
}
