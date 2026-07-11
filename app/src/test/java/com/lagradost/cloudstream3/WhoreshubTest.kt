package com.lagradost.cloudstream3

import org.junit.Test
import org.jsoup.Jsoup
import java.io.File

class WhoreshubTest {
    @Test
    fun fetch() {
        var output = "testing..."
        try {
            var doc = Jsoup.connect("https://www.whoreshub.com/latest-updates/1/").get()
            output += "Main page title: " + doc.title() + "\n"
            var elements = doc.select("div.block-thumbs a.item")
            output += "Main page elements a.item: " + elements.size + " :: " + elements.map { it.attr("href") }.take(2) + "\n"
            
            val firstUrl = elements.firstOrNull()?.attr("href")
            output += windowUrl(firstUrl)
        } catch (e: Exception) {
            output += e.toString()
            e.printStackTrace()
        }
        File("whoreshub_out.txt").writeText(output)
    }
    
    fun windowUrl(firstUrl: String?): String {
        var output = "testing..."
        if (firstUrl != null) {
            val resolvedUrl = if (firstUrl.startsWith("http")) firstUrl else "https://www.whoreshub.com" + firstUrl
            output += "Loading: " + resolvedUrl + "\n"
            val loadDoc = Jsoup.connect(resolvedUrl).get()
            output += "Load title: " + loadDoc.title() + "\n"
            val scripts = loadDoc.select("script")
            output += "Scripts with video_url: " + scripts.filter { it.html().contains("video_url") }.map { it.html().take(100) } + "\n"
            
            // let's actually try the regex
            val docText = loadDoc.toString()
            val regex = Regex("""video_(?:url|alt_url(?:2|3)?):\s*'(https://[^']+)'""")
            val links = regex.findAll(docText).map { it.groupValues[1] }.toList()
            output += "Matched Links: " + links + "\n"
        }
        return output
    }
}
