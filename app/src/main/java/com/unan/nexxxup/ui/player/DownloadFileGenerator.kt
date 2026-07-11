package com.unan.nexxxup.ui.player

import android.net.Uri
import com.unan.nexxxup.NexxxupApp.Companion.context
import com.unan.nexxxup.CommonActivity.activity
import com.unan.nexxxup.R
import com.unan.nexxxup.ui.player.PlayerSubtitleHelper.Companion.toSubtitleMimeType
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.SubtitleHelper.fromLanguageToTagIETF
import com.unan.nexxxup.utils.SubtitleUtils.cleanDisplayName
import com.unan.nexxxup.utils.SubtitleUtils.isMatchingSubtitle
import com.unan.nexxxup.utils.downloader.DownloadFileManagement.getFolder
import com.unan.nexxxup.utils.downloader.VideoDownloadManager.getDownloadFileInfo

class DownloadFileGenerator(
    episodes: List<ExtractorUri>,
    currentIndex: Int = 0
) : VideoGenerator<ExtractorUri>(episodes, currentIndex) {
    override val hasCache = false
    override val canSkipLoading = false

    override suspend fun generateLinks(
        clearCache: Boolean,
        sourceTypes: Set<ExtractorLinkType>,
        callback: (Pair<ExtractorLink?, ExtractorUri?>) -> Unit,
        subtitleCallback: (SubtitleData) -> Unit,
        offset: Int,
        isCasting: Boolean
    ): Boolean {
        val meta = getCurrent(offset) ?: return false

        if (meta.uri == Uri.EMPTY) {
            // We do this here so that we only load it when
            // we actually need it as it can be more expensive.
            val info = meta.id?.let { id ->
                activity?.let { act ->
                    getDownloadFileInfo(act, id)
                }
            }

            if (info != null) {
                val newMeta = meta.copy(uri = info.path)
                callback(null to newMeta)
            } else callback(null to meta)
        } else callback(null to meta)

        val ctx = context ?: return true
        val relative = meta.relativePath ?: return true
        val display = meta.displayName ?: return true

        val cleanDisplay = cleanDisplayName(display)

        getFolder(ctx, relative, meta.basePath)?.forEach { (name, uri) ->
            if (isMatchingSubtitle(name, display, cleanDisplay)) {
                val cleanName = cleanDisplayName(name)
                val lastNum = Regex(" ([0-9]+)$")
                val nameSuffix = lastNum.find(cleanName)?.groupValues?.get(1) ?: ""
                val originalName = cleanName.removePrefix(cleanDisplay).replace(lastNum, "").trim()

                subtitleCallback(
                    SubtitleData(
                        originalName.ifBlank { ctx.getString(R.string.default_subtitles) },
                        nameSuffix,
                        uri.toString(),
                        SubtitleOrigin.DOWNLOADED_FILE,
                        name.toSubtitleMimeType(),
                        emptyMap(),
                        fromLanguageToTagIETF(originalName, true)
                    )
                )
            }
        }

        return true
    }
}