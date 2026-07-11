package com.unan.nexxxup.ui.settings.extensions

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.unan.nexxxup.AllLanguagesName
import com.unan.nexxxup.BuildConfig
import com.unan.nexxxup.databinding.FragmentPluginsBinding
import com.unan.nexxxup.mvvm.observe
import com.unan.nexxxup.R
import com.unan.nexxxup.TvType
import com.unan.nexxxup.ui.BaseFragment
import com.unan.nexxxup.ui.home.HomeFragment.Companion.bindChips
import com.unan.nexxxup.ui.result.FOCUS_SELF
import com.unan.nexxxup.ui.result.setLinearListLayout
import com.unan.nexxxup.ui.setRecycledViewPool
import com.unan.nexxxup.ui.settings.Globals.EMULATOR
import com.unan.nexxxup.ui.settings.Globals.TV
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setSystemBarsPadding
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.unan.nexxxup.utils.AppContextUtils.getApiProviderLangSettings
import com.unan.nexxxup.utils.SingleSelectionHelper.showMultiDialog
import com.unan.nexxxup.utils.SubtitleHelper.getNameNextToFlagEmoji
import com.unan.nexxxup.utils.UIHelper.toPx

const val PLUGINS_BUNDLE_NAME = "name"
const val PLUGINS_BUNDLE_URL = "url"
const val PLUGINS_BUNDLE_LOCAL = "isLocal"

class PluginsFragment : BaseFragment<FragmentPluginsBinding>(
    BaseFragment.BindingCreator.Inflate(FragmentPluginsBinding::inflate)
) {
    private lateinit var pluginViewModel: PluginsViewModel

    override fun onDestroyView() {
        pluginViewModel.clear() // clear for the next observe
        super.onDestroyView()
    }

    override fun fixLayout(view: View) {
        setSystemBarsPadding()
    }

    override fun onBindingCreated(binding: FragmentPluginsBinding) {
        // Filter by language set on preferred media - completely bypassed to allow all countries/languages
        /*
        activity?.let {
            val providerLangs = it.getApiProviderLangSettings().toList()
            if (!providerLangs.contains(AllLanguagesName)) {
                pluginViewModel.selectedLanguages = mutableListOf("none") + providerLangs
            }
        }
        */

        val name = arguments?.getString(PLUGINS_BUNDLE_NAME)
        val url = arguments?.getString(PLUGINS_BUNDLE_URL) ?: return
        val isLocal = arguments?.getBoolean(PLUGINS_BUNDLE_LOCAL) ?: false
        pluginViewModel = ViewModelProvider(this)[PluginsViewModel::class.java]

        // Since the ViewModel is getting reused the tvTypes must be cleared between uses
        pluginViewModel.tvTypes.clear()
        pluginViewModel.selectedLanguages = listOf()
        pluginViewModel.clear()

        // download all extensions button
        val downloadAllButton = binding.settingsToolbar.menu?.findItem(R.id.download_all)

        if (url == null || name == null) {
            dispatchBackPressed()
            return
        }

        setToolBarScrollFlags()
        setUpToolbar(name)
        binding.settingsToolbar.apply {
            setOnMenuItemClickListener { menuItem ->
                when (menuItem?.itemId) {
                    R.id.download_all -> {
                        PluginsViewModel.downloadAll(activity, url, pluginViewModel)
                    }

                    R.id.lang_filter -> {
                        val languagesTagName = pluginViewModel.pluginLanguages
                            .map { langTag ->
                                Pair(
                                    langTag,
                                    getNameNextToFlagEmoji(langTag) ?: langTag
                                )
                            }
                            .sortedBy {
                                it.second.substringAfter("\u00a0").lowercase()
                            } // name ignoring flag emoji
                            .toMutableList()

                        // Move "none" to 1st position as it's special code to indicate unknown/missing language
                        if (languagesTagName.remove(Pair("none", "none"))) {
                            languagesTagName.add(0, Pair("none", getString(R.string.no_data)))
                        }

                        val currentIndexList = pluginViewModel.selectedLanguages.map { langTag ->
                            languagesTagName.indexOfFirst { lang -> lang.first == langTag }
                        }

                        activity?.showMultiDialog(
                            languagesTagName.map { it.second },
                            currentIndexList,
                            getString(R.string.provider_lang_settings),
                            {}
                        ) { selectedList ->
                            pluginViewModel.selectedLanguages =
                                selectedList.map { languagesTagName[it].first }
                            pluginViewModel.updateFilteredPlugins()
                        }
                    }

                    else -> {}
                }
                return@setOnMenuItemClickListener true
            }

            val searchView =
                menu?.findItem(R.id.search_button)?.actionView as? SearchView

            // Don't go back if active query
            setNavigationOnClickListener {
                if (searchView?.isIconified == false) {
                    searchView.isIconified = true
                } else {
                    dispatchBackPressed()
                }
            }

            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    pluginViewModel.search(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    pluginViewModel.search(newText)
                    return true
                }
            })
        }
//        searchView?.onActionViewCollapsed = {
//            pluginViewModel.search(null)
//        }

        // Because onActionViewCollapsed doesn't wanna work we need this workaround :(

        binding.pluginRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextDown = FOCUS_SELF,
                nextRight = FOCUS_SELF,
            )
            setRecycledViewPool(PluginAdapter.sharedPool)
            adapter =
                PluginAdapter { plugin, isUpdate ->
                    pluginViewModel.handlePluginAction(activity, listOf(url), plugin, isLocal, isUpdate)
                }
        }

        if (isLayout(TV or EMULATOR)) {
            // Scrolling down does not reveal the whole RecyclerView on TV, add to bypass that.
            binding.pluginRecyclerView.setPadding(0, 0, 0, 200.toPx)
        }

        observe(pluginViewModel.filteredPlugins) { (scrollToTop, list) ->
            (binding.pluginRecyclerView.adapter as? PluginAdapter)?.submitList(list)
            if (scrollToTop) {
                binding.pluginRecyclerView.scrollToPosition(0)
            }
        }

        if (isLocal) {
            // No download button and no categories on local
            downloadAllButton?.isVisible = false
            binding.settingsToolbar.menu?.findItem(R.id.lang_filter)?.isVisible = false
            pluginViewModel.updatePluginListLocal()

            binding.tvtypesChipsScroll.root.isVisible = false
        } else {
            pluginViewModel.updatePluginList(context, listOf(url))
            binding.tvtypesChipsScroll.root.isVisible = true
            // not needed for users but may be useful for devs
            downloadAllButton?.isVisible = BuildConfig.DEBUG

            bindChips(
                binding.tvtypesChipsScroll.tvtypesChips,
                emptyList(),
                TvType.entries.toList(),
                callback = { list ->
                    pluginViewModel.tvTypes.clear()
                    pluginViewModel.tvTypes.addAll(list.map { it.name })
                    pluginViewModel.updateFilteredPlugins()
                },
                nextFocusDown = R.id.plugin_recycler_view,
                nextFocusUp = null,
            )
        }
    }

    companion object {
        fun newInstance(name: String, url: String, isLocal: Boolean): Bundle {
            return Bundle().apply {
                putString(PLUGINS_BUNDLE_NAME, name)
                putString(PLUGINS_BUNDLE_URL, url)
                putBoolean(PLUGINS_BUNDLE_LOCAL, isLocal)
            }
        }

//        class RepoSearchView(context: Context) : android.widget.SearchView(context) {
//            var onActionViewCollapsed = {}
//
//            override fun onActionViewCollapsed() {
//                onActionViewCollapsed()
//            }
//        }

    }
}