package com.hari.tracea.web

import com.hari.tracea.core.model.BodyData
import com.hari.tracea.core.model.NetworkEvent
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Robust JSON serializer for NetworkEvent to power the Web Dashboard and REST APIs.
 */
object NetworkEventJsonSerializer {

    fun serialize(event: NetworkEvent): JsonObject {
        return buildJsonObject {
            put("id", event.id)
            put("timestamp", event.timestamp)
            put("method", event.method.name)
            put("url", event.url)
            put("scheme", event.scheme)
            put("host", event.host)
            if (event.port != null) put("port", event.port!!)
            put("path", event.path)

            put("queryParameters", buildJsonObject {
                event.queryParameters.forEach { (k, v) ->
                    put(k, buildJsonArray { v.forEach { add(JsonPrimitive(it)) } })
                }
            })

            if (event.protocol != null) put("protocol", event.protocol)

            put("requestHeaders", buildJsonObject {
                event.requestHeaders.forEach { (k, v) ->
                    put(k, buildJsonArray { v.forEach { add(JsonPrimitive(it)) } })
                }
            })

            event.requestBody?.let { body ->
                put("requestBody", serializeBody(body))
            }
            if (event.requestContentType != null) put("requestContentType", event.requestContentType)
            put("requestSize", event.requestSize)

            if (event.statusCode != null) put("statusCode", event.statusCode!!)
            if (event.statusMessage != null) put("statusMessage", event.statusMessage)

            put("responseHeaders", buildJsonObject {
                event.responseHeaders.forEach { (k, v) ->
                    put(k, buildJsonArray { v.forEach { add(JsonPrimitive(it)) } })
                }
            })

            event.responseBody?.let { body ->
                put("responseBody", serializeBody(body))
            }
            if (event.responseContentType != null) put("responseContentType", event.responseContentType)
            put("responseSize", event.responseSize)

            put("timing", buildJsonObject {
                put("startTimestamp", event.timing.startTimestamp)
                event.timing.endTimestamp?.let { put("endTimestamp", it) }
                event.timing.dnsMs?.let { put("dnsMs", it) }
                event.timing.connectMs?.let { put("connectMs", it) }
                event.timing.tlsMs?.let { put("tlsMs", it) }
                event.timing.waitingMs?.let { put("waitingMs", it) }
                event.timing.downloadMs?.let { put("downloadMs", it) }
                event.timing.totalMs?.let { put("totalMs", it) }
            })

            event.error?.let { err ->
                put("error", buildJsonObject {
                    put("type", err.type.name)
                    if (err.message != null) put("message", err.message)
                })
            }

            put("source", event.source.name)
            put("state", event.state.name)
            put("sessionId", event.sessionId)
            put("sessionName", event.sessionName)
        }
    }

    fun serializeList(events: List<NetworkEvent>): JsonArray {
        return buildJsonArray {
            events.forEach { add(serialize(it)) }
        }
    }

    private fun serializeBody(body: BodyData): JsonObject {
        return buildJsonObject {
            put("contentType", body.contentType.name)
            put("size", body.size)
            when (body) {
                is BodyData.Text -> put("content", body.content)
                is BodyData.FileReference -> put("content", "[Stored in file: ${body.path}]")
                is BodyData.Truncated -> put("content", "[Truncated body (actual: ${body.actualSize} B, captured: ${body.capturedSize} B)]")
                is BodyData.Binary -> put("content", "[Binary Data: ${body.size} bytes]")
            }
        }
    }
}
