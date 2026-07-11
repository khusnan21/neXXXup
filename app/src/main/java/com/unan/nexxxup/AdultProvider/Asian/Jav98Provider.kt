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
import org.jsoup.nodes.Document

class Jav98Provider : MainAPI() {
    override var mainUrl = "https://jav98.com"
    override var name = "Jav98"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        "" to "Home",
        "latest" to "Latest",
        "top-videos" to "Popular",
        "rank/30-days" to "Rank",
        "updated" to "Updated Magnet"
    )

    private fun parseSearchResult(doc: Document): Pair<List<SearchResponse>, Boolean> {
        val elements = doc.select("a[href^=/v/]")
        val items = mutableListOf<SearchResponse>()
        val addedUrls = mutableSetOf<String>()
        
        for (el in elements) {
            val href = el.attr("href")
            val fullUrl = mainUrl + href
            if (!addedUrls.add(fullUrl)) continue
            
            val imgEl = el.selectFirst("img")
            if (imgEl != null) {
                val posterUrl = imgEl.attr("src")
                val name = el.attr("title").takeIf { it.isNotEmpty() } ?: imgEl.attr("alt").takeIf { it.isNotEmpty() } ?: "Video"
                
                items.add(
                    newMovieSearchResponse(name, fullUrl, TvType.NSFW) {
                        this.posterUrl = posterUrl
                    }
                )
            }
        }
        val hasNext = doc.html().contains("下一页") // "Next page" in Chinese, or check if page=X exists, handled simply below
        return Pair(items, true)
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        var url = mainUrl
        if (request.data.isNotEmpty()) {
            url += "/${request.data}"
        }
        
        if (page > 1) {
            url += "?page=$page"
        }

        val res = app.get(url)
        val (items, _) = parseSearchResult(res.document)
        val hasNext = res.text.contains("page=${page + 1}") || res.text.contains("下一页")
        
        return newHomePageResponse(request.name, items, hasNext)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search?type=id&q=$query"
        val doc = app.get(url).document
        val (items, _) = parseSearchResult(doc)
        return items
    }

    override suspend fun load(url: String): LoadResponse {
        val res = app.get(url)
        val doc = res.document
        
        val titleEl = doc.selectFirst("title")
        val title = titleEl?.text()?.replace(" - JAV98", "") ?: url.split("/").last()
        
        val imgEl = doc.selectFirst("img[src*=/work/]")
        val posterUrl = imgEl?.attr("src")
        
        val tags = doc.select(".tag").map { it.text().trim() }.filter { it.isNotEmpty() && !it.contains("GB") }
        
        val actors = mutableSetOf<String>()
        doc.select("a[href^=/actress/]").forEach { 
            val actText = it.text().trim()
            if (actText.isNotEmpty()) actors.add(actText)
        }
        
        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = posterUrl
            this.tags = tags
            this.actors = actors.map { com.unan.nexxxup.ActorData(actor = it) }
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val res = app.get(data)
        val text = res.text.replace(Regex("\\r?\\n"), " ")
        
        val magnetRe = Regex("<span class=\"magnet-size[^\"]*\">([^<]+)</span>[\\s\\S]*?<a[^>]+href=\"(magnet:\\?xt=[^\"]+)\"[^>]*>([\\s\\S]*?)</a>")
        val matches = magnetRe.findAll(text)
        
        for (match in matches) {
            val size = match.groupValues[1].trim()
            var magnetUrl = match.groupValues[2]
            val nameText = match.groupValues[3].trim()
            val name = "$nameText ($size)"
            
            magnetUrl = magnetUrl.replace("&amp;", "&")
            
            callback.invoke(
                newExtractorLink(
                    this.name,
                    name,
                    magnetUrl,
                    "",
                    0,
                    false
                )
            )
        }
        return true
    }
}
