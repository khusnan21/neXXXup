package com.lagradost.cloudstream3.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.ErrorLoadingException
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.*

open class JetPlayer : ExtractorApi() {
    override val name = "JetPlayer"
    override val mainUrl = "https://jetplayer.net"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val extRef = referer ?: ""
        val pageHtml = app.get(url, referer = extRef).text
        val hash = Regex("var hash = '([a-f0-9]+)'").find(pageHtml)?.groupValues?.get(1)
            ?: throw ErrorLoadingException("Hash bulunamadı")
        val alt = Regex("""id="alternative"\s+value="(\d+)"""").find(pageHtml)?.groupValues?.get(1)
            ?: "0"
        val ord = Regex("""id="order"\s+value="(\d+)"""").find(pageHtml)?.groupValues?.get(1)
            ?: "0"

        val response = app.post(
            "$mainUrl/jet/ajax_sources.php",
            data = mapOf(
                "vid" to hash,
                "alternative" to alt,
                "ord" to ord
            ),
            referer = mainUrl
        )

        val json = response.parsedSafe<JetResponse>() ?: throw ErrorLoadingException("Video kaynağı parse edilemedi")
        if (json.status != true) {
            throw ErrorLoadingException("Video kaynağı alınamadı")
        }

        val sources = json.source ?: throw ErrorLoadingException("source alanı yok")
        val videoUrl = sources.firstOrNull { it.file.endsWith(".mp4") }?.file
            ?: throw ErrorLoadingException("MP4 linki bulunamadı")

        callback.invoke(
            newExtractorLink(
                source = name,
                name = "JetPlayer MP4",
                url = videoUrl,
                type = ExtractorLinkType.VIDEO
            ) {
                quality = Qualities.Unknown.value
                this.referer = url
                headers = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:101.0) Gecko/20100101 Firefox/101.0"
                )
            }
        )
    }

    data class JetResponse(
        @JsonProperty("status") val status: Boolean? = null,
        @JsonProperty("source") val source: List<JetSource>? = null,
    )

    data class JetSource(
        @JsonProperty("file") val file: String,
        @JsonProperty("label") val label: String? = null,
        @JsonProperty("type") val type: String? = null,
    )
}
