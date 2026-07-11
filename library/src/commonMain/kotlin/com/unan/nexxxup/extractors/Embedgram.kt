package com.unan.nexxxup.extractors

import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.getQualityFromName
import com.unan.nexxxup.utils.httpsify
import com.unan.nexxxup.utils.newExtractorLink

open class Embedgram : ExtractorApi() {
    override val name = "Embedgram"
    override val mainUrl = "https://embedgram.com"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val document = app.get(url, referer = referer).document
        val link = document.select("video source:last-child").attr("src")
        val quality = document.select("video source:last-child").attr("title")
        callback.invoke(
            newExtractorLink(
                this.name,
                this.name,
                httpsify(link),
            ) {
                this.referer = "$mainUrl/"
                this.quality = getQualityFromName(quality)
                this.headers = mapOf(
                    "Range" to "bytes=0-"
                )
            }
        )
    }
}