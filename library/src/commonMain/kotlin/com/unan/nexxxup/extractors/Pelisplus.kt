package com.unan.nexxxup.extractors

import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.amap
import com.unan.nexxxup.app
import com.unan.nexxxup.mvvm.safeAsync
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.INFER_TYPE
import com.unan.nexxxup.utils.extractorApis
import com.unan.nexxxup.utils.getQualityFromName
import com.unan.nexxxup.utils.loadExtractor
import com.unan.nexxxup.utils.newExtractorLink
import org.jsoup.Jsoup

/**
 * overrideMainUrl is necessary for for other vidstream clones like vidembed.cc
 * If they diverge it'd be better to make them separate.
 * */
open class Pelisplus(val mainUrl: String) {
    val name: String = "Vidstream"

    private fun getExtractorUrl(id: String): String {
        return "$mainUrl/play?id=$id"
    }

    private fun getDownloadUrl(id: String): String {
        return "$mainUrl/download?id=$id"
    }

    private val normalApis = arrayListOf(MultiQuality())

    // https://gogo-stream.com/streaming.php?id=MTE3NDg5
    suspend fun getUrl(
        id: String,
        isCasting: Boolean = false,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        try {
            normalApis.amap { api ->
                val url = api.getExtractorUrl(id)
                api.getSafeUrl(url, subtitleCallback = subtitleCallback, callback = callback)
            }
            val extractorUrl = getExtractorUrl(id)

            /** Stolen from GogoanimeProvider.kt extractor */
            safeAsync {
                val link = getDownloadUrl(id)
                println("Generated vidstream download link: $link")
                val page = app.get(link, referer = extractorUrl)

                val pageDoc = Jsoup.parse(page.text)
                val qualityRegex = Regex("(\\d+)P")

                //a[download]
                pageDoc.select(".dowload > a").amap { element ->
                    val href = element.attr("href")
                    val qual = if (element.text()
                            .contains("HDP")
                    ) "1080" else qualityRegex.find(element.text())?.destructured?.component1()
                        .toString()

                    if (!loadExtractor(href, link, subtitleCallback, callback)) {
                        callback.invoke(
                            newExtractorLink(
                                this.name,
                                name = this.name,
                                href
                            ) {
                                this.referer = page.url
                                this.quality = getQualityFromName(qual)
                            }
                        )
                    }
                }
            }

            with(app.get(extractorUrl)) {
                val document = Jsoup.parse(this.text)
                val primaryLinks = document.select("ul.list-server-items > li.linkserver")
                //val extractedLinksList: MutableList<ExtractorLink> = mutableListOf()

                // All vidstream links passed to extractors
                primaryLinks.distinctBy { it.attr("data-video") }.forEach { element ->
                    val link = element.attr("data-video")
                    //val name = element.text()

                    // Matches vidstream links with extractors
                    extractorApis.filter { !it.requiresReferer || !isCasting }.amap { api ->
                        if (link.startsWith(api.mainUrl)) {
                            api.getSafeUrl(link, extractorUrl, subtitleCallback, callback)
                        }
                    }
                }
                return true
            }
        } catch (e: Exception) {
            return false
        }
    }
}
