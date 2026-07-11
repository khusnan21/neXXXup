package com.unan.nexxxup.plugins

import com.fasterxml.jackson.annotation.JsonProperty
import com.unan.nexxxup.APIHolder
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.utils.ExtractorApi
import com.unan.api.Log
import com.unan.nexxxup.utils.extractorApis

const val PLUGIN_TAG = "PluginInstance"

abstract class BasePlugin {
    /**
     * Used to register providers instances of MainAPI
     * @param element MainAPI provider you want to register
     */
    fun registerMainAPI(element: MainAPI) {
        Log.i(PLUGIN_TAG, "Adding ${element.name} (${element.mainUrl}) MainAPI")
        element.sourcePlugin = this.filename
        // Race condition causing which would case duplicates if not for distinctBy
        synchronized(APIHolder.allProviders) {
            APIHolder.allProviders.add(element)
        }
        APIHolder.addPluginMapping(element)
    }

    /**
     * Used to register extractor instances of ExtractorApi
     * @param element ExtractorApi provider you want to register
     */
    fun registerExtractorAPI(element: ExtractorApi) {
        Log.i(PLUGIN_TAG, "Adding ${element.name} (${element.mainUrl}) ExtractorApi")
        element.sourcePlugin = this.filename
        extractorApis.add(element)
    }

    /**
     * Called when your Plugin is being unloaded
     */
    @Throws(Throwable::class)
    open fun beforeUnload() {
    }

    /**
     * Called when your Plugin is loaded
     */
    @Throws(Throwable::class)
    open fun load() {
    }

    /** Full file path to the plugin. */
    @Deprecated(
        "Renamed to `filename` to follow conventions",
        replaceWith = ReplaceWith("filename"),
        level = DeprecationLevel.ERROR
    )
    var __filename: String?
        get() = filename
        set(value) {
            filename = value
        }
    var filename: String? = null


    class Manifest {
        @JsonProperty("name")
        var name: String? = null

        @JsonProperty("pluginClassName")
        var pluginClassName: String? = null

        @JsonProperty("version")
        var version: Int? = null

        @JsonProperty("requiresResources")
        var requiresResources: Boolean = false
    }
}