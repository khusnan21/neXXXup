package com.unan.nexxxup.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.*
import com.unan.nexxxup.utils.AppUtils.parseJson

data class Files(
    @JsonProperty("file") val id: String,
    @JsonProperty("label") val label: String? = null,
)

open class Supervideo : ExtractorApi() {
    override var name = "Supervideo"
    override var mainUrl = "https://supervideo.cc"
    override val requiresReferer = false
    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val extractedLinksList: MutableList<ExtractorLink> = mutableListOf()
        val response = app.get(url).text
        val jstounpack = Regex("eval((.|\\n)*?)</script>").find(response)?.groups?.get(1)?.value
        val unpacjed = JsUnpacker(jstounpack).unpack()
        val extractedUrl =
            unpacjed?.let { Regex("""sources:((.|\n)*?)image""").find(it) }?.groups?.get(1)?.value.toString()
                .replace("file", """"file"""").replace("label", """"label"""")
                .substringBeforeLast(",")
        val parsedlinks = parseJson<List<Files>>(extractedUrl)
        parsedlinks.forEach { data ->
            if (data.label.isNullOrBlank()) { // mp4 links (with labels) are slow. Use only m3u8 link.
                M3u8Helper.generateM3u8(
                    name,
                    data.id,
                    url,
                    headers = mapOf("referer" to url)
                ).forEach { link ->
                    extractedLinksList.add(link)
                }
            }
        }
        return extractedLinksList
    }
}