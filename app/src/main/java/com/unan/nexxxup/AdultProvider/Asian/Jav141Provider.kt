package com.unan.nexxxup.AdultProvider.Asian

import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.TvType
import com.unan.nexxxup.app
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.newMovieLoadResponse
import com.unan.nexxxup.newMovieSearchResponse
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.newExtractorLink
import com.unan.nexxxup.mainPageOf
import com.unan.nexxxup.fixUrl
import org.jsoup.nodes.Element

class Jav141Provider : MainAPI() {
    override var mainUrl = "https://www.141jav.com"
    override var name = "141JAV"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        "" to "Home",
        "new" to "New",
        "popular" to "Popular",
        "tag/FC2" to "FC2",
        "tag/JavPlayer" to "JavPlayer",
        "random" to "Random",
        "actress" to "Actresses"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val url = if (page <= 1) {
            if (request.data.isEmpty()) "$mainUrl/" else "$mainUrl/${request.data}"
        } else {
            if (request.data.contains("?")) {
                "$mainUrl/${request.data}&page=$page"
            } else {
                val sep = if (request.data.isEmpty()) "" else "/"
                "$mainUrl$sep${request.data}?page=$page"
            }
        }

        val doc = app.get(url).document
        val items = doc.select("div.card").mapNotNull { parseSearchResult(it) }

        return newHomePageResponse(request.name, items)
    }

    private fun parseSearchResult(el: Element): SearchResponse? {
        var a = el.selectFirst("h5.title a")
        if (a == null) a = el.selectFirst("h5.card-header a")
        if (a == null) a = el.selectFirst("a[href*=/actress/]")
        if (a == null) return null

        val href = fixUrl(a.attr("href"))
        var title: String? = null

        val titleEl = el.selectFirst("h5.title a") ?: el.selectFirst("h5.card-header a")
        if (titleEl != null) {
            title = titleEl.text()
        } else {
            val p = el.selectFirst("p.card-header-title")
            if (p != null) {
                val small = p.selectFirst("small")
                if (small != null) {
                    title = p.text().replace(small.text(), "").trim() + " (" + small.text().trim() + ")"
                } else {
                    title = p.text().trim()
                }
            }
        }
        if (title == null) return null

        val img = el.selectFirst("img.is-cover2") ?: el.selectFirst("img")
        val posterUrl = img?.let { it.attr("data-src").takeIf { src -> src.isNotEmpty() } ?: it.attr("src") }

        return newMovieSearchResponse(title, href, TvType.NSFW) {
            this.posterUrl = posterUrl?.let { fixUrl(it) }
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/${query}"
        val doc = app.get(url).document
        return doc.select("div.card").mapNotNull { parseSearchResult(it) }
    }

    override suspend fun load(url: String): LoadResponse {
        val doc = app.get(url).document
        
        val titleEl = doc.selectFirst("h5.title a") ?: doc.selectFirst("h5.card-header a")
        val title = titleEl?.text() ?: ""
        
        val img = doc.selectFirst("img.is-cover2") ?: doc.selectFirst("img")
        val posterUrl = img?.let { it.attr("data-src").takeIf { src -> src.isNotEmpty() } ?: it.attr("src") }
        
        val tags = doc.select("div.tags a.tag").map { it.text().trim() }
        
        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = posterUrl?.let { fixUrl(it) }
            this.tags = tags
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document
        val asList = doc.select("a")
        for (a in asList) {
            val href = a.attr("href")
            if (href.startsWith("magnet:")) {
                var linkName = a.text().trim()
                if (linkName.isEmpty()) linkName = "Magnet"
                callback.invoke(
                    newExtractorLink(
                        name,
                        linkName,
                        href,
                        "",
                        0,
                        false
                    )
                )
            } else if (href.lowercase().endsWith(".torrent")) {
                var linkName = a.text().trim()
                if (linkName.isEmpty()) linkName = "Torrent"
                callback.invoke(
                    newExtractorLink(
                        name,
                        linkName,
                        fixUrl(href),
                        "",
                        0,
                        false
                    )
                )
            }
        }
        return true
    }
}
