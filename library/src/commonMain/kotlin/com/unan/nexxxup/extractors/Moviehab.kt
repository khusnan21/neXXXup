package com.unan.nexxxup.extractors

import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.app
import com.unan.nexxxup.newSubtitleFile
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.M3u8Helper

class MoviehabNet : Moviehab() {
    override var mainUrl = "https://play.moviehab.asia"
}

open class Moviehab : ExtractorApi() {
    override var name = "Moviehab"
    override var mainUrl = "https://play.moviehab.com"
    override val requiresReferer = false

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val res = app.get(url)
        res.document.select("video#player").let {
            //should redirect first for making it works
            val link = app.get("$mainUrl/${it.select("source").attr("src")}", referer = url).url
            M3u8Helper.generateM3u8(
                this.name,
                link,
                url
            ).forEach(callback)

            Regex("src[\"|'],\\s[\"|'](\\S+)[\"|']\\)").find(res.text)?.groupValues?.get(1).let {sub ->
                subtitleCallback.invoke(
                    newSubtitleFile(
                        it.select("track").attr("label"),
                        "$mainUrl/$sub"
                    )
                )
            }
        }
    }
}
