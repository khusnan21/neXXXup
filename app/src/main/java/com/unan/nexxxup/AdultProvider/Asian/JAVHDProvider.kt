package com.unan.nexxxup.AdultProvider.Asian

import com.unan.nexxxup.HomePageList
import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SearchResponseList
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.TvType
import com.unan.nexxxup.VPNStatus
import com.unan.nexxxup.app
import com.unan.nexxxup.base64Decode
import com.unan.nexxxup.fixUrlNull
import com.unan.nexxxup.mainPageOf
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.newMovieLoadResponse
import com.unan.nexxxup.newMovieSearchResponse
import com.unan.nexxxup.newSearchResponseList
import com.unan.nexxxup.newSubtitleFile
import com.unan.nexxxup.runAllAsync
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.loadExtractor
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.json.JSONObject
import org.jsoup.Jsoup

class JAVHDProvider : MainAPI() {
    override var mainUrl              = "https://javhd.today"
    override var name                 = "JAV HD"
    override val hasMainPage          = true
    override var lang                 = "en"
    override val hasDownloadSupport   = true
    override val hasChromecastSupport = true
    override val supportedTypes       = setOf(TvType.NSFW)
    override val vpnStatus            = VPNStatus.MightBeNeeded
    val subtitleCatUrl = "https://www.subtitlecat.com"
    override val mainPage = mainPageOf(
            "/releaseday/" to "Release Day",
            "/recent/" to "Latest Updates",
            "/popular/today/" to "Most View Today",
            "/popular/week/" to "Most View Week",
            "/eng-sub-jav/recent/" to "Recent Jav Subbed",
            "/eng-sub-jav/popular/year/" to "Most Viewed Jav Subbed",
            "/uncensored-jav/recent/" to "Recent Uncensored",
            "/reducing-mosaic/recent/" to "Recent Reduced Mosaic",
        )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val path = request.data
        val cleanPath = path.removePrefix(mainUrl).removePrefix("/")
        
        val url = if (cleanPath.startsWith("releaseday") || cleanPath.startsWith("recent") || cleanPath.startsWith("popular")) {
            "$mainUrl/$cleanPath?ajax=browse_videos&page=$page"
        } else {
            if (page == 1) {
                val page1Path = cleanPath
                    .replace("recent/", "")
                    .removeSuffix("/")
                "$mainUrl/$page1Path/?ajax=1"
            } else {
                val formattedPath = cleanPath.removeSuffix("/")
                "$mainUrl/$formattedPath/$page/?ajax=1"
            }
        }

        val json = app.get(url).text
        val html = JSONObject(json).optString("html", "")
        val document = Jsoup.parse(html)

        val responseList  = document.select("div.video").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(HomePageList(request.name, responseList, isHorizontalImages = false), hasNext = true)
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title = this.select(".video-title").text()
        val href = fixUrlNull(this.select(".thumbnail").attr("href")) ?: ""
        
        val img = this.selectFirst(".video-thumb img") ?: this.selectFirst("img")
        val posterUrl = img?.attr("data-src")?.ifEmpty { null }
            ?: img?.attr("src")?.ifEmpty { null }

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String, page: Int): SearchResponseList {
        val json = app.get("$mainUrl/search/video/?s=$query&page=$page&ajax=1").text
        val html = JSONObject(json).getString("html")
        val document = Jsoup.parse(html)
        val results = document.select("div.video").mapNotNull { it.toSearchResult() }
        val hasNext = if (results.isEmpty()) false else true
        return newSearchResponseList(results, hasNext)
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        val title = document.selectFirst("meta[property=og:title]")?.attr("content")?.trim().toString()
        val poster = fixUrlNull(document.selectFirst("meta[property=og:image]")?.attr("content")
            ?: document.selectFirst("meta[name=twitter:image]")?.attr("content")
            ?: document.selectFirst("link[rel=image_src]")?.attr("href")
            ?: document.selectFirst("span.poster img")?.attr("src"))
        val description = document.selectFirst("meta[property=og:description]")?.attr("content")?.trim()
    

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(data: String, isCasting: Boolean, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit): Boolean {
        val doc = app.get(data).document
        runAllAsync(
            {
                val episodeList = doc.select(".button_style .button_choice_server")
                    episodeList.forEach { item ->
                    val link = item.attr("data-embed")
                    loadExtractor(base64Decode(link),subtitleCallback,callback)
                }
            },
            {
                getExternalSubtitile(doc, subtitleCallback)
            }
        )

        return true
    }

    suspend fun getExternalSubtitile(doc: Document, subtitleCallback: (SubtitleFile) -> Unit) {
        try {
            val title = doc.selectFirst("meta[property=og:title]")?.attr("content")?.trim().toString()
            val javCode = "([a-zA-Z]+-\\d+)".toRegex().find(title)?.groups?.get(1)?.value
            if(!javCode.isNullOrEmpty())
            {
                val query = "$subtitleCatUrl/index.php?search=$javCode"
                val subDoc = app.get(query, timeout = 15).document
                val subList = subDoc.select("td a")
                for(item in subList)
                {
                    if(item.text().contains(javCode))
                    {
                        val fullUrl = "$subtitleCatUrl/${item.attr("href")}"
                        val pDoc = app.get(fullUrl, timeout = 10).document
                        val sList = pDoc.select(".col-md-6.col-lg-4")
                        for(item in sList)
                        {
                            try {
                                val language = item.select(".sub-single span:nth-child(2)").text()
                                val text = item.select(".sub-single span:nth-child(3) a")
                                if(text.isNotEmpty() && text[0].text() == "Download")
                                {
                                    val url = "$subtitleCatUrl${text[0].attr("href")}"
                                    subtitleCallback.invoke(
                                        newSubtitleFile(
                                            language.replace("\uD83D\uDC4D \uD83D\uDC4E",""),  // Use label for the name
                                            url     // Use extracted URL
                                        )
                                    )
                                }
                            } catch (_: Exception) { }
                        }

                    }
                }

            }
        } catch (_: Exception) { }
    }
}
