package com.unan.nexxxup

import org.junit.Test
import java.net.URL

class ProvidersTest {
    @Test
    fun testJavHD() {
        try {
            val result = StringBuilder()
            val categories = listOf(
                "/eng-sub-jav/popular/year/ page 1" to "https://javhd.today/eng-sub-jav/popular/year/?ajax=1",
                "/eng-sub-jav/popular/year/ page 2" to "https://javhd.today/eng-sub-jav/popular/year/2/?ajax=1"
            )
            for (cat in categories) {
                try {
                    val url = cat.second
                    val connection = java.net.URL(url).openConnection()
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    val jsonText = connection.getInputStream().bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(jsonText)
                    val html = json.optString("html", "")
                    val doc = org.jsoup.Jsoup.parse(html)
                    val videos = doc.select("div.video")
                    result.append("Category ${cat.first}: found ${videos.size} videos via AJAX\n")
                    if (videos.isNotEmpty()) {
                        val title = videos[0].select(".video-title").text()
                        val poster = videos[0].selectFirst(".video-thumb img")?.attr("src") ?: videos[0].selectFirst("img")?.attr("src")
                        result.append("  First video title: $title\n")
                        result.append("  First video poster: $poster\n")
                    }
                } catch (inner: Exception) {
                    result.append("Category ${cat.first} FAILED: ${inner.message} (${cat.second})\n")
                }
            }
            val outText = result.toString()
            java.io.File("javhd_test_msg.txt").writeText(outText)
            java.io.File("/app/javhd_test.txt").writeText(outText)
            println("Successfully wrote results of length ${outText.length}")
        } catch (e: Exception) {
            val errorMsg = "Error in testJavHD: ${e.message}\n${e.stackTraceToString()}"
            println(errorMsg)
            java.io.File("javhd_test_msg.txt").writeText(errorMsg)
            java.io.File("/app/javhd_test.txt").writeText(errorMsg)
        }
    }

