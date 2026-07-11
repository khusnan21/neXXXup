package com.unan.nexxxup.utils

import java.net.URLDecoder
import java.net.URLEncoder

object StringUtils {
    fun String.encodeUri(): String {
        return URLEncoder.encode(this, "UTF-8")
    }

    fun String.decodeUri(): String {
        return URLDecoder.decode(this, "UTF-8")
    }
}