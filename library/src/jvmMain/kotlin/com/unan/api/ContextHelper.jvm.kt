package com.unan.api

import java.lang.ref.WeakReference

actual fun getContext(): Any? {
    return null
}

actual fun setContext(context: WeakReference<Any>) {
}