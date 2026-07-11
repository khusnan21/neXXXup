package com.unan.nexxxup.extractors

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.*
import com.unan.nexxxup.utils.AppUtils.tryParseJson

class Yufiles : Hxfile() {
    override val name = "Yufiles"
    override val mainUrl = "https://yufiles.com"
}

class Aico : Hxfile() {
    override val name = "Aico"
    override val mainUrl = "https://aico.pw"
}

open class Hxfile : ExtractorApi() {
    override val name = "Hxfile"
    override val mainUrl = "https://hxfile.co"
    override val requiresReferer = false
    open val redirect = true

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val sources = mutableListOf<ExtractorLink>()
        val document = app.get(url, allowRedirects = redirect, referer = referer).document
        with(document) {
            this.select("script").map { script ->
                if (script.data().contains("eval(function(p,a,c,k,e,d)")) {
                    val data =
                        getAndUnpack(script.data()).substringAfter("sources:[").substringBefore("]")
                    tryParseJson<List<ResponseSource>>("[$data]")?.map {
                        sources.add(
                            newExtractorLink(
                                name,
                                name,
                                it.file,
                            ) {
                                this.referer = mainUrl
                                this.quality = when {
                                    url.contains("hxfile.co") -> getQualityFromName(
                                        Regex("\\d\\.(.*?).mp4").find(
                                            document.select("title").text()
                                        )?.groupValues?.get(1).toString()
                                    )
                                    else -> getQualityFromName(it.label)
                                }
                            }
                        )
                    }
                } else if (script.data().contains("\"sources\":[")) {
                    val data = script.data().substringAfter("\"sources\":[").substringBefore("]")
                    tryParseJson<List<ResponseSource>>("[$data]")?.map {
                        sources.add(
                            newExtractorLink(
                                name,
                                name,
                                it.file,
                            ) {
                                this.referer = mainUrl
                                this.quality = when {
                                    it.label?.contains("HD") == true -> Qualities.P720.value
                                    it.label?.contains("SD") == true -> Qualities.P480.value
                                    else -> getQualityFromName(it.label)
                                }
                            }
                        )
                    }
                }
                else {
                    null
                }
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
