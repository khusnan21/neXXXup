package com.unan.nexxxup.actions.temp.fcast

import android.content.Context
import com.unan.nexxxup.NexxxupApp.Companion.getActivity
import com.unan.nexxxup.R
import com.unan.nexxxup.USER_AGENT
import com.unan.nexxxup.actions.VideoClickAction
import com.unan.nexxxup.ui.result.LinkLoadingResult
import com.unan.nexxxup.ui.result.ResultEpisode
import com.unan.nexxxup.utils.txt
import com.unan.nexxxup.utils.DataStoreHelper.getViewPos
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.SingleSelectionHelper.showBottomDialog

class FcastAction: VideoClickAction() {
    override val name = txt("Fcast to device")

    override val oneSource = true

    override val sourceTypes = setOf(
        ExtractorLinkType.VIDEO,
        ExtractorLinkType.DASH,
        ExtractorLinkType.M3U8
    )

    override fun shouldShow(context: Context?, video: ResultEpisode?) = FcastManager.currentDevices.isNotEmpty()

    override suspend fun runAction(
        context: Context?,
        video: ResultEpisode,
        result: LinkLoadingResult,
        index: Int?
    ) {
        val link = result.links.getOrNull(index ?: 0) ?: return
        val devices = FcastManager.currentDevices.toList()
        uiThread {
            context?.getActivity()?.showBottomDialog(
                devices.map { it.name },
                -1,
                txt(R.string.player_settings_select_cast_device).asString(context),
                false,
                {}) {
                val position = getViewPos(video.id)?.position
                castTo(devices.getOrNull(it), link, position)
            }
        }
    }


    private fun castTo(device: PublicDeviceInfo?, link: ExtractorLink, position: Long?) {
        val host = device?.host ?: return

        FcastSession(host).use { session ->
            session.sendMessage(
                Opcode.Play,
                PlayMessage(
                    link.type.getMimeType(),
                    link.url,
                    time = position?.let { it / 1000.0 },
                    headers = mapOf(
                        "referer" to link.referer,
                        "user-agent" to USER_AGENT
                    ) + link.headers
                )
            )
        }
    }
}
