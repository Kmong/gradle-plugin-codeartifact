package com.kmong

import net.pearx.kasechange.toScreamingSnakeCase
import java.net.URI

internal fun URI.queryParameters() =
    query?.split("&")?.associate {
        val (key, value) = it.split("=", limit = 2)
        key to value
    } ?: emptyMap()

internal fun resolveSystemVar(key: String): String? =
    System.getProperty(key)?.takeIf(String::isNotBlank)
        ?: System.getenv(key.toScreamingSnakeCase())?.takeIf(String::isNotBlank)