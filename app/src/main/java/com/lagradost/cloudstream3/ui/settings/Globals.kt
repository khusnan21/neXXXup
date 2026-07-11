package com.lagradost.cloudstream3.ui.settings

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.R

object Globals {
    var beneneCount = 0

    const val PHONE : Int = 0b001
    const val TV : Int = 0b010
    const val EMULATOR : Int = 0b100
    private const val INVALID = -1
    private var layoutId = PHONE

    private fun Context.getLayoutInt(): Int {
        return PHONE
    }

    private fun Context.isAutoTv(): Boolean {
        return false
    }

    private fun Context.layoutIntCorrected(): Int {
        return PHONE
    }

    fun Context.updateTv() {
        layoutId = PHONE
    }

    /** Returns true if the current orientation is landscape. */
    fun isLandscape(): Boolean =
        Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    /** Returns true if the layout is any of the flags,
     * so isLayout(TV or EMULATOR) is a valid statement for checking if the layout is in the emulator
     * or tv. Auto will become the "TV" or the "PHONE" layout.
     *
     * Valid flags are: PHONE, TV, EMULATOR
     * */
    fun isLayout(flags: Int) : Boolean {
        return (PHONE and flags) != 0
    }
}
