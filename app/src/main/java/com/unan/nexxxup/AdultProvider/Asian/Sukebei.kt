package com.unan.nexxxup.AdultProvider.Asian

import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.TvType
import com.unan.nexxxup.app
import com.unan.nexxxup.mainPageOf
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.newMovieLoadResponse
import com.unan.nexxxup.newMovieSearchResponse
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.ExtractorLinkType
import com.unan.nexxxup.utils.Qualities
import org.jsoup.nodes.Element

class Sukebei : MainAPI() {
    override var mainUrl = "https://sukebei.nyaa.si"
    override var name = "Sukebei"
    override val hasMainPage = true
    override var lang = "ja"
    override val supportedTypes = setOf(TvType.NSFW, TvType.Torrent)

    override val mainPage = mainPageOf(
        "?f=0&c=0_0&q=" to "Latest Sukebei Releases",
        "?f=0&c=0_0&s=seeders&o=desc&q=" to "Most popular"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}&p=$page").document
        val items = document.select("table.table tbody tr").mapNotNull {
            toSearchResponse(it)
        }
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/?f=0&c=0_0&q=$query").document
        return document.select("table.table tbody tr").mapNotNull {
            toSearchResponse(it)
        }
    }

    private fun toSearchResponse(element: Element): SearchResponse? {
        val titleNode = element.selectFirst("td[colspan=2] a:not(.comments)") ?: return null
        val title = titleNode.attr("title").ifEmpty { titleNode.text() }
        val id = titleNode.attr("href")

        val seedersElem = element.select("td.text-center").getOrNull(2)
        val seeders = seedersElem?.text()?.toIntOrNull()
        
        return newMovieSearchResponse(title, id, TvType.Movie) {
            this.posterUrl = "" // Sukebei typically doesn't have posters in list view
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val fullUrl = if (url.startsWith("http")) url else "$mainUrl$url"
        val document = app.get(fullUrl).document
        val panel = document.selectFirst("div.panel.panel-default") ?: return null
        val title = panel.selectFirst("div.panel-heading h3.panel-title")?.text() ?: "Unknown"

        val magnet = document.selectFirst("div.panel-footer a[href^=magnet:]")?.attr("href") ?: return null
        
        val desc = panel.selectFirst("div#torrent-description")
        val poster = desc?.selectFirst("img")?.attr("src")

        return newMovieLoadResponse(title, fullUrl, TvType.Movie, magnet) {
            this.posterUrl = poster
            this.plot = desc?.text()
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val trackers = listOf(
            "http://sukebei.tracker.wf:8888/announce",
            "http://t.overflow.biz:6969/announce",
            "http://tracker.bt4g.com:2095/announce",
            "https://1337.abcvg.info:443/announce",
            "https://tracker1.520.jp:443/announce",
            "udp://208.83.20.20:6969/announce",
            "udp://89.234.156.205:451/announce",
            "udp://93.158.213.92:1337/announce",
            "udp://exodus.desync.com:6969/announce",
            "udp://open.stealth.si:80/announce",
            "udp://tracker.opentrackr.org:1337/announce",
            "udp://tracker.torrent.eu.org:451/announce"
        )
            
        var magnetUri = data
        trackers.forEach { tr ->
             if (!magnetUri.contains(tr)) {
                 magnetUri += "&tr=" + java.net.URLEncoder.encode(tr, "UTF-8")
             }
        }

        callback.invoke(
            ExtractorLink(
                name,
                "Torrent",
                magnetUri,
                referer = "",
                quality = Qualities.Unknown.value,
                type = ExtractorLinkType.MAGNET
            )
        )
        return true
    }
}
