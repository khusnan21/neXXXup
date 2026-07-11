package com.unan.nexxxup.AdultProvider.Webcam

import com.unan.nexxxup.*
import com.unan.nexxxup.utils.AppUtils.parseJson
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.Qualities
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.newExtractorLink
import org.jsoup.nodes.Element

class Cipok : MainAPI() {
    override var mainUrl = "https://cipok5.com"
    override var name = "Cipok"
    override val hasMainPage = true
    override var lang = "id"
    override val hasDownloadSupport = false
    override val supportedTypes = setOf(TvType.Live, TvType.NSFW)

    override val mainPage = mainPageOf(
        "\$mainUrl/home" to "Home",
        "\$mainUrl/liveCenter" to "Live Center",
        "\$mainUrl/liveCountry?areaCode=ID&name=Indonesia" to "Indonesia",
        "\$mainUrl/liveCountry?areaCode=VN&name=Vietnam" to "Vietnam"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val url = request.data
        // Because cipok is likely a SPA (Vue/React), you might need to query their JSON API directly
        // This is a placeholder standard JSOUP scraping logic, you might need to change it
        // to call the real API endpoints depending on what the web browser actually calls.
        val document = app.get(url).document

        val items = document.select("div.room-item, li.live-item, a.item").mapNotNull {
            it.toSearchResult()
        }

        return newHomePageResponse(request.name, items)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val title = this.selectFirst("div.title, span.name")?.text() ?: return null
        val href = fixUrlNull(this.selectFirst("a")?.attr("href") ?: this.attr("href")) ?: return null
        val posterUrl = fixUrlNull(this.selectFirst("img")?.attr("src") ?: this.selectFirst("img")?.attr("data-src"))

        return newMovieSearchResponse(title, href, TvType.Live) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("\$mainUrl/search?keyword=\$query").document
        return document.select("div.room-item, li.live-item, a.item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("h1.title, div.room-title")?.text() ?: "Live Stream"
        val poster = fixUrlNull(document.selectFirst("img.poster")?.attr("src"))

        return newLiveStreamLoadResponse(
            name = title,
            url = url,
            dataUrl = url
        ) {
            this.posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // Find M3U8 inside the HTML or fetch API
        val document = app.get(data).document
        val html = document.html()
        
        // Example simple regex to find m3u8 matching
        val m3u8Regex = Regex("[\"'](http[^\"']+\\.m3u8[^\"']*)[\"']")
        val m3u8Url = m3u8Regex.find(html)?.groupValues?.get(1)
        
        if (m3u8Url != null) {
            callback.invoke(
                newExtractorLink(
                    source = this.name,
                    name = this.name,
                    url = m3u8Url.replace("\\/", "/"),
                    type = ExtractorLinkType.M3U8
                ) {
                    this.referer = this@Cipok.mainUrl
                    this.quality = Qualities.Unknown.value
                }
            )
            return true
        }

        return false
    }
}

