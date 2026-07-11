package com.lagradost.cloudstream3.ui.settings

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.android.material.switchmaterial.SwitchMaterial
import com.lagradost.cloudstream3.APIHolder.allProviders
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.MainActivity
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.databinding.AddRemoveSitesBinding
import com.lagradost.cloudstream3.databinding.AddSiteInputBinding
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe
import com.lagradost.cloudstream3.network.initClient
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.beneneCount
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.BatteryOptimizationChecker.isAppRestricted
import com.lagradost.cloudstream3.utils.BatteryOptimizationChecker.showBatteryOptimizationDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showMultiDialog
import com.lagradost.cloudstream3.utils.SubtitleHelper
import com.lagradost.cloudstream3.utils.UIHelper.dismissSafe
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import com.lagradost.cloudstream3.utils.UIHelper.navigate
import com.lagradost.cloudstream3.utils.USER_PROVIDER_API
import com.lagradost.cloudstream3.utils.downloader.DownloadFileManagement
import com.lagradost.cloudstream3.utils.downloader.DownloadFileManagement.getBasePath
import com.lagradost.cloudstream3.utils.downloader.DownloadQueueManager
import java.util.Locale

fun getCurrentLocale(context: Context): String {
    return "in"
}

val appLanguages = arrayListOf(
    Pair("Bahasa Indonesia", "in"),
).sortedBy { it.first.lowercase(Locale.ROOT) }

fun Pair<String, String>.nameNextToFlagEmoji(): String {
    val flag = SubtitleHelper.getFlagFromIso(this.second) ?: "\ud83c\udde6\ud83c\udde6"
    return "$flag\u00a0${this.first}"
}

class SettingsGeneral : Fragment() {
    
    data class CustomSite(
        @JsonProperty("parentJavaClass")
        val parentJavaClass: String,
        @JsonProperty("name")
        val name: String,
        @JsonProperty("url")
        val url: String,
        @JsonProperty("lang")
        val lang: String,
    )

    private val pathPicker = getChooseFolderLauncher { uri, path ->
        val context = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        (path ?: uri.toString()).let {
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putString(getString(R.string.download_path_key), uri.toString())
                putString(getString(R.string.download_path_key_visual), it)
            }
            view?.findViewById<TextView>(R.id.txt_download_path_desc)?.text = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings_general, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(getString(R.string.category_general))
        setToolBarScrollFlags()
        hideKeyboard()

        val settingsManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        fun getCurrent(): MutableList<CustomSite> {
            return getKey<Array<CustomSite>>(USER_PROVIDER_API)?.toMutableList()
                ?: mutableListOf()
        }

        view.findViewById<LinearLayout>(R.id.btn_battery)?.setOnClickListener {
            val ctx = context ?: return@setOnClickListener
            if (isAppRestricted(ctx)) {
                ctx.showBatteryOptimizationDialog()
            } else {
                showToast(R.string.app_unrestricted_toast)
            }
        }

        fun showAdd() {
            val providers = synchronized(allProviders) { allProviders.distinctBy { it.javaClass }.sortedBy { it.name } }
            activity?.showDialog(
                providers.map { "${it.name} (${it.mainUrl})" },
                -1,
                context?.getString(R.string.add_site_pref) ?: return,
                true,
                {}) { selection ->
                val provider = providers.getOrNull(selection) ?: return@showDialog

                val binding = AddSiteInputBinding.inflate(layoutInflater, null, false)
                val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom).setView(binding.root)
                val dialog = builder.create()
                dialog.show()

                binding.text2.text = provider.name
                binding.applyBtt.setOnClickListener {
                    val name = binding.siteNameInput.text?.toString()
                    val url = binding.siteUrlInput.text?.toString()
                    val lang = binding.siteLangInput.text?.toString()
                    val realLang = if (lang.isNullOrBlank()) provider.lang else lang
                    if (url.isNullOrBlank() || name.isNullOrBlank()) {
                        showToast(R.string.error_invalid_data, Toast.LENGTH_SHORT)
                        return@setOnClickListener
                    }

                    val current = getCurrent()
                    val newSite = CustomSite(provider.javaClass.simpleName, name, url, realLang)
                    current.add(newSite)
                    setKey(USER_PROVIDER_API, current.toTypedArray())
                    MainActivity.afterPluginsLoadedEvent.invoke(false)
                    dialog.dismissSafe(activity)
                }
                binding.cancelBtt.setOnClickListener {
                    dialog.dismissSafe(activity)
                }
            }
        }

        fun showDelete() {
            val current = getCurrent()
            activity?.showMultiDialog(
                current.map { it.name },
                listOf(),
                context?.getString(R.string.remove_site_pref) ?: return,
                {}) { indexes ->
                current.removeAll(indexes.map { current[it] })
                setKey(USER_PROVIDER_API, current.toTypedArray())
            }
        }

        fun showAddOrDelete() {
            val binding = AddRemoveSitesBinding.inflate(layoutInflater, null, false)
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom).setView(binding.root)
            val dialog = builder.create()
            dialog.show()

