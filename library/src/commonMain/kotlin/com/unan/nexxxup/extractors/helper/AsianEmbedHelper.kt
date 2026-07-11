package com.unan.nexxxup.extractors.helper

import com.unan.api.Log
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.amap
import com.unan.nexxxup.app
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.loadExtractor

class AsianEmbedHelper {
    companion object {
        suspend fun getUrls(
            url: String,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
        ) {
            // Fetch links
            val doc = app.get(url).document
            val links = doc.select("div#list-server-more > ul > li.linkserver")
            if (!links.isNullOrEmpty()) {
                links.amap {
                    val datavid = it.attr("data-video")
                    //Log.i("AsianEmbed", "Result => (datavid) ${datavid}")
                    if (datavid.isNotBlank()) {
                        val res = loadExtractor(datavid, url, subtitleCallback, callback)
                        Log.i("AsianEmbed", "Result => ($res) (datavid) $datavid")
                    }
                }
            }
        }
    }
}