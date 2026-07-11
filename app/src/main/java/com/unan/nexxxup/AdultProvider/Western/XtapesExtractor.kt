package com.unan.nexxxup.AdultProvider.Western

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.api.Log
import com.unan.nexxxup.app
import com.unan.nexxxup.extractors.Filesim
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.INFER_TYPE
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.newExtractorLink

class Stream : Filesim() {
    override var mainUrl = "https://55k.io"
}

open class VID : ExtractorApi() {
    override var name = "VID Xtapes"
    override var mainUrl = "https://vid.xtapes.to"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val response = app.get(url).document.toString()
        val link = response.substringAfter("src: '").substringBefore("',")
        return listOf(
            newExtractorLink(
                source = this.name,
                name = this.name,
                url = link,
                INFER_TYPE
            ) {
                this.referer = referer ?: ""
                this.quality = Qualities.Unknown.value
            }
        )
    }
}
