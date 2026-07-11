package com.unan.nexxxup.extractors

import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.newExtractorLink

open class CDNJWPlayer : ExtractorApi() {
    override val name: String = "CDN JWPlayer"
    override val mainUrl: String = "https://cdn.jwplayer.com"
    override val requiresReferer: Boolean = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        callback.invoke(
            newExtractorLink(
                source  = this.name,
                name    = this.name,
                url     = url,
                type    = ExtractorLinkType.M3U8
            ) {
                this.referer = referer ?: ""
                this.quality = Qualities.Unknown.value
            }
        )
    }
}
