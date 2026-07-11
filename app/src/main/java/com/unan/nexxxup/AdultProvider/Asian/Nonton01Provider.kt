package com.unan.nexxxup.AdultProvider.Asian

import com.unan.nexxxup.HomePageList
import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.AdultProvider.Asian.Nonton01Utils.pageUrls
import com.unan.nexxxup.AdultProvider.Asian.Nonton01Utils.searchUrl
import com.unan.nexxxup.AdultProvider.Asian.Nonton01Utils.mirrorUrlsFor
import com.unan.nexxxup.AdultProvider.Asian.Nonton01Utils.siteHeadersFor
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.TvType
import com.unan.nexxxup.app
import com.unan.nexxxup.mainPageOf
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.utils.ExtractorLink





class Nonton01Provider : MainAPI() {
    override var mainUrl = Nonton01Seeds.MAIN_URL
    override var name = "Nonton01"
    override var lang = "id"
    override val hasMainPage = true
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.NSFW)

    override val mainPage = mainPageOf(*Nonton01Seeds.mainPageRows())

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val results = firstNonEmptyPageResult(pageUrls(mainUrl, request.data, page))
        return if (results.isNotEmpty()) {
            newHomePageResponse(listOf(HomePageList(request.name, results)))
        } else {
            newHomePageResponse(emptyList())
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val candidates = listOf(
            searchUrl(Nonton01Seeds.MAIN_URL, query),
            searchUrl(Nonton01Seeds.SOURCE_URL, query)
        ).distinct()
        return firstNonEmptyPageResult(candidates)
    }

    override suspend fun quickSearch(query: String): List<SearchResponse>? = search(query)

    override suspend fun load(url: String): LoadResponse? {
        for (candidate in mirrorUrlsFor(url)) {
            val response = runCatching {
                val origin = Nonton01Utils.originOf(candidate) ?: mainUrl
                val document = app.get(candidate, headers = siteHeadersFor(origin), referer = origin).document
                Nonton01Parser.parseLoadResponse(this, candidate, document)
            }.getOrNull()
            if (response != null) return response
        }
        return null
    }

    private suspend fun firstNonEmptyPageResult(urls: List<String>): List<SearchResponse> {
        for (url in urls) {
            val results = runCatching {
                val origin = Nonton01Utils.originOf(url) ?: mainUrl
                val document = app.get(url, headers = siteHeadersFor(origin), referer = origin).document
                Nonton01Parser.parseListing(this, document)
            }.getOrElse { emptyList() }
            if (results.isNotEmpty()) return results
        }
        return emptyList()
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return Nonton01Extractor.loadLinks(name, data, subtitleCallback, callback)
    }
}
