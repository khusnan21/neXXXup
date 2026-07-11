package com.lagradost.cloudstream3

import org.junit.Test
import org.jsoup.Jsoup
import java.io.File

class VlxxTest {
    @Test
    fun fetch() {
        var output = "testing vlxx.moi..."
        try {
            var doc = Jsoup.connect("https://vlxx.moi").get()
            output += "Main page title: " + doc.title() + "\n"
            var elements = doc.select("div#video-list > div.video-item a")
            output += "Main page elements a: " + elements.size + " :: " + elements.map { it.attr("href") }.take(2) + "\n"
            
            doc = Jsoup.connect("https://vlxx.moi/search/mom/").get()
            output += "Search HTML Snippet: " + doc.html().take(1500) + "\n"
            elements = doc.select(".video-item a")
            output += "Search elements a via .video-item a: " + elements.size + " :: " + elements.map { it.attr("href") }.take(2) + "\n"
            
            val firstUrl = doc.select("div.video-item a[href*=/video/]").firstOrNull()?.attr("href") ?: elements.firstOrNull()?.attr("href")
            output += windowUrl(firstUrl)
        } catch (e: Exception) {
            output += e.toString()
            e.printStackTrace()
        }
        File("vlxx_out.txt").writeText(output)
    }
    
    fun windowUrl(firstUrl: String?): String = kotlinx.coroutines.runBlocking {
        var output = ""
        if (firstUrl != null) {
            val resolvedUrl = if (firstUrl.startsWith("http")) firstUrl else "https://vlxx.moi" + firstUrl
            output += "Loading: " + resolvedUrl + "\n"
            val loadDoc = app.get(resolvedUrl).document
            output += "Load title: " + loadDoc.title() + "\n"
            output += "container H2: " + loadDoc.select("div#container h2").text() + "\n"
            
            // test loadLinks
            val pathSplits = resolvedUrl.split("/")
            val id = pathSplits[pathSplits.size - 2]
            output += "ajax ID: $id\n"
            
            val text = app.post("https://vlxx.moi/ajax.php", 
                headers = mapOf("X-Requested-With" to "XMLHttpRequest"),
                data = mapOf(
                    "vlxx_server" to "1",
                    "id" to id,
                    "server" to "1"
                ),
                referer = resolvedUrl
            ).text
            output += "Ajax Response Text (first 500): " + text.take(500) + "\n"
            
            val iframeMatch = Regex("""src=\\"(.*?)\\"""").find(text)
            val iframeUrl = iframeMatch?.groupValues?.getOrNull(1)?.replace("\\", "")
            output += "iframeUrl: $iframeUrl\n"
            
            if (iframeUrl != null) {
                val playerConfig = app.get(iframeUrl, referer = "https://vlxx.moi").text
                val scripts = Regex("""<script[^>]*>(.*?)</script>""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)).findAll(playerConfig).toList()
                val inlineScripts = scripts.map { it.groupValues.getOrNull(1) }.filter { !it.isNullOrBlank() }
                output += "Inline scripts:\n" + inlineScripts.joinToString("\n===\n") + "\n"
                
                val m3u8Match = Regex("""file:\s*["'](.*?)["']""").find(playerConfig)
                output += "m3u8: " + m3u8Match?.groupValues?.getOrNull(1) + "\n"
            }
        }
        return@runBlocking output
    }
    
    private fun getParamFromJS(str: String, key: String, keyEnd: String): String? {
        try {
            val firstIndex = str.indexOf(key) + key.length // 4 to index point to first char.
            val temp = str.substring(firstIndex)
            val lastIndex = temp.indexOf(keyEnd) + (keyEnd.length)
            val jsonConfig = temp.substring(0, lastIndex) //
            return jsonConfig.replace("\\r", "").replace("\\t", "").replace("\\\"", "\"").replace("\\\\\\/", "/").replace("\\n", "")
        } catch (e: Exception) {
            return null
        }
    }
}
