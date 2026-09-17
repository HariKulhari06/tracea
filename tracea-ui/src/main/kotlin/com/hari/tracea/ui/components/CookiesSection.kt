package com.hari.tracea.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hari.tracea.ui.theme.LocalDebuggerColors

/**
 * Data class representing a parsed cookie.
 */
data class ParsedCookie(
    val name: String,
    val value: String,
    val attributes: Map<String, String> = emptyMap(),
    val flags: Set<String> = emptySet()
)

/**
 * Displays a section with parsed cookies in a structured table-like layout.
 *
 * For request cookies (Cookie header): shows name=value pairs.
 * For response cookies (Set-Cookie header): shows name=value pairs with
 * attributes like Path, Domain, Expires, Max-Age, SameSite, and flags
 * like HttpOnly and Secure.
 */
@Composable
fun CookiesSection(
    title: String,
    cookies: List<ParsedCookie>,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null
) {
    if (cookies.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }
    val displayCookies = if (expanded || cookies.size <= 5) cookies else cookies.take(5)
    val actionText = if (cookies.size > 5) {
        if (expanded) "Show Less" else "View All (${cookies.size})"
    } else null

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = title,
            action = actionText,
            onAction = { expanded = !expanded }
        )

        Spacer(modifier = Modifier.height(4.dp))

        val colors = LocalDebuggerColors.current

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(colors.surfaceContainer)
                .border(0.5.dp, colors.outline, RoundedCornerShape(8.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Copy button row
                if (onCopy != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier
                                .padding(top = 2.dp, end = 2.dp)
                                .size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = colors.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                displayCookies.forEachIndexed { index, cookie ->
                    CookieRow(cookie = cookie)
                    if (index < displayCookies.size - 1) {
                        HorizontalDivider(
                            color = colors.outline.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun CookieRow(cookie: ParsedCookie) {
    val colors = LocalDebuggerColors.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Cookie name = value
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = cookie.name,
                color = colors.primary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = " = ",
                color = colors.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
            Text(
                text = cookie.value,
                color = colors.onSurface,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        }

        // Attributes (Path, Domain, Expires, Max-Age, SameSite)
        if (cookie.attributes.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                cookie.attributes.forEach { (key, value) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = key,
                            color = colors.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "=",
                            color = colors.outline,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = value,
                            color = colors.onSurface.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Flags (HttpOnly, Secure)
        if (cookie.flags.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cookie.flags.forEach { flag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.primaryContainer)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = flag,
                            color = colors.onPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Utility for parsing cookie strings.
 */
object CookieParser {

    /**
     * Known cookie attribute names (case-insensitive).
     */
    private val KNOWN_ATTRIBUTES = setOf(
        "path", "domain", "expires", "max-age", "samesite"
    )

    /**
     * Known cookie flag names (case-insensitive, attributes without values).
     */
    private val KNOWN_FLAGS = setOf(
        "httponly", "secure", "partitioned"
    )

    /**
     * Parse request Cookie header values.
     * Format: "name1=value1; name2=value2; name3=value3"
     */
    fun parseRequestCookies(cookieHeaderValues: List<String>): List<ParsedCookie> {
        val cookies = mutableListOf<ParsedCookie>()
        for (headerValue in cookieHeaderValues) {
            // Handle redacted values
            if (headerValue.contains("[REDACTED]")) {
                cookies.add(ParsedCookie(name = "Cookie", value = "[REDACTED]"))
                continue
            }
            val pairs = headerValue.split(";").map { it.trim() }.filter { it.isNotEmpty() }
            for (pair in pairs) {
                val eqIndex = pair.indexOf('=')
                if (eqIndex > 0) {
                    val name = pair.substring(0, eqIndex).trim()
                    val value = pair.substring(eqIndex + 1).trim()
                    cookies.add(ParsedCookie(name = name, value = value))
                } else {
                    cookies.add(ParsedCookie(name = pair.trim(), value = ""))
                }
            }
        }
        return cookies
    }

    /**
     * Parse response Set-Cookie header values.
     * Format: "name=value; Path=/; Domain=.example.com; HttpOnly; Secure; SameSite=Lax"
     * Each Set-Cookie header value represents one cookie.
     */
    fun parseResponseCookies(setCookieValues: List<String>): List<ParsedCookie> {
        val cookies = mutableListOf<ParsedCookie>()
        for (setCookieValue in setCookieValues) {
            // Handle redacted values
            if (setCookieValue.contains("[REDACTED]")) {
                cookies.add(ParsedCookie(name = "Set-Cookie", value = "[REDACTED]"))
                continue
            }
            val parts = setCookieValue.split(";").map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isEmpty()) continue

            // First part is name=value
            val firstPart = parts[0]
            val eqIndex = firstPart.indexOf('=')
            val name: String
            val value: String
            if (eqIndex > 0) {
                name = firstPart.substring(0, eqIndex).trim()
                value = firstPart.substring(eqIndex + 1).trim()
            } else {
                name = firstPart.trim()
                value = ""
            }

            // Remaining parts are attributes or flags
            val attributes = mutableMapOf<String, String>()
            val flags = mutableSetOf<String>()

            for (i in 1 until parts.size) {
                val attrPart = parts[i]
                val attrEqIndex = attrPart.indexOf('=')
                if (attrEqIndex > 0) {
                    val attrName = attrPart.substring(0, attrEqIndex).trim()
                    val attrValue = attrPart.substring(attrEqIndex + 1).trim()
                    if (KNOWN_ATTRIBUTES.contains(attrName.lowercase())) {
                        attributes[attrName] = attrValue
                    }
                } else {
                    val flagName = attrPart.trim()
                    if (KNOWN_FLAGS.contains(flagName.lowercase())) {
                        flags.add(flagName)
                    }
                }
            }

            cookies.add(ParsedCookie(name = name, value = value, attributes = attributes, flags = flags))
        }
        return cookies
    }
}
