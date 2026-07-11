package com.lagradost.cloudstream3.AdultProvider.Asian

import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.newMovieLoadResponse
import com.lagradost.cloudstream3.newMovieSearchResponse
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.mainPageOf
import org.json.JSONObject

class UncenXProvider : MainAPI() {
    override var mainUrl = "https://www.uncenx.com"
    override var name = "UncenX"
    override val hasMainPage = true
    override var lang = "id"
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        "" to "Home",
        "trending" to "Trending",
        "popular" to "Popular"
    )

    private fun extractVideos(text: String): List<SearchResponse> {
        var cleanText = text.replace(Regex("\"]\\)[^<]*</script>[^<]*<script>[^\"]*self\\.__next_f\\.push\\(\\[1,\"/"), "")
        cleanText = cleanText.replace("\\\"", "\"").replace("\\\\", "\\")
        
        val items = mutableListOf<SearchResponse>()
        val mapItems = mutableSetOf<String>()
        val blockRe = Regex("\"id\":(\\d+),\"movie_code\":\"([^\"]+)\",\"slug\":\"([^\"]+)\",\"title\":\"([^\"]*)\",\"title_th\":(?:\"([^\"]*)\"|null),\"title_zh\":(?:\"([^\"]*)\"|null).*?(?:\"poster_path\":\"([^\"]*)\").*?(?:\"thumb_path\":\"([^\"]*)\"|null)")
        
        val matches = blockRe.findAll(cleanText)
        for (match in matches) {
            val id = match.groupValues[1]
            if (mapItems.add(id)) {
                val titleTh = match.groupValues[5]
                val title = match.groupValues[4]
                val movieCode = match.groupValues[2]
                val slug = match.groupValues[3]
                val poster1 = match.groupValues[7]
                val poster2 = match.groupValues[8]
                
                val finalTitle = titleTh.takeIf { it.isNotEmpty() } ?: title.takeIf { it.isNotEmpty() } ?: movieCode
                items.add(
                    newMovieSearchResponse(finalTitle, "$mainUrl/$slug", TvType.NSFW) {
                        this.posterUrl = poster1.takeIf { it.isNotEmpty() } ?: poster2.takeIf { it.isNotEmpty() } ?: ""
                    }
                )
            }
        }
        return items
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        var url = mainUrl
        if (request.data.isNotEmpty()) url += "/${request.data}"
        if (page > 1) {
            url += "?page=$page"
        }
        
        val res = app.get(url).text
        val items = extractVideos(res)
        
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/search/$query"
        val res = app.get(url).text
        return extractVideos(res)
    }

    override suspend fun load(url: String): LoadResponse {
        val res = app.get(url).text
        
        val titleMatch = Regex("<title>([^<]+)</title>").find(res)
        val title = titleMatch?.groupValues?.get(1)?.replace(" | UNCEN X JAV ไม่เซ็นเท่านั้น", "") ?: url.split("/").last()
        
        var posterUrl: String? = null
        val ogMatch = Regex("og:image\" content=\"([^\"]+)\"").find(res)
        if (ogMatch != null) {
            posterUrl = ogMatch.groupValues[1]
            val urlMatch = Regex("url=([^&]+)").find(posterUrl)
            if (urlMatch != null) {
                posterUrl = java.net.URLDecoder.decode(urlMatch.groupValues[1], "UTF-8")
            }
        }
        
        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val slugMatch = Regex("([^/]+)$").find(data) ?: return false
        val slug = slugMatch.groupValues[1]
        
        val apiUrl = "$mainUrl/api/player/$slug"
        val res = app.get(apiUrl).text
        
        var embedUrl: String? = null
        try {
            val obj = JSONObject(res)
            embedUrl = obj.optString("url")
        } catch (e: Exception) {}
        
        if (embedUrl.isNullOrEmpty()) return false
        
        val embedRes = app.get(embedUrl).text
        val m3u8Match = Regex("\"(https?://[^\"]+\\.m3u8[^\"]*)\"").find(embedRes)
        val mp4Match = Regex("\"(https?://[^\"]+\\.mp4[^\"]*)\"").find(embedRes)
        
        if (m3u8Match != null) {
            callback.invoke(
                ExtractorLink(source = name, name = "HLS", url = m3u8Match.groupValues[1], referer = "", quality = 0, type = com.lagradost.cloudstream3.utils.ExtractorLinkType.VIDEO)
            )
        } else if (mp4Match != null) {
            callback.invoke(
                ExtractorLink(source = name, name = "MP4", url = mp4Match.groupValues[1], referer = "", quality = 0, type = com.lagradost.cloudstream3.utils.ExtractorLinkType.VIDEO)
            )
        }
        
        return true
    }
}
