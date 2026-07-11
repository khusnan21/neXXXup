package com.unan.nexxxup.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.AppUtils.tryParseJson
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.getAndUnpack
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.M3u8Helper

open class StreamoUpload : ExtractorApi() {
    override val name = "StreamoUpload"
    override val mainUrl = "https://streamoupload.xyz"
    override val requiresReferer = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink> {
        val sources = mutableListOf<ExtractorLink>()
        val response = app.get(url, referer = referer)
        val scriptElements = response.document.select("script").map { script ->
            if (script.data().contains("eval(function(p,a,c,k,e,d)")) {
                val data = getAndUnpack(script.data())
                    .substringAfter("sources:[")
                    .substringBefore("],")
                    .replace("file", "\"file\"")
                    .trim()
                tryParseJson<File>(data)?.let {
                    M3u8Helper.generateM3u8(
                        name,
                        it.file,
                        "$mainUrl/",
                    ).forEach { m3uData -> sources.add(m3uData) }
                }
            }
        }
        return sources
    }

    private data class File(
        @JsonProperty("file") val file: String,
    )
}
