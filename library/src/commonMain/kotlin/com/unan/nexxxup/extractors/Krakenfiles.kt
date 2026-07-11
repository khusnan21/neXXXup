package com.unan.nexxxup.extractors

import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.httpsify
import com.unan.nexxxup.utils.newExtractorLink

open class Krakenfiles : ExtractorApi() {
    override val name = "Krakenfiles"
    override val mainUrl = "https://krakenfiles.com"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val id = Regex("/(?:view|embed-video)/([\\da-zA-Z]+)").find(url)?.groupValues?.get(1)
        val doc = app.get("$mainUrl/embed-video/$id").document
        val link = doc.selectFirst("source")?.attr("src")

        callback.invoke(
            newExtractorLink(
                this.name,
                this.name,
                httpsify(link ?: return),
            )
        )

    }

}