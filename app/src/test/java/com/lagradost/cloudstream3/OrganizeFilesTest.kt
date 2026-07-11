package com.lagradost.cloudstream3

import org.junit.Test
import java.io.File

class OrganizeFilesTest {

    @Test
    fun proposeOrganization() {
        val mainJavaDir = File("src/main/java/com")
        if (!mainJavaDir.exists()) {
            println("Java directory does not exist at normal path, trying absolute path...")
            return
        }

        val sourceDirs = listOf("kraptor", "kerimmkirac", "owencz1998", "coxju", "xxx", "cxxx")
        
        println("=== Proposed organization ===")
        for (dirName in sourceDirs) {
            val dir = File(mainJavaDir, dirName)
            if (!dir.exists()) {
                println("Directory com/$dirName does not exist, skipping.")
                continue
            }
            
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension == "kt") {
                    val category = classifyFile(file.name, dirName)
                    println("File: ${dirName}/${file.name} -> com/${category}")
                }
            }
        }
    }

    private fun classifyFile(fileName: String, originDir: String): String {
        val nameLower = fileName.lowercase()
        
        // Webcam overrides
        if (nameLower.contains("cam") || 
            nameLower.contains("chaturbate") || 
            nameLower.contains("stripchat") || 
            nameLower.contains("camsoda")) {
            return "webcam"
        }

        // Western specific overrides
        if (fileName.startsWith("CornHubProvider") || 
            fileName.startsWith("IncestFlix") || 
            fileName.startsWith("Paradisehill") || 
            fileName.startsWith("XnxxProvider") || 
            fileName.startsWith("AllClassicPorn") || 
            fileName.startsWith("FamilyPorn") || 
            fileName.startsWith("FreeUsePorn") || 
            fileName.startsWith("PerfectGirls") || 
            fileName.startsWith("MilfNut") ||
            fileName.startsWith("Porn00") || 
            fileName.startsWith("PornHat") || 
            fileName.startsWith("CollectionOfBestPorn") || 
            fileName.startsWith("FreePornVideos")) {
            return "Western"
        }

        // Asian overrides
        if (nameLower.contains("jav") || 
            nameLower.contains("hentai") || 
            nameLower.contains("hanime") || 
            nameLower.contains("asian") || 
            nameLower.contains("desi") || 
            nameLower.contains("korea") || 
            nameLower.contains("turk") || 
            nameLower.contains("ifsalar") || 
            nameLower.contains("doeda") || 
            nameLower.contains("roshy") || 
            nameLower.contains("arab") || 
            nameLower.contains("tolly") || 
            nameLower.contains("uncut") || 
            nameLower.contains("mango") || 
            nameLower.contains("anime") || 
            nameLower.contains("vlxx") || 
            nameLower.contains("viet") || 
            nameLower.contains("hstream") || 
            nameLower.contains("hahomoe") || 
            nameLower.contains("happy2hub")) {
            return "Asian"
        }

        // Defaults by origin directory
        return when (originDir) {
            "cxxx", "coxju", "kerimmkirac" -> "Asian"
            "kraptor", "owencz1998", "xxx" -> "Western"
            else -> "Western"
        }
    }
}
