package com.unan.nexxxup.AdultProvider.Asian

import com.unan.nexxxup.HomePageResponse
import com.unan.nexxxup.LoadResponse
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.MainPageRequest
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.SearchResponseList
import com.unan.nexxxup.TvType
import com.unan.nexxxup.app
import com.unan.nexxxup.mainPage
import com.unan.nexxxup.newHomePageResponse
import com.unan.nexxxup.newMovieLoadResponse
import com.unan.nexxxup.newSearchResponseList
import com.unan.nexxxup.utils.ExtractorLink
import com.unan.nexxxup.SubtitleFile

class PasarBokepProvider : MainAPI() {
    override var mainUrl = "https://pasarbokep.com"
    override var name = "PasarBokep"
    override var lang = "id"

    override val hasMainPage = true
    override val hasDownloadSupport = true
    override val hasChromecastSupport = true
    override var sequentialMainPage = true
    override var sequentialMainPageDelay = 150L
    override val supportedTypes = setOf(TvType.NSFW)

    override val mainPage = PasarBokepSeeds.mainPage.map { category ->
        mainPage(category.path, category.name, category.horizontalImages)
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = PasarBokepUtils.pagedUrl(request.data, page, mainUrl)
        val document = app.get(url, headers = PasarBokepUtils.headers, referer = mainUrl).document
        val list = PasarBokepParser.parseCards(document, this, scoped = request.data.contains("/category/"))
        return newHomePageResponse(request.name, list, hasNext = document.hasNextPage())
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encoded = PasarBokepUtils.encodeQuery(query)
        val document = app.get("$mainUrl/?s=$encoded", headers = PasarBokepUtils.headers, referer = mainUrl).document
        return PasarBokepParser.parseCards(document, this)
    }

    override suspend fun search(query: String, page: Int): SearchResponseList? {
        val encoded = PasarBokepUtils.encodeQuery(query)
        val url = if (page <= 1) "$mainUrl/?s=$encoded" else "$mainUrl/page/$page/?s=$encoded"
        val document = app.get(url, headers = PasarBokepUtils.headers, referer = mainUrl).document
        return newSearchResponseList(PasarBokepParser.parseCards(document, this), hasNext = document.hasNextPage())
    }

    override suspend fun load(url: String): LoadResponse {
        val fixedUrl = PasarBokepUtils.updateHost(url, mainUrl)
        val document = app.get(fixedUrl, headers = PasarBokepUtils.headers, referer = mainUrl).document

        val title = PasarBokepUtils.cleanText(
            document.selectFirst("h1.entry-title, h1.post-title, article h1, main h1, h1")?.text()
        ).ifBlank { PasarBokepUtils.titleFromUrl(fixedUrl) }

        val poster = document.bestPoster(mainUrl)
        val plot = PasarBokepParser.parsePlot(document)
        val tags = PasarBokepParser.parseTags(document)
        val recommendations = PasarBokepParser.parseCards(document, this)
            .filterNot { it.url == fixedUrl }
            .take(12)

        return newMovieLoadResponse(title, fixedUrl, TvType.NSFW, fixedUrl) {
            posterUrl = poster
            this.plot = plot
            this.tags = tags
            this.recommendations = recommendations
            contentRating = "18+"
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit,
    ): Boolean {
        val fixedUrl = PasarBokepUtils.updateHost(data, mainUrl)
        return PasarBokepExtractor.resolve(
            pageUrl = fixedUrl,
            mainUrl = mainUrl,
            subtitleCallback = subtitleCallback,
            callback = callback,
        )
    }
}
