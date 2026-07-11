package com.unan.nexxxup.actions.temp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.net.toUri
import com.unan.api.Log
import com.unan.nexxxup.NexxxupApp.Companion.getKey
import com.unan.nexxxup.actions.OpenInAppAction
import com.unan.nexxxup.actions.makeTempM3U8Intent
import com.unan.nexxxup.actions.updateDurationAndPosition
import com.unan.nexxxup.ui.result.LinkLoadingResult
import com.unan.nexxxup.ui.result.ResultEpisode
import com.unan.nexxxup.utils.txt
import com.unan.nexxxup.ui.subtitles.SUBTITLE_AUTO_SELECT_KEY
import com.unan.nexxxup.utils.DataStoreHelper.getViewPos

// https://github.com/videolan/vlc-android/blob/3706c4be2da6800b3d26344fc04fab03ffa4b860/application/vlc-android/src/org/videolan/vlc/gui/video/VideoPlayerActivity.kt#L1898
// https://wiki.videolan.org/Android_Player_Intents/

class VlcNightlyPackage : VlcPackage() {
    override val packageName = "org.videolan.vlc.debug"
    override val appName = txt("VLC Nightly")
}

open class VlcPackage: OpenInAppAction(
    appName = txt("VLC"),
    packageName = "org.videolan.vlc",
    intentClass = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        "org.videolan.vlc.gui.video.VideoPlayerActivity"
    } else {
        null
    },
    action = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        "org.videolan.vlc.player.result"
    } else {
        Intent.ACTION_VIEW
    }
) {
    // while VLC supports multi links, it has poor support, so we disable it for now
    override val oneSource = true

    override suspend fun putExtra(
        context: Context,
        intent: Intent,
        video: ResultEpisode,
        result: LinkLoadingResult,
        index: Int?
    ) {
        if (index != null) {
            intent.setDataAndType(result.links[index].url.toUri(), "video/*")
        } else {
            makeTempM3U8Intent(context, intent, result)
        }
        val position = getViewPos(video.id)?.position ?: 0L

        intent.putExtra("from_start", false)
        intent.putExtra("position", position)
        intent.putExtra("secure_uri", true)
        intent.putExtra("title", video.name)

        val subsLang = getKey(SUBTITLE_AUTO_SELECT_KEY) ?: "en"
        result.subs.firstOrNull {
            subsLang == it.languageCode
        }?.let {
            intent.putExtra("subtitles_location", it.url)
        }
    }

    override fun onResult(activity: Activity, intent: Intent?) {
        val position = intent?.getLongExtra("extra_position", -1) ?: -1
        val duration = intent?.getLongExtra("extra_duration", -1) ?: -1
        Log.d("VLC", "Position: $position, Duration: $duration")
        updateDurationAndPosition(position, duration)
    }
}