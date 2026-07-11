package com.unan.nexxxup.extractors

import com.unan.nexxxup.app
import com.unan.nexxxup.utils.*

class MixDropPs : MixDrop() {
    override var mainUrl = "https://mixdrop.ps"
}

class Mdy : MixDrop() {
    override var mainUrl = "https://mdy48tn97.com"
}

class MxDropTo : MixDrop() {
    override var mainUrl = "https://mxdrop.to"
}

class MixDropSi : MixDrop() {
    override var mainUrl = "https://mixdrop.si"
}

class MixDropBz : MixDrop(){
    override var mainUrl = "https://mixdrop.bz"
}

class MixDropAg : MixDrop(){
    override var mainUrl = "https://mixdrop.ag"
}

class MixDropCh : MixDrop(){
    override var mainUrl = "https://mixdrop.ch"
}
class MixDropTo : MixDrop(){
    override var mainUrl = "https://mixdrop.to"
}

class MixDropTop : MixDrop(){
    override var mainUrl = "https://mixdrop.top"
}

open class MixDrop : ExtractorApi() {
    override var name = "MixDrop"
    override var mainUrl = "https://mixdrop.co"
    private val srcRegex = Regex("""wurl.*?=.*?"(.*?)";""")
    override val requiresReferer = false

    override fun getExtractorUrl(id: String): String {
        return "$mainUrl/e/$id"
    }

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        with(app.get(url.replaceFirst("/f/", "/e/"))) {
            getAndUnpack(this.text).let { unpackedText ->
                srcRegex.find(unpackedText)?.groupValues?.get(1)?.let { link ->
                    return listOf(
                        newExtractorLink(
                            name,
                            name,
                            httpsify(link).replace("&amp;", "&"),
                        ) {
                            this.referer = url
                            this.quality = Qualities.Unknown.value
                        }
                    )
                }
            }
        }
        return null
    }
}
