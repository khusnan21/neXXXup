package com.unan.nexxxup.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.AppUtils.tryParseJson
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.getQualityFromName
import com.unan.nexxxup.utils.newExtractorLink

open class JWPlayer : ExtractorApi() {
    override val name = "JWPlayer"
    override val mainUrl = "https://www.jwplayer.com"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val sources = mutableListOf<ExtractorLink>()
        with(app.get(url).document) {
            val data = this.select("script").mapNotNull { script ->
                if (script.data().contains("sources: [")) {
                    script.data().substringAfter("sources: [")
                        .substringBefore("],").replace("'", "\"")
                } else if (script.data().contains("otakudesu('")) {
                    script.data().substringAfter("otakudesu('")
                        .substringBefore("');")
                } else {
                    null
                }
            }

            tryParseJson<List<ResponseSource>>("$data")?.map {
                sources.add(
                    newExtractorLink(
                        name,
                        name,
                        it.file,
                    ) {
                        this.referer = url
                        this.quality = getQualityFromName(
                            Regex("(\\d{3,4}p)").find(it.file)?.groupValues?.get(
                                1
                            )
                        )
                    }
                )
            }
        }
        return sources
    }

    private data class ResponseSource(
        @JsonProperty("file") val file: String,
        @JsonProperty("type") val type: String?,
        @JsonProperty("label") val label: String?
    )

}