package com.unan.nexxxup.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.unan.nexxxup.CloudStreamApp.Companion.getActivity
import com.unan.nexxxup.MainActivity
import com.unan.nexxxup.R
import com.unan.nexxxup.SearchQuality
import com.unan.nexxxup.mvvm.logError
import com.unan.nexxxup.ui.BasePreferenceFragmentCompat
import com.unan.nexxxup.ui.clear
import com.unan.nexxxup.ui.home.HomeChildItemAdapter
import com.unan.nexxxup.ui.home.ParentItemAdapter
import com.unan.nexxxup.ui.search.SearchAdapter
import com.unan.nexxxup.ui.search.SearchResultBuilder
import com.unan.nexxxup.ui.settings.Globals.EMULATOR
import com.unan.nexxxup.ui.settings.Globals.PHONE
import com.unan.nexxxup.ui.settings.Globals.updateTv
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.getPref
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.hideOn
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setPaddingBottom
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.unan.nexxxup.utils.SingleSelectionHelper.showBottomDialog
import com.unan.nexxxup.utils.SingleSelectionHelper.showDialog
import com.unan.nexxxup.utils.SingleSelectionHelper.showMultiDialog
import com.unan.nexxxup.utils.UIHelper.hideKeyboard
import com.unan.nexxxup.utils.UIHelper.toPx

class SettingsUI : BasePreferenceFragmentCompat() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_ui)
        setPaddingBottom()
        setToolBarScrollFlags()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        hideKeyboard()
        setPreferencesFromResource(R.xml.settings_ui, rootKey)
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        (getPref(R.string.overscan_key)?.hideOn(PHONE or EMULATOR) as? SeekBarPreference)?.setOnPreferenceChangeListener { pref, newValue ->
            val padding = (newValue as? Int)?.toPx ?: return@setOnPreferenceChangeListener true
            (pref.context.getActivity() as? MainActivity)?.binding?.homeRoot?.setPadding(padding, padding, padding, padding)
            return@setOnPreferenceChangeListener true
        }

        getPref(R.string.bottom_title_key)?.setOnPreferenceChangeListener { _, _ ->
            HomeChildItemAdapter.sharedPool.clear()
            ParentItemAdapter.sharedPool.clear()
            SearchAdapter.sharedPool.clear()
            true
        }

        getPref(R.string.poster_size_key)?.setOnPreferenceChangeListener { _, newValue ->
            HomeChildItemAdapter.sharedPool.clear()
            ParentItemAdapter.sharedPool.clear()
            SearchAdapter.sharedPool.clear()
            context?.let { HomeChildItemAdapter.updatePosterSize(it, newValue as? Int) }
            true
        }

        getPref(R.string.poster_ui_key)?.setOnPreferenceClickListener {
            val prefNames = resources.getStringArray(R.array.poster_ui_options)
            val keys = resources.getStringArray(R.array.poster_ui_options_values)
            val prefValues = keys.map {
                settingsManager.getBoolean(it, true)
            }.mapIndexedNotNull { index, b ->
                if (b) {
                    index
                } else null
            }

            activity?.showMultiDialog(
                prefNames.toList(),
                prefValues,
                getString(R.string.poster_ui_settings),
                {}
            ) { list ->
                settingsManager.edit {
                    for ((i, key) in keys.withIndex()) {
                        putBoolean(key, list.contains(i))
                    }
                }
                SearchResultBuilder.updateCache(it.context)
            }

            return@setOnPreferenceClickListener true
        }



        getPref(R.string.pref_filter_search_quality_key)?.setOnPreferenceClickListener {
            val names = enumValues<SearchQuality>().sorted().map { it.name }
            val currentList = settingsManager.getStringSet(
                getString(R.string.pref_filter_search_quality_key),
                setOf()
            )?.map {
                it.toInt()
            } ?: listOf()

            activity?.showMultiDialog(
                names,
                currentList,
                getString(R.string.pref_filter_search_quality),
                {}
            ) { selectedList ->
                settingsManager.edit {
                    putStringSet(
                        getString(R.string.pref_filter_search_quality_key),
                        selectedList.map { it.toString() }.toMutableSet()
                    )
                }
            }

            return@setOnPreferenceClickListener true
        }

        getPref(R.string.confirm_exit_key)?.setOnPreferenceClickListener {
            val prefNames = resources.getStringArray(R.array.confirm_exit)
            val prefValues = resources.getIntArray(R.array.confirm_exit_values)
            val confirmExit = settingsManager.getInt(getString(R.string.confirm_exit_key), -1)

            activity?.showBottomDialog(
                items = prefNames.toList(),
                selectedIndex = prefValues.indexOf(confirmExit),
                name = getString(R.string.confirm_before_exiting_title),
                showApply = true,
                dismissCallback = {},
                callback = { selectedOption ->
                    settingsManager.edit {
                        putInt(getString(R.string.confirm_exit_key), prefValues[selectedOption])
                    }
                }
            )
            return@setOnPreferenceClickListener true
        }
    }
}