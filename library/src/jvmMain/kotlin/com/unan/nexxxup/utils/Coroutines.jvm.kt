package com.unan.nexxxup.utils

actual fun runOnMainThreadNative(work: () -> Unit) {
    work.invoke()
}