package com.unan.nexxxup.AdultProvider.Asian

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.HomePageList
import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.TvType
import com.unan.nexxxup.VPNStatus
import com.unan.nexxxup.amap
import com.unan.nexxxup.app
import com.unan.nexxxup.fixUrl
import com.unan.nexxxup.fixUrlNull
import com.unan.nexxxup.mainPageOf
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.newMovieLoadResponse
import com.unan.nexxxup.newMovieSearchResponse
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.utils.loadExtractor
import org.jsoup.nodes.Element

class KRX18 : MainAPI() {
    override var mainUrl = "https://krx18.com"
    override var name = "KRX 18"
    override val hasMainPage = true
    override val hasDownloadSupport = true
    override val vpnStatus = VPNStatus.MightBeNeeded
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(
        "movies" to "Recently added",
        "genre/eng-sub" to "English SUB",
        "genre/korea" to "Korea",
        "genre/china" to "China",
        "genre/japan" to "Japan",
        "genre/thailand" to "Thailand",
        "genre/philippines" to "Philippines",
    )

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val document = app.get("$mainUrl/${request.data}/?page/$page").document
        val home = document.select("#archive-content article,div.items.normal article")
            .map { it.toSearchResult() }
        return newHomePageResponse(
            list = HomePageList(
                name = request.name,
                list = home,
                isHorizontalImages = false
            ),
            hasNext = true
        )
    }

    private fun Element.toSearchResult(): SearchResponse {
        val title = this.select("h3").text()
        val href = fixUrl(this.select("h3 a").attr("href"))
        val posterUrl = fixUrlNull(this.select("img").attr("src"))
        return newMovieSearchResponse(title, href, TvType.Movie) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/search/videos?search_query=$query").document
        val searchResponse = document.select("div.card.border-0").map { it.toSearchResult() }
        return searchResponse
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val title = document.selectFirst("div.data h1")?.text() ?: "Unknown"
        val poster =
            document.selectFirst("meta[property=og:image]")?.attr("content")?.trim() ?: "Unknown"
        val tags =
            document.select("div:nth-child(2) > div.video-details__item_links a").map { it.text() }
        val description = document.selectFirst("div.wp-content p")?.text()?.trim() ?: "Unknown"
        val recommendations = document.select("div.card.border-0").map { it.toSearchResult() }
        return newMovieLoadResponse(title, url, TvType.NSFW, url) {
            this.posterUrl = poster
            this.plot = description
            this.recommendations = recommendations
            this.tags = tags
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val req = app.get(data).document
        req.select("ul#playeroptionsul li").map {
            Triple(
                it.attr("data-post"),
                it.attr("data-nume"),
                it.attr("data-type")
            )
        }.amap { (id, nume, type) ->
            if (!nume.contains("trailer")) {
                val source = app.post(
                    url = "$mainUrl/wp-admin/admin-ajax.php",
                    data = mapOf(
                        "action" to "doo_player_ajax",
                        "post" to id,
                        "nume" to nume,
                        "type" to type
                    ),
                    referer = mainUrl,
                    headers = mapOf("X-Requested-With" to "XMLHttpRequest")
                ).parsed<ResponseHash>().embed_url
                loadExtractor(source,subtitleCallback,callback)
            }
        }
        return true
    }

    data class ResponseHash(
        @JsonProperty("embed_url") val embed_url: String,
        @JsonProperty("key") val key: String? = null,
        @JsonProperty("type") val type: String? = null,
    )
}