            binding.addSite.setOnClickListener {
                showAdd()
                dialog.dismissSafe(activity)
            }
            binding.removeSite.setOnClickListener {
                showDelete()
                dialog.dismissSafe(activity)
            }
        }

        view.findViewById<LinearLayout>(R.id.btn_add_site)?.setOnClickListener {
            if (getCurrent().isEmpty()) {
                showAdd()
            } else {
                showAddOrDelete()
            }
        }

        val dnsDesc = view.findViewById<TextView>(R.id.txt_dns_desc)
        val prefNames = resources.getStringArray(R.array.dns_pref)
        val prefValues = resources.getIntArray(R.array.dns_pref_values)
        val currentDns = settingsManager.getInt(getString(R.string.dns_pref), 0)
        val currentDnsIndex = prefValues.indexOf(currentDns).let { if (it == -1) 0 else it }
        dnsDesc?.text = prefNames.getOrNull(currentDnsIndex) ?: ""

        view.findViewById<LinearLayout>(R.id.btn_dns)?.setOnClickListener {
            val curDns = settingsManager.getInt(getString(R.string.dns_pref), 0)
            val idx = prefValues.indexOf(curDns).let { if (it == -1) 0 else it }
            
            activity?.showBottomDialog(
                prefNames.toList(),
                idx,
                getString(R.string.dns_pref),
                true,
                {}) { selectedIdx ->
                settingsManager.edit { putInt(getString(R.string.dns_pref), prefValues[selectedIdx]) }
                dnsDesc?.text = prefNames[selectedIdx]
                (context ?: CloudStreamApp.context)?.let { ctx -> app.initClient(ctx) }
            }
        }

        fun getDownloadDirs(): List<String> {
            return safe {
                context?.let { ctx ->
                    val defaultDir = DownloadFileManagement.getDefaultDir(ctx)?.filePath()
                    val first = listOf(defaultDir)
                    (try {
                        val currentDir = ctx.getBasePath().let { it.first?.filePath() ?: it.second }
                        (first + ctx.getExternalFilesDirs("").mapNotNull { it.path } + currentDir)
                    } catch (e: Exception) {
                        first
                    }).filterNotNull().distinct()
                }
            } ?: emptyList()
        }

        val jsdelivrSwitch = view.findViewById<SwitchMaterial>(R.id.switch_jsdelivr)
        jsdelivrSwitch?.isChecked = getKey(getString(R.string.jsdelivr_proxy_key), false) ?: false
        jsdelivrSwitch?.setOnCheckedChangeListener { _, isChecked ->
            setKey(getString(R.string.jsdelivr_proxy_key), isChecked)
            settingsManager.edit { putBoolean(getString(R.string.jsdelivr_proxy_key), isChecked) }
        }

        val seekParallel = view.findViewById<SeekBar>(R.id.seek_parallel)
        val txtParallel = view.findViewById<TextView>(R.id.txt_parallel_val)
        val seekConcurrent = view.findViewById<SeekBar>(R.id.seek_concurrent)
        val txtConcurrent = view.findViewById<TextView>(R.id.txt_concurrent_val)

        val parallelVal = settingsManager.getInt(getString(R.string.download_parallel_key), 3)
        val concurrentVal = settingsManager.getInt(getString(R.string.download_concurrent_key), 3)
        
        seekParallel?.progress = (parallelVal - 1).coerceIn(0, 9)
        txtParallel?.text = parallelVal.toString()
        seekParallel?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 1
                txtParallel?.text = value.toString()
                settingsManager.edit { putInt(getString(R.string.download_parallel_key), value) }
                DownloadQueueManager.forceRefreshQueue()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekConcurrent?.progress = (concurrentVal - 1).coerceIn(0, 9)
        txtConcurrent?.text = concurrentVal.toString()
        seekConcurrent?.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress + 1
                txtConcurrent?.text = value.toString()
                settingsManager.edit { putInt(getString(R.string.download_concurrent_key), value) }
                DownloadQueueManager.forceRefreshQueue()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val txtDownloadPath = view.findViewById<TextView>(R.id.txt_download_path_desc)
        val currentDir = settingsManager.getString(getString(R.string.download_path_key_visual), null)
                    ?: context?.let { ctx -> DownloadFileManagement.getDefaultDir(ctx)?.filePath() }
        txtDownloadPath?.text = currentDir ?: "Default"

        view.findViewById<LinearLayout>(R.id.btn_download_path)?.setOnClickListener {
            val dirs = getDownloadDirs()
            val curDir = settingsManager.getString(getString(R.string.download_path_key_visual), null)
                    ?: context?.let { ctx -> DownloadFileManagement.getDefaultDir(ctx)?.filePath() }
            
            activity?.showBottomDialog(
                dirs + listOf(getString(R.string.custom)),
                dirs.indexOf(curDir),
                getString(R.string.download_path_pref),
                true,
                {}) {
                if (it == dirs.size) {
                    try {
                        pathPicker.launch(Uri.EMPTY)
                    } catch (e: Exception) {
                        logError(e)
                    }
                } else {
                    settingsManager.edit {
                        putString(getString(R.string.download_path_key), dirs[it])
                        putString(getString(R.string.download_path_key_visual), dirs[it])
                    }
                    txtDownloadPath?.text = dirs[it]
                }
            }
        }
    }
}
