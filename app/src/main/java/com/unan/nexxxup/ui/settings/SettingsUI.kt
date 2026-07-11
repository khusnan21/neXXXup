package com.unan.nexxxup.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.preference.SeekBarPreference
import com.unan.nexxxup.NexxxupApp.Companion.getActivity
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
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.unan.nexxxup.utils.SingleSelectionHelper.showBottomDialog
import com.unan.nexxxup.utils.SingleSelectionHelper.showDialog
import com.unan.nexxxup.utils.SingleSelectionHelper.showMultiDialog
import com.unan.nexxxup.utils.UIHelper.hideKeyboard
import com.unan.nexxxup.utils.UIHelper.toPx

class SettingsUI : androidx.fragment.app.Fragment(com.unan.nexxxup.R.layout.fragment_settings_ui) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_ui)
        
        setToolBarScrollFlags()

        val settingsManager = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_bottom_title_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.unan.nexxxup.R.string.bottom_title_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.unan.nexxxup.R.string.bottom_title_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.unan.nexxxup.R.string.bottom_title_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<android.widget.SeekBar>(com.unan.nexxxup.R.id.seek_overscan_key)?.let { seek ->
            val min = 0
            val max = 100
            val step = 1
            seek.max = (max - min) / step
            val current = settingsManager.getInt(getString(com.unan.nexxxup.R.string.overscan_key), 0)
            seek.progress = (current - min) / step
            view.findViewById<android.widget.TextView>(com.unan.nexxxup.R.id.txt_overscan_key_val)?.text = current.toString()
            
            seek.setOnSeekBarChangeListener(object: android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = min + (progress * step)
                    view.findViewById<android.widget.TextView>(com.unan.nexxxup.R.id.txt_overscan_key_val)?.text = value.toString()
                    if (fromUser) {
                        settingsManager.edit { putInt(getString(com.unan.nexxxup.R.string.overscan_key), value) }
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }

        view.findViewById<android.widget.SeekBar>(com.unan.nexxxup.R.id.seek_poster_size_key)?.let { seek ->
            val min = 0
            val max = 15
            val step = 1
            seek.max = (max - min) / step
            val current = settingsManager.getInt(getString(com.unan.nexxxup.R.string.poster_size_key), 0)
            seek.progress = (current - min) / step
            view.findViewById<android.widget.TextView>(com.unan.nexxxup.R.id.txt_poster_size_key_val)?.text = current.toString()
            
            seek.setOnSeekBarChangeListener(object: android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = min + (progress * step)
                    view.findViewById<android.widget.TextView>(com.unan.nexxxup.R.id.txt_poster_size_key_val)?.text = value.toString()
                    if (fromUser) {
                        settingsManager.edit { putInt(getString(com.unan.nexxxup.R.string.poster_size_key), value) }
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_advanced_search)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.unan.nexxxup.R.string.advanced_search), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.unan.nexxxup.R.string.advanced_search), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.unan.nexxxup.R.string.advanced_search) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_search_suggestions_enabled)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean("search_suggestions_enabled", true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean("search_suggestions_enabled", isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = "search_suggestions_enabled" }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_show_trailers_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.unan.nexxxup.R.string.show_trailers_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.unan.nexxxup.R.string.show_trailers_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.unan.nexxxup.R.string.show_trailers_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_show_cast_in_details_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.unan.nexxxup.R.string.show_cast_in_details_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.unan.nexxxup.R.string.show_cast_in_details_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.unan.nexxxup.R.string.show_cast_in_details_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_show_fillers_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.unan.nexxxup.R.string.show_fillers_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.unan.nexxxup.R.string.show_fillers_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.unan.nexxxup.R.string.show_fillers_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.unan.nexxxup.R.id.switch_random_button_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.unan.nexxxup.R.string.random_button_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.unan.nexxxup.R.string.random_button_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.unan.nexxxup.R.string.random_button_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        bindPreferences(view)

    }

    fun bindPreferences(view: android.view.View) {
        hideKeyboard()
        
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // overscan listener is handled by seekbar

        view.findViewById<android.view.View>(R.id.btn_bottom_title_key)?.setOnClickListener { _ ->
            HomeChildItemAdapter.sharedPool.clear()
            ParentItemAdapter.sharedPool.clear()
            SearchAdapter.sharedPool.clear()
        }

        view.findViewById<android.view.View>(R.id.btn_poster_size_key)?.setOnClickListener { _ ->
            HomeChildItemAdapter.sharedPool.clear()
            ParentItemAdapter.sharedPool.clear()
            SearchAdapter.sharedPool.clear()
            // context?.let { HomeChildItemAdapter.updatePosterSize(it, newValue as? Int) }
        }

        view.findViewById<android.view.View>(R.id.btn_poster_ui_key)?.setOnClickListener {
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

            return@setOnClickListener
        }



        view.findViewById<android.view.View>(R.id.btn_pref_filter_search_quality_key)?.setOnClickListener {
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

            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_confirm_exit_key)?.setOnClickListener {
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
            return@setOnClickListener
        }
    }
}