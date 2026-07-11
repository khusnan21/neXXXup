package com.unan.nexxxup.extractors

import com.unan.nexxxup.app
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.INFER_TYPE
import com.unan.nexxxup.utils.JsUnpacker
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.newExtractorLink
import java.net.URI

open class Streamhub : ExtractorApi() {
    override var mainUrl = "https://streamhub.to"
    override var name = "Streamhub"
    override val requiresReferer = false

    override fun getExtractorUrl(id: String): String {
        return "$mainUrl/e/$id"
    }

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val response = app.get(url).text
        Regex("eval((.|\\n)*?)</script>").find(response)?.groupValues?.get(1)?.let { jsEval ->
            JsUnpacker("eval$jsEval").unpack()?.let { unPacked ->
                Regex("sources:\\[\\{src:\"(.*?)\"").find(unPacked)?.groupValues?.get(1)?.let { link ->
                    return listOf(
                        newExtractorLink(
                            source = this.name,
                            this.name,
                            link,
                        )
                    )
                }
            }
        }
        return null
    }
}