package com.unan.nexxxup.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.getQualityFromName
import com.unan.nexxxup.utils.newExtractorLink

open class Vicloud : ExtractorApi() {
    override val name: String = "Vicloud"
    override val mainUrl: String = "https://vicloud.sbs"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val id = Regex("\"apiQuery\":\"(.*?)\"").find(app.get(url).text)?.groupValues?.getOrNull(1)
        app.get(
            "$mainUrl/api/?$id=&_=${System.currentTimeMillis()}",
            headers = mapOf(
                "X-Requested-With" to "XMLHttpRequest"
            ),
            referer = url
        ).parsedSafe<Responses>()?.sources?.map { source ->
            callback.invoke(
                newExtractorLink(
                    name,
                    name,
                    source.file ?: return@map null,
                ) {
                    this.referer = url
                    this.quality = getQualityFromName(source.label)
                }
            )
        }

    }

    private data class Sources(
        @JsonProperty("file") val file: String? = null,
        @JsonProperty("label") val label: String? = null,
    )

    private data class Responses(
        @JsonProperty("sources") val sources: List<Sources>? = arrayListOf(),
    )

}