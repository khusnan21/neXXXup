package com.lagradost.cloudstream3.AdultProvider.Asian

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor

class JavNinja : MainAPI() {
    override var mainUrl = "https://jav.ninja"
    override var name = "JavNinja"
    override val hasMainPage = true
    override var lang = "en"
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        "$mainUrl/en" to "Home",
        "$mainUrl/en/videos?filter=new" to "Newest",
        "$mainUrl/en/videos?filter=popular&range=month" to "Popular (Month)",
        "$mainUrl/en/videos?filter=popular&range=year" to "Popular (Year)",
        "$mainUrl/en/videos?filter=popular&range=all" to "Popular (All Time)"
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        var url = request.data
        if (page > 1) {
            url += if (url.contains("?")) "&page=$page" else "?page=$page"
        }

        val doc = app.get(url).document
        val items = mutableListOf<SearchResponse>()

        doc.select(".video-card a").forEach { el ->
            val href = el.attr("href")
            val fullUrl = if (href.startsWith("http")) href else mainUrl + href
            
            val imgEl = el.select("img").lastOrNull()
            var poster = imgEl?.attr("src")?.substringBefore("?") ?: ""
            if (poster.startsWith("/")) {
                poster = mainUrl + poster
            }
            
            val titleEl = el.selectFirst("span.hover\\:underline")
            val title = titleEl?.text()?.trim() ?: imgEl?.attr("alt")?.trim() ?: "Video"

            if (href.isNotBlank()) {
                items.add(
                    newMovieSearchResponse(title, fullUrl, TvType.NSFW) {
                        this.posterUrl = poster
                    }
                )
            }
        }

        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = items
            ),
            hasNext = items.isNotEmpty() // Simple pagination assumption
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/en/videos?search=$query"
        val doc = app.get(url).document
        val items = mutableListOf<SearchResponse>()

        doc.select(".video-card a").forEach { el ->
            val href = el.attr("href")
            val fullUrl = if (href.startsWith("http")) href else mainUrl + href
            
            val imgEl = el.select("img").lastOrNull()
            var poster = imgEl?.attr("src")?.substringBefore("?") ?: ""
            if (poster.startsWith("/")) {
                poster = mainUrl + poster
            }
            
            val titleEl = el.selectFirst("span.hover\\:underline")
            val title = titleEl?.text()?.trim() ?: imgEl?.attr("alt")?.trim() ?: "Video"

            if (href.isNotBlank()) {
                items.add(
                    newMovieSearchResponse(title, fullUrl, TvType.NSFW) {
                        this.posterUrl = poster
                    }
                )
            }
        }
        return items
    }

    override suspend fun load(url: String): LoadResponse {
        val res = app.get(url)
        val doc = res.document
        
        val title = doc.selectFirst("meta[property=og:title]")?.attr("content")
            ?: doc.selectFirst("title")?.text()?.replace("- JavNinja", "")?.trim()
            ?: "Video"
            
        var poster = doc.selectFirst("meta[property=og:image]")?.attr("content")
        if (poster?.startsWith("/") == true) {
            poster = mainUrl + poster
        }
        
        val description = doc.selectFirst("meta[name=description]")?.attr("content")

        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot = description
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val text = app.get(data).text
        val streamtapeRegex = Regex("""streamtapeUrl:"(https?://streamtape\.com/[^"]+)"""")
        val match = streamtapeRegex.find(text)
        
        match?.groupValues?.get(1)?.let { streamUrl ->
            loadExtractor(streamUrl, data, subtitleCallback, callback)
        }
        
        return true
    }
}
