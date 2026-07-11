package com.unan.nexxxup.ui.home

import android.view.View
import androidx.viewpager2.widget.ViewPager2

class HomeScrollTransformer : ViewPager2.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        val absPos = Math.abs(position)
        page.apply {
            val scale = if (absPos > 1) 0.85f else 1 - absPos * 0.15f
            scaleX = scale
            scaleY = scale
            alpha = if (absPos > 1) 0f else 1 - absPos
        }
    }
}