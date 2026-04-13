package com.kmong

import net.pearx.kasechange.toScreamingSnakeCase
import java.net.URI

internal fun URI.queryParameters() =
    query?.split("&")?.associate {
        val parts = it.split("=", limit = 2)
        parts[0] to (parts.getOrNull(1) ?: "")
    } ?: emptyMap()

internal fun resolveSystemVar(key: String): String? =
    System.getProperty(key)?.takeIf(String::isNotBlank)
        ?: System.getenv(key.toScreamingSnakeCase())?.takeIf(String::isNotBlank)