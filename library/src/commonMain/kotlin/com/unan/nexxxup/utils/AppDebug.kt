package com.unan.nexxxup.utils

import com.unan.nexxxup.InternalAPI

@InternalAPI
object AppDebug {
    @Volatile
    var isDebug: Boolean = false
}
