package com.unan.nexxxup

import org.junit.Test
import org.jsoup.Jsoup

class JavriderTest {
    @Test
    fun fetch() = kotlinx.coroutines.runBlocking {
        val doc = Jsoup.connect("https://javrider.id").get()
        val allUrls = doc.select("a[href]").map { it.attr("href") }
        java.io.File("/app/urls.txt").writeText(allUrls.joinToString("\n"))
        val firstUrl = allUrls.firstOrNull { it.contains("javrider.id") && !it.contains("202") && !it.contains("category/") && !it.contains("page/") && !it.contains("tag/") && it.count { ch -> ch == '/' } > 3 }
        
        var output = "FIRST_URL: $firstUrl\n"
        if (firstUrl != null) {
            val page = Jsoup.connect(firstUrl).get()
            val playerIframe = page.select("iframe[src]").map { it.attr("src") }.firstOrNull { it.contains("player") }
            output += "PLAYER_IFRAME: $playerIframe\n"
            if (playerIframe != null) {
                val playerPage = app.get(playerIframe).document
                output += "JAVPLAYERS SCRIPTS: " + playerPage.select("script").map { it.html() }.joinToString("\n======\n") + "\n"
            }
        }
        val outputStr = output
        java.io.File("test_output.txt").writeText(outputStr)
    }
}
