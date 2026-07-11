package com.lagradost.cloudstream3.ui.settings

import android.os.Bundle
import android.text.format.Formatter.formatShortFileSize
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.actions.VideoClickActionHolder
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.player.source_priority.QualityProfileDialog
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.Globals.PHONE
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getFolderSize
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.hideOn
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.hidePrefs
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.subtitles.ChromecastSubtitlesFragment
import com.lagradost.cloudstream3.ui.subtitles.SubtitlesFragment
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showMultiDialog
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard

class SettingsPlayer : androidx.fragment.app.Fragment(com.lagradost.cloudstream3.R.layout.fragment_settings_player) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.category_player)
        
        setToolBarScrollFlags()

        val settingsManager = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_episode_sync_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.episode_sync_enabled_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.episode_sync_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.episode_sync_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_hide_player_control_names_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.hide_player_control_names_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.hide_player_control_names_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.hide_player_control_names_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_pip_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.pip_enabled_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.pip_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.pip_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_player_resize_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.player_resize_enabled_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.player_resize_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.player_resize_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_playback_speed_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.playback_speed_enabled_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.playback_speed_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.playback_speed_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_speedup_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.speedup_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.speedup_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.speedup_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_autoplay_next_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.autoplay_next_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.autoplay_next_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.autoplay_next_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_enable_skip_op_from_database)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.enable_skip_op_from_database), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.enable_skip_op_from_database), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.enable_skip_op_from_database) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_rotate_video_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.rotate_video_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.rotate_video_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.rotate_video_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_auto_rotate_video_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.auto_rotate_video_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.auto_rotate_video_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.auto_rotate_video_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_preview_seekbar_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.preview_seekbar_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.preview_seekbar_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.preview_seekbar_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_extra_brightness_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.extra_brightness_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.extra_brightness_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.extra_brightness_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_swipe_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.swipe_enabled_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.swipe_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.swipe_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_swipe_vertical_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.swipe_vertical_enabled_key), true)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.swipe_vertical_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.swipe_vertical_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_double_tap_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.double_tap_enabled_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.double_tap_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.double_tap_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(com.lagradost.cloudstream3.R.id.switch_double_tap_pause_enabled_key)?.let { switch ->
            switch.isChecked = settingsManager.getBoolean(getString(com.lagradost.cloudstream3.R.string.double_tap_pause_enabled_key), false)
            switch.setOnCheckedChangeListener { _, isChecked ->
                settingsManager.edit { putBoolean(getString(com.lagradost.cloudstream3.R.string.double_tap_pause_enabled_key), isChecked) }
                // Emulate preference click
                val pref = androidx.preference.SwitchPreference(requireContext()).apply { this.key = getString(com.lagradost.cloudstream3.R.string.double_tap_pause_enabled_key) }
                // We rely on the rest of the KT file matching "getPref" and attaching listeners. We must patch "getPref" calls!
            }
        }

        view.findViewById<android.widget.SeekBar>(com.lagradost.cloudstream3.R.id.seek_double_tap_seek_time_key)?.let { seek ->
            val min = 5
            val max = 60
            val step = 5
            seek.max = (max - min) / step
            val current = settingsManager.getInt(getString(com.lagradost.cloudstream3.R.string.double_tap_seek_time_key), 10)
            seek.progress = (current - min) / step
            view.findViewById<android.widget.TextView>(com.lagradost.cloudstream3.R.id.txt_double_tap_seek_time_key_val)?.text = current.toString()
            
            seek.setOnSeekBarChangeListener(object: android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = min + (progress * step)
                    view.findViewById<android.widget.TextView>(com.lagradost.cloudstream3.R.id.txt_double_tap_seek_time_key_val)?.text = value.toString()
                    if (fromUser) {
                        settingsManager.edit { putInt(getString(com.lagradost.cloudstream3.R.string.double_tap_seek_time_key), value) }
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }

        view.findViewById<android.widget.SeekBar>(com.lagradost.cloudstream3.R.id.seek_android_tv_interface_on_seek_key)?.let { seek ->
            val min = 5
            val max = 60
            val step = 5
            seek.max = (max - min) / step
            val current = settingsManager.getInt(getString(com.lagradost.cloudstream3.R.string.android_tv_interface_on_seek_key), 10)
            seek.progress = (current - min) / step
            view.findViewById<android.widget.TextView>(com.lagradost.cloudstream3.R.id.txt_android_tv_interface_on_seek_key_val)?.text = current.toString()
            
            seek.setOnSeekBarChangeListener(object: android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = min + (progress * step)
                    view.findViewById<android.widget.TextView>(com.lagradost.cloudstream3.R.id.txt_android_tv_interface_on_seek_key_val)?.text = value.toString()
                    if (fromUser) {
                        settingsManager.edit { putInt(getString(com.lagradost.cloudstream3.R.string.android_tv_interface_on_seek_key), value) }
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }

        view.findViewById<android.widget.SeekBar>(com.lagradost.cloudstream3.R.id.seek_android_tv_interface_off_seek_key)?.let { seek ->
            val min = 5
            val max = 60
            val step = 5
            seek.max = (max - min) / step
            val current = settingsManager.getInt(getString(com.lagradost.cloudstream3.R.string.android_tv_interface_off_seek_key), 10)
            seek.progress = (current - min) / step
            view.findViewById<android.widget.TextView>(com.lagradost.cloudstream3.R.id.txt_android_tv_interface_off_seek_key_val)?.text = current.toString()
            
            seek.setOnSeekBarChangeListener(object: android.widget.SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                    val value = min + (progress * step)
                    view.findViewById<android.widget.TextView>(com.lagradost.cloudstream3.R.id.txt_android_tv_interface_off_seek_key_val)?.text = value.toString()
                    if (fromUser) {
                        settingsManager.edit { putInt(getString(com.lagradost.cloudstream3.R.string.android_tv_interface_off_seek_key), value) }
                    }
                }
                override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
            })
        }

        bindPreferences(view)

    }

    fun bindPreferences(view: android.view.View) {
        hideKeyboard()
        
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        //Hide specific prefs on TV/EMULATOR
        // removed hidePrefs
        
        if (com.lagradost.cloudstream3.ui.settings.Globals.isLayout(TV)) view.findViewById<android.view.View>(R.id.btn_preview_seekbar_key)?.visibility = android.view.View.GONE
        // if (com.lagradost.cloudstream3.ui.settings.Globals.isLayout(PHONE)) view.findViewById<android.view.View>(R.id.btn_pref_category_android_tv_key)?.visibility = android.view.View.GONE

        view.findViewById<android.view.View>(R.id.btn_video_buffer_length_key)?.setOnClickListener {
            val prefNames = resources.getStringArray(R.array.video_buffer_length_names)
            val prefValues = resources.getIntArray(R.array.video_buffer_length_values)

            val currentPrefSize =
                settingsManager.getInt(getString(R.string.video_buffer_length_key), 0)

            activity?.showDialog(
                prefNames.toList(),
                prefValues.indexOf(currentPrefSize),
                getString(R.string.video_buffer_length_settings),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.video_buffer_length_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_prefer_limit_title_key)?.setOnClickListener {
            val prefNames = resources.getStringArray(R.array.limit_title_pref_names)
            val prefValues = resources.getIntArray(R.array.limit_title_pref_values)
            val current = settingsManager.getInt(getString(R.string.prefer_limit_title_key), 0)

            activity?.showBottomDialog(
                prefNames.toList(),
                prefValues.indexOf(current),
                getString(R.string.limit_title),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.prefer_limit_title_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_software_decoding_key)?.setOnClickListener {
            val prefNames = resources.getStringArray(R.array.software_decoding_switch)
            val prefValues = resources.getIntArray(R.array.software_decoding_switch_values)
            val current = settingsManager.getInt(getString(R.string.software_decoding_key), -1)

            activity?.showBottomDialog(
                prefNames.toList(),
                prefValues.indexOf(current),
                getString(R.string.software_decoding),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.software_decoding_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_prefer_limit_show_player_info)?.setOnClickListener {
            val ctx = context ?: return@setOnClickListener

            val prefNames = resources.getStringArray(R.array.title_info_pref_names)
            val keys = resources.getStringArray(R.array.title_info_pref_values)

            // Player defaults
            val playerDefaults = mapOf(
                ctx.getString(R.string.show_name_key) to true,
                ctx.getString(R.string.show_resolution_key) to true,
                ctx.getString(R.string.show_media_info_key) to false
            )

            val selectedIndices = keys.map { key ->
                settingsManager.getBoolean(key, playerDefaults[key] ?: false)
            }.mapIndexedNotNull { index, enabled ->
                if (enabled) index else null
            }

            activity?.showMultiDialog(
                prefNames.toList(),
                selectedIndices,
                getString(R.string.limit_title_rez),
                {}
            ) { selected ->
                settingsManager.edit {
                    for ((index, key) in keys.withIndex()) {
                        putBoolean(key, selected.contains(index))
                    }
                }
            }

            true
        }

        if (com.lagradost.cloudstream3.ui.settings.Globals.isLayout(TV)) view.findViewById<android.view.View>(R.id.btn_hide_player_control_names_key)?.visibility = android.view.View.GONE

        /*
        view.findViewById<android.view.View>(R.id.btn_quality_pref_key)?.setOnClickListener {
            val prefValues = Qualities.entries.map { it.value }.reversed().toMutableList()
            prefValues.remove(Qualities.Unknown.value)

            val prefNames = prefValues.map { Qualities.getStringByInt(it) }

            val currentQuality =
                settingsManager.getInt(
                    getString(R.string.quality_pref_key),
                    Qualities.entries.last().value
                )

            activity?.showBottomDialog(
                prefNames.toList(),
                prefValues.indexOf(currentQuality),
                getString(R.string.watch_quality_pref),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.quality_pref_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_quality_pref_mobile_data_key)?.setOnClickListener {
            val prefValues = Qualities.entries.map { it.value }.reversed().toMutableList()
            prefValues.remove(Qualities.Unknown.value)

            val prefNames = prefValues.map { Qualities.getStringByInt(it) }

            val currentQuality =
                settingsManager.getInt(
                    getString(R.string.quality_pref_mobile_data_key),
                    Qualities.entries.last().value
                )

            activity?.showBottomDialog(
                prefNames.toList(),
                prefValues.indexOf(currentQuality),
                getString(R.string.watch_quality_pref_data),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.quality_pref_mobile_data_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }
        */

        view.findViewById<android.view.View>(R.id.btn_player_default_key)?.setOnClickListener {
            val players = VideoClickActionHolder.getPlayers(activity)
            val prefNames = buildList {
                add(getString(R.string.player_settings_play_in_app))
                addAll(players.map { it.name.asStringNull(activity) ?: it.javaClass.simpleName })
            }
            val prefValues = buildList {
                add("")
                addAll(players.map { it.uniqueId() })
            }
            val current =
                settingsManager.getString(getString(R.string.player_default_key), "") ?: ""

            activity?.showBottomDialog(
                prefNames.toList(),
                prefValues.indexOf(current),
                getString(R.string.player_pref),
                true,
                {}
            ) {
                settingsManager.edit {
                    putString(getString(R.string.player_default_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_subtitle_settings_key)?.setOnClickListener {
            SubtitlesFragment.push(activity, false)
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_subtitle_settings_chromecast_key)?.setOnClickListener {
            ChromecastSubtitlesFragment.push(activity, false)
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_player_source_priority_key)?.setOnClickListener {
            ioSafe {
                val defaultSources = QualityProfileDialog.getAllDefaultSources()
                val activity = activity ?: return@ioSafe
                activity.runOnUiThread {
                    QualityProfileDialog(
                        activity,
                        R.style.DialogFullscreenPlayer,
                        defaultSources,
                    ).show()
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_video_buffer_disk_key)?.setOnClickListener {
            val prefNames = resources.getStringArray(R.array.video_buffer_size_names)
            val prefValues = resources.getIntArray(R.array.video_buffer_size_values)

            val currentPrefSize =
                settingsManager.getInt(getString(R.string.video_buffer_disk_key), 0)

            activity?.showDialog(
                prefNames.toList(),
                prefValues.indexOf(currentPrefSize),
                getString(R.string.video_buffer_disk_settings),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.video_buffer_disk_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }
        view.findViewById<android.view.View>(R.id.btn_video_buffer_size_key)?.setOnClickListener {
            val prefNames = resources.getStringArray(R.array.video_buffer_size_names)
            val prefValues = resources.getIntArray(R.array.video_buffer_size_values)

            val currentPrefSize =
                settingsManager.getInt(getString(R.string.video_buffer_size_key), 0)

            activity?.showDialog(
                prefNames.toList(),
                prefValues.indexOf(currentPrefSize),
                getString(R.string.video_buffer_size_settings),
                true,
                {}
            ) {
                settingsManager.edit {
                    putInt(getString(R.string.video_buffer_size_key), prefValues[it])
                }
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_video_buffer_clear_key)?.let { pref ->
            val cacheDir = context?.cacheDir ?: return@let

            fun updateSummary() {
                try {
                    view.findViewById<android.widget.TextView>(R.id.txt_video_buffer_clear_key_desc)?.apply {
                        text = formatShortFileSize(context, getFolderSize(cacheDir))
                        visibility = android.view.View.VISIBLE
                    }
                } catch (e: Exception) {
                    logError(e)
                }
            }

            updateSummary()

            pref.setOnClickListener {
                try {
                    cacheDir.deleteRecursively()
                    updateSummary()
                } catch (e: Exception) {
                    logError(e)
                }
            }
        }
    }
}
