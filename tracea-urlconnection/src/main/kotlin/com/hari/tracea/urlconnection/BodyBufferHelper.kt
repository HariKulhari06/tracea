package com.hari.tracea.urlconnection

import com.hari.tracea.core.config.BodyCaptureConfig
import com.hari.tracea.core.model.BodyContentType
import com.hari.tracea.core.model.BodyData
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

internal object BodyBufferHelper {

    fun parseContentType(mediaType: String?): BodyContentType {
        if (mediaType == null) return BodyContentType.UNKNOWN
        val lowerType = mediaType.lowercase()
        return when {
            lowerType.contains("application/json") -> BodyContentType.JSON
            lowerType.contains("application/xml") || lowerType.contains("text/xml") -> BodyContentType.XML
            lowerType.contains("text/html") -> BodyContentType.HTML
            lowerType.contains("application/x-www-form-urlencoded") -> BodyContentType.FORM
            lowerType.contains("multipart/") -> BodyContentType.MULTIPART
            lowerType.contains("image/") -> BodyContentType.IMAGE
            lowerType.contains("video/") -> BodyContentType.VIDEO
            lowerType.contains("audio/") -> BodyContentType.AUDIO
            lowerType.contains("text/") -> BodyContentType.TEXT
            else -> BodyContentType.BINARY
        }
    }

    fun createBodyData(
        bytes: ByteArray,
        contentTypeHeader: String?,
        config: BodyCaptureConfig,
        maxSize: Long
    ): BodyData? {
        if (bytes.isEmpty()) return null

        val parsedType = parseContentType(contentTypeHeader)
        if (parsedType == BodyContentType.BINARY && !config.captureBinary) {
            return BodyData.Binary(size = bytes.size.toLong(), contentType = parsedType)
        }

        val totalSize = bytes.size.toLong()
        val capturedBytes = if (totalSize > maxSize) {
            bytes.copyOf(maxSize.toInt())
        } else {
            bytes
        }

        val charset = extractCharset(contentTypeHeader)

        return when (parsedType) {
            BodyContentType.BINARY, BodyContentType.IMAGE, BodyContentType.VIDEO, BodyContentType.AUDIO -> {
                if (totalSize > maxSize) {
                    BodyData.Truncated(actualSize = totalSize, capturedSize = maxSize, contentType = parsedType)
                } else {
                    BodyData.Binary(size = totalSize, contentType = parsedType)
                }
            }
            else -> {
                val text = try {
                    String(capturedBytes, charset)
                } catch (e: Exception) {
                    String(capturedBytes, StandardCharsets.UTF_8)
                }
                if (totalSize > maxSize) {
                    BodyData.Truncated(actualSize = totalSize, capturedSize = maxSize, contentType = parsedType)
                } else {
                    BodyData.Text(content = text, contentType = parsedType, size = totalSize)
                }
            }
        }
    }

    private fun extractCharset(contentType: String?): Charset {
        if (contentType == null) return StandardCharsets.UTF_8
        val parts = contentType.split(";")
        for (part in parts) {
            val trimmed = part.trim()
            if (trimmed.startsWith("charset=", ignoreCase = true)) {
                val charsetName = trimmed.substring("charset=".length).trim().replace("\"", "")
                return runCatching { Charset.forName(charsetName) }.getOrDefault(StandardCharsets.UTF_8)
            }
        }
        return StandardCharsets.UTF_8
    }
}