    @Test
    fun testJavSubIndo() {
        try {
            val result = StringBuilder()
            val mainUrl = "https://javsubindo.life"
            val url = mainUrl
            result.append("Connecting to $url...\n")
            val connection = java.net.URL(url).openConnection()
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            val html = connection.getInputStream().bufferedReader().use { it.readText() }
            result.append("Received HTML of length ${html.length}\n")
            val doc = org.jsoup.Jsoup.parse(html)
            
            val articles = doc.select("article.loop-video.thumb-block, article.loop-video, article.thumb-block")
            result.append("Found ${articles.size} primary articles\n")
            
            val genericFallback = doc.select("article:has(a[href]):has(img), .post:has(a[href]):has(img), a[href]:has(img)")
            result.append("Found ${genericFallback.size} generic fallback articles\n")
            
            val allArticles = if (articles.isNotEmpty()) articles else genericFallback
            var firstHref = ""
            for ((idx, art) in allArticles.take(5).withIndex()) {
                val anchor = if (art.`is`("a[href]")) art else art.selectFirst("a[href][title], a[href]:has(img), h2 a[href], h3 a[href], .entry-header a[href], a[href]")
                val href = anchor?.attr("href")
                if (firstHref.isEmpty() && href != null) {
                    firstHref = href
                }
                val img = art.selectFirst("img") ?: anchor?.selectFirst("img")
                val poster = img?.attr("src") ?: img?.attr("data-src")
                val title = img?.attr("alt") ?: anchor?.attr("title") ?: anchor?.text()
                result.append("  [#$idx] Title: $title, Link: $href, Poster: $poster\n")
            }
            
            if (firstHref.isNotEmpty()) {
                result.append("\n--- ANALYZING VIDEO POST PAGE: $firstHref ---\n")
                val postConn = java.net.URL(firstHref).openConnection()
                postConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                val postHtml = postConn.getInputStream().bufferedReader().use { it.readText() }
                result.append("Post HTML length: ${postHtml.length}\n")
                
                val postDoc = org.jsoup.Jsoup.parse(postHtml)
                
                // Find all iframe elements
                val iframes = postDoc.select("iframe")
                result.append("Found ${iframes.size} iframes:\n")
                iframes.forEachIndexed { i, iframe ->
                    result.append("  Iframe #$i: src='${iframe.attr("src")}', data-src='${iframe.attr("data-src")}', outerHTML='${iframe.outerHtml()}'\n")
                }
                
                // Find all script elements
                val scripts = postDoc.select("script")
                result.append("Found ${scripts.size} script tags.\n")
                
                // Inspect players or option buttons (tabs)
                val players = postDoc.select(".player, [class*=player], [id*=player]")
                result.append("Found ${players.size} elements matching class/id option player:\n")
                players.forEachIndexed { i, player ->
                    result.append("  Player tag #$i: tag='${player.tagName()}', class='${player.className()}', id='${player.id()}', outerHTML='${player.outerHtml()}'\n")
                }
                
                // Inspect download / player buttons
                val sourceButtons = postDoc.select(".server, .button, .option-players, [id*=server], [class*=server], [id*=option], [class*=option]")
                result.append("Found ${sourceButtons.size} optional source element tags:\n")
                sourceButtons.take(20).forEachIndexed { i, btn ->
                    result.append("  Option #$i: tag='${btn.tagName()}', class='${btn.className()}', id='${btn.id()}', text='${btn.text()}', outerHTML='${btn.outerHtml()}'\n")
                }

                // Check all onclick attributes or whole HTML for atob() decoded URLs
                val decodedUrls = mutableListOf<String>()
                val atobRegex = Regex("""(?i)atob\(['"]([A-Za-z0-9+/=]+)['"]\)""")
                atobRegex.findAll(postHtml).forEach { match ->
                    val base64Str = match.groupValues[1]
                    try {
                        val decoded = String(java.util.Base64.getDecoder().decode(base64Str))
                        decodedUrls.add(decoded)
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                result.append("Found ${decodedUrls.size} atob decoded URLs in entire HTML:\n")
                decodedUrls.forEach { urlCandidate ->
                    result.append("  Decoded: '$urlCandidate'\n")
                }

                // Let's fetch one of the StreamVid page HTMLs and inspect it!
                val streamVidUrl = decodedUrls.find { it.contains("streamvid.dev") }
                if (streamVidUrl != null) {
                    result.append("\n--- FETCHING AND ANALYZING STREAMVID: $streamVidUrl ---\n")
                    try {
                        val svConn = java.net.URL(streamVidUrl).openConnection()
                        svConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        svConn.setRequestProperty("Referer", firstHref)
                        val svHtml = svConn.getInputStream().bufferedReader().use { it.readText() }
                        result.append("StreamVid HTML length: ${svHtml.length}\n")

                        // Look for .m3u8, .mp4, video, source tags, script variables
                        val m3u8_matches = Regex("""https?://[^\s'"<>\\]+\.m3u8[^\s'"<>\\]*""").findAll(svHtml).map { it.value }.toList()
                        val mp4_matches = Regex("""https?://[^\s'"<>\\]+\.mp4[^\s'"<>\\]*""").findAll(svHtml).map { it.value }.toList()
                        result.append("Found ${m3u8_matches.size} m3u8 URLs in StreamVid HTML:\n")
                        m3u8_matches.forEach { result.append("  M3U8: '$it'\n") }
                        result.append("Found ${mp4_matches.size} mp4 URLs in StreamVid HTML:\n")
                        mp4_matches.forEach { result.append("  MP4: '$it'\n") }

                        // Parse the HTML
                        val svDoc = org.jsoup.Jsoup.parse(svHtml)
                        val svSources = svDoc.select("video, source, iframe")
                        result.append("Found ${svSources.size} sources in StreamVid:\n")
                        svSources.forEach { elem ->
                            result.append("  Source element: tag='${elem.tagName()}', src='${elem.attr("src")}', data-src='${elem.attr("data-src")}', outer='${elem.outerHtml()}'\n")
                        }

                        // Let's print any scripting block containing player or source variables
                        val svScripts = svDoc.select("script")
                        result.append("Found ${svScripts.size} scripts in StreamVid:\n")
                        svScripts.forEachIndexed { sIdx, script ->
                            val sText = script.html()
                            if (sText.contains("player", ignoreCase = true) || sText.contains("eval", ignoreCase = true) || sText.contains("sources", ignoreCase = true) || sText.contains("file", ignoreCase = true)) {
                                result.append("  Script #$sIdx density (length=${sText.length}):\n")
                                val linesWithKeywords = sText.lines().filter { l -> l.contains("file", ignoreCase = true) || l.contains("source", ignoreCase = true) || l.contains("player", ignoreCase = true) || l.contains("eval", ignoreCase = true) || l.contains("packed", ignoreCase = true) }
                                linesWithKeywords.take(15).forEach { l ->
                                    result.append("    Line: ${l.trim().take(150)}\n")
                                }
                            }
                        }

                        // Also lets check if there is some packer or eval
                        if (svHtml.contains("eval(function(p,a,c,k,e,")) {
                            result.append("  Detected packed javascript!\n")
                            // Find the script tag containing packed script
                            val packedScript = svScripts.map { it.html() }.find { it.contains("eval(function(p,a,c,k,e,") }
                            if (packedScript != null) {
                                result.append("  Found packed script text of length: ${packedScript.length}\n")
                                val unpacker = com.unan.nexxxup.utils.JsUnpacker(packedScript)
                                val unpacked = unpacker.unpack()
                                result.append("  Unpacked script success: ${unpacked != null}\n")
                                if (unpacked != null) {
                                    result.append("  Unpacked script success: true\n")
                                    // Save the full unpacked script to aid debugging
                                    java.io.File("streamvid_unpacked.js").writeText(unpacked)
                                    
                                    val dp_line = svHtml.lines().find { it.contains("let dp =") }
                                    if (dp_line != null) {
                                        val dp_val = dp_line.substringAfter("let dp = '").substringBefore("'")
                                        result.append("  Full dp base64 string length: ${dp_val.length}\n")
                                        try {
                                            val decoded_dp = String(java.util.Base64.getDecoder().decode(dp_val))
                                            result.append("  Decoded dp: ${decoded_dp.take(300)}...\n")
                                        } catch (e: Exception) {
                                            result.append("  Failed to decode dp: ${e.message}\n")
                                        }
                                    }

                                    // Let's print all scripts from StreamVid that have _0x variables to find the decryption algorithm!
                                    svScripts.forEachIndexed { idx, scr ->
                                        val code = scr.html()
                                        if (code.contains("let pox =") && code.contains("eval(function(p,a,c,k,e,")) {
                                            // Let's print the entire function body of pr
                                            val prStart = code.indexOf("function pr(")
                                            if (prStart >= 0) {
                                                val prEnd = (prStart + 2000).coerceAtMost(code.length)
                                                result.append("  === FULL FUNCTION pr DEFINITION ===\n")
                                                result.append(code.substring(prStart, prEnd))
                                                result.append("\n=================================\n")
                                            }

                                            // Let's print the entire function body of EncpJS
                                            val encpStart = code.indexOf("var EncpJS={")
                                            if (encpStart >= 0) {
                                                val encpEnd = (encpStart + 3000).coerceAtMost(code.length)
                                                result.append("  === FULL var EncpJS DEFINITION ===\n")
                                                result.append(code.substring(encpStart, encpEnd))
                                                result.append("\n==================================\n")
                                            }
                                                
                                            // Try executing pure Kotlin decryption logic to evaluate pr(pox)
                                            try {
                                                val poxValue = Regex("""pox\s*=\s*'([^']+)'""").find(code)?.groupValues?.get(1) ?: ""
                                                val dpValue = Regex("""dp\s*=\s*'([^']+)'""").find(code)?.groupValues?.get(1) ?: ""
                                                result.append("POX VALUE: $poxValue\n")
                                                result.append("DP VALUE LENGTH: ${dpValue.length}\n")
                                                
                                                if (poxValue.isNotEmpty() && dpValue.isNotEmpty()) {
                                                    val passphrase = poxValue.split('+').getOrNull(1)?.substring(1) ?: ""
                                                    result.append("PASSPHRASE DERIVED: '$passphrase'\n")
                                                    
                                                    val decodedDpBytes = java.util.Base64.getDecoder().decode(dpValue)
                                                    val decodedDp = String(decodedDpBytes, Charsets.UTF_8)
                                                    result.append("DECODED DP JSON: $decodedDp\n")
                                                    
                                                    val ct = Regex("""\"ct\"\s*:\s*\"([^\"]+)\"""").find(decodedDp)?.groupValues?.get(1) ?: ""
                                                    val ivHex = Regex("""\"iv\"\s*:\s*\"([^\"]+)\"""").find(decodedDp)?.groupValues?.get(1) ?: ""
                                                    val saltHex = Regex("""\"s\"\s*:\s*\"([^\"]+)\"""").find(decodedDp)?.groupValues?.get(1) ?: ""
                                                    
                                                    result.append("CT: ${ct.length}, IV: $ivHex, SALT: $saltHex\n")
                                                    
                                                    if (ct.isNotEmpty() && ivHex.isNotEmpty() && saltHex.isNotEmpty() && passphrase.isNotEmpty()) {
                                                        fun hexToByteArray(s: String): ByteArray {
                                                            val len = s.length
                                                            val data = ByteArray(len / 2)
                                                            var i = 0
                                                            while (i < len) {
                                                                data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
                                                                i += 2
                                                            }
                                                            return data
                                                        }
                                                        val salt = hexToByteArray(saltHex)
                                                        val iv = hexToByteArray(ivHex)
                                                        val cleanCt = ct.replace("\\/", "/")
                                                        val ctBytes = java.util.Base64.getDecoder().decode(cleanCt)
                                                        
                                                        val md = java.security.MessageDigest.getInstance("MD5")
                                                        val passBytes = passphrase.toByteArray(Charsets.UTF_8)
                                                        
                                                        // D_1 = MD5(passphrase + salt)
                                                        md.update(passBytes)
                                                        val d1 = md.digest(salt)
                                                        
                                                        // D_2 = MD5(D_1 + passphrase + salt)
                                                        md.update(d1)
                                                        md.update(passBytes)
                                                        val d2 = md.digest(salt)
                                                        
                                                        val key = ByteArray(32)
                                                        System.arraycopy(d1, 0, key, 0, 16)
                                                        System.arraycopy(d2, 0, key, 16, 16)
                                                        
                                                        val cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding")
                                                        val keySpec = javax.crypto.spec.SecretKeySpec(key, "AES")
                                                        val ivSpec = javax.crypto.spec.IvParameterSpec(iv)
                                                        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, keySpec, ivSpec)
                                                        
                                                        val decryptedBytes = cipher.doFinal(ctBytes)
                                                        val decryptedText = String(decryptedBytes, Charsets.UTF_8)
                                                        result.append("PURE KOTLIN DECRYPTION SUCCESS!!! Result: '$decryptedText'\n")
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                result.append("Pure Kotlin decryption failed: ${e.message}\n${e.stackTraceToString()}\n")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        result.append("  Error fetching StreamVid URL: ${e.message}\n${e.stackTraceToString()}\n")
                    }
                }
            }
            
            val outText = result.toString()
            java.io.File("javsubindo_test_msg.txt").writeText(outText)
            println("JavSubIndo test finished.")
        } catch (e: Exception) {
            val errorMsg = "Error in testJavSubIndo: ${e.message}\n${e.stackTraceToString()}"
            println(errorMsg)
            java.io.File("javsubindo_test_msg.txt").writeText(errorMsg)
        }
    }

    @Test
    fun testJavbangers() {
        val html = URL("https://www.javbangers.com/").readText()
        val videoUrl = Regex("href=\"([^\"]+/video/[^\"]+)\"").find(html)?.groupValues?.get(1) ?: return
        val fixedUrl = if (videoUrl.startsWith("http")) videoUrl else "https://www.javbangers.com\$videoUrl"
        val videoHtml = URL(fixedUrl).readText()
        val result = StringBuilder()
        videoHtml.lines().forEachIndexed { index, line ->
            if (line.contains("iframe", true) || line.contains("video", true) || line.contains("source", true) || line.contains("player", true) || line.contains("data-src", true)) {
                result.append(index.toString() + ": " + line.trim() + "\n")
            }
        }
        java.io.File("/app/video_output.txt").writeText(result.toString())
    }
}
