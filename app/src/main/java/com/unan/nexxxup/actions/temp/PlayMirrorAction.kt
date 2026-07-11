package com.unan.nexxxup.actions.temp

import android.app.Activity
import android.content.Context
import com.unan.nexxxup.R
import com.unan.nexxxup.actions.VideoClickAction
import com.unan.nexxxup.ui.player.ExtractorUri
import com.unan.nexxxup.ui.player.GeneratorPlayer
import com.unan.nexxxup.ui.player.LOADTYPE_INAPP
import com.unan.nexxxup.ui.player.SubtitleData
import com.unan.nexxxup.ui.player.VideoGenerator
import com.unan.nexxxup.ui.result.LinkLoadingResult
import com.unan.nexxxup.ui.result.ResultEpisode
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.UIHelper.navigate
import com.unan.nexxxup.utils.txt

class PlayMirrorAction : VideoClickAction() {
    override val name = txt(R.string.episode_action_play_mirror)

    override val oneSource = true

    override val isPlayer = true

    override val sourceTypes: Set<ExtractorLinkType> = LOADTYPE_INAPP

    override fun shouldShow(context: Context?, video: ResultEpisode?) = true

    override suspend fun runAction(
        context: Context?,
        video: ResultEpisode,
        result: LinkLoadingResult,
        index: Int?
    ) {
        //Implemented a generator to handle the single
        val activity = context as? Activity ?: return
        val generatorMirror = object : VideoGenerator<ResultEpisode>(listOf(video)) {
            override val hasCache: Boolean = false
            override val canSkipLoading: Boolean = false

            override suspend fun generateLinks(
                clearCache: Boolean,
                sourceTypes: Set<ExtractorLinkType>,
                callback: (Pair<ExtractorLink?, ExtractorUri?>) -> Unit,
                subtitleCallback: (SubtitleData) -> Unit,
                offset: Int,
                isCasting: Boolean
            ): Boolean {
                index?.let { callback(result.links[it] to null) }
                result.subs.forEach { subtitle -> subtitleCallback(subtitle) }
                return true
            }
        }

        activity.navigate(
            R.id.global_to_navigation_player,
            GeneratorPlayer.newInstance(
                generatorMirror, result.syncData
            )
        )
    }
}