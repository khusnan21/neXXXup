package com.unan.nexxxup.AdultProvider.Webcam

import org.jsoup.nodes.Element
import com.unan.nexxxup.*
import com.unan.nexxxup.utils.*
import com.unan.nexxxup.extractors.DoodLaExtractor
import com.unan.nexxxup.AdultProvider.Asian.Vidguardto

class Dooodster : DoodLaExtractor() {
    override var mainUrl = "https://dooodster.com"
}

class Listeamed : Vidguardto() {
    override var mainUrl = "https://listeamed.net"
}

class Beamed : Vidguardto() {
    override var mainUrl = "https://bembed.net"
}

class Bgwp : Bigwarp() {
    override var mainUrl = "https://bgwp.cc"
}

open class Bigwarp : ExtractorApi() {
    override var name = "Bigwarp"
    override var mainUrl = "https://bigwarp.io"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val link = app.get(url, allowRedirects = false).headers["location"] ?: url
        val source = app.get(link).document.selectFirst("body > script").toString()
        val regex = Regex("""file:\s*\"((?:https?://|//)[^\"]+)""")
        val matchResult = regex.find(source)
        val match = matchResult?.groupValues?.get(1)

        if (match != null) {
            callback.invoke(
                newExtractorLink(
                    source = this.name,
                    name = this.name,
                    url = match
                ) {
                    this.referer = ""
                    this.quality = Qualities.Unknown.value
                }
            )
        }
    }
}
