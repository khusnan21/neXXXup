package com.unan.nexxxup.AdultProvider.Asian

import com.unan.nexxxup.HomePageList
import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.AdultProvider.Asian.KingBokepUtils.pageUrl
import com.unan.nexxxup.AdultProvider.Asian.KingBokepUtils.searchUrl
import com.unan.nexxxup.AdultProvider.Asian.KingBokepUtils.cleanLoadUrl
import com.unan.nexxxup.AdultProvider.Asian.KingBokepUtils.posterFromLoadUrl
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.TvType
import com.unan.nexxxup.app
import com.unan.nexxxup.mainPageOf
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.utils.ExtractorLink





class KingBokepProvider : MainAPI() {
    override var mainUrl = KingBokepSeeds.MAIN_URL
    override var name = "KingBokep"
    override var lang = "id"
    override val hasMainPage = true
    override val hasQuickSearch = true
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = mainPageOf(*KingBokepSeeds.mainPageRows())

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = pageUrl(mainUrl, request.data, page)
        val document = app.get(url, headers = KingBokepUtils.siteHeaders).document
        val results = KingBokepParser.parseListing(this, document)
        return newHomePageResponse(listOf(HomePageList(request.name, results, isHorizontalImages = true)))
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = searchUrl(mainUrl, query)
        val document = app.get(url, headers = KingBokepUtils.siteHeaders).document
        return KingBokepParser.parseListing(this, document)
    }

    override suspend fun load(url: String): LoadResponse? {
        val cleanUrl = cleanLoadUrl(url)
        val document = app.get(cleanUrl, headers = KingBokepUtils.siteHeaders, referer = mainUrl).document
        return KingBokepParser.parseLoadResponse(this, cleanUrl, document, posterFromLoadUrl(url))
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        return KingBokepExtractor.loadLinks(name, mainUrl, data, subtitleCallback, callback)
    }
}
