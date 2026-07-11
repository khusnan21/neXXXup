package com.unan.nexxxup.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.unan.nexxxup.CloudStreamApp
import com.unan.nexxxup.R
import com.unan.nexxxup.services.BackupWorkManager
import com.unan.nexxxup.ui.BasePreferenceFragmentCompat
import com.unan.nexxxup.ui.settings.Globals.EMULATOR
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.getPref
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.hideOn
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.unan.nexxxup.ui.settings.utils.getChooseFolderLauncher
import com.unan.nexxxup.utils.BackupUtils
import com.unan.nexxxup.utils.BackupUtils.restorePrompt
import com.unan.nexxxup.utils.Coroutines.ioSafe
import com.unan.nexxxup.utils.SingleSelectionHelper.showBottomDialog
import com.unan.nexxxup.utils.SingleSelectionHelper.showDialog
import com.unan.nexxxup.utils.UIHelper.hideKeyboard
import com.unan.nexxxup.mvvm.logError
import com.unan.nexxxup.mvvm.safe

class SettingsBackup : androidx.fragment.app.Fragment(com.unan.nexxxup.R.layout.fragment_settings_backup) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(R.string.pref_category_backup)
        
        setToolBarScrollFlags()

        val settingsManager = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())

        bindPreferences(view)

    }

    private val pathPicker = getChooseFolderLauncher { uri, path ->
        val context = context ?: CloudStreamApp.context ?: return@getChooseFolderLauncher
        (path ?: uri.toString()).let {
            PreferenceManager.getDefaultSharedPreferences(context).edit {
                putString(getString(R.string.backup_path_key), uri.toString())
                putString(getString(R.string.backup_dir_key), it)
            }
        }
    }

    @Suppress("DEPRECATION_ERROR")
    fun bindPreferences(view: android.view.View) {
        hideKeyboard()
        
        val settingsManager = PreferenceManager.getDefaultSharedPreferences(requireContext())

        view.findViewById<android.view.View>(R.id.btn_backup_key)?.setOnClickListener {
            BackupUtils.backup(activity)
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_automatic_backup_key)?.setOnClickListener {
            val prefNames = resources.getStringArray(R.array.periodic_work_names)
            val prefValues = resources.getIntArray(R.array.periodic_work_values)
            val current = settingsManager.getInt(getString(R.string.automatic_backup_key), 0)

            activity?.showDialog(
                prefNames.toList(),
                prefValues.indexOf(current),
                getString(R.string.backup_frequency),
                true,
                {}
            ) { index ->
                settingsManager.edit {
                    putInt(getString(R.string.automatic_backup_key), prefValues[index])
                }
                BackupWorkManager.enqueuePeriodicWork(
                    context ?: CloudStreamApp.context,
                    prefValues[index].toLong()
                )
            }
            return@setOnClickListener
        }

        view.findViewById<android.view.View>(R.id.btn_restore_key)?.setOnClickListener {
            activity?.restorePrompt()
            return@setOnClickListener
        }

        if (com.unan.nexxxup.ui.settings.Globals.isLayout(EMULATOR)) view.findViewById<android.view.View>(R.id.btn_backup_path_key)?.visibility = android.view.View.GONE
        view.findViewById<android.view.View>(R.id.btn_backup_path_key)?.setOnClickListener {
            val dirs = getBackupDirsForDisplay()
            val currentDir =
                settingsManager.getString(getString(R.string.backup_dir_key), null)
                    ?: context?.let { ctx -> BackupUtils.getDefaultBackupDir(ctx)?.filePath() }

            activity?.showBottomDialog(
                dirs + listOf(getString(R.string.custom)),
                dirs.indexOf(currentDir),
                getString(R.string.backup_path_title),
                true,
                {}
            ) {
                // Last = custom
                if (it == dirs.size) {
                    try {
                        pathPicker.launch(Uri.EMPTY)
                    } catch (e: Exception) {
                        logError(e)
                    }
                } else {
                    settingsManager.edit {
                        putString(getString(R.string.backup_path_key), dirs[it])
                        putString(getString(R.string.backup_dir_key), dirs[it])
                    }
                }
            }
            return@setOnClickListener
        }
    }

    private fun getBackupDirsForDisplay(): List<String> {
        return safe {
            context?.let { ctx ->
                val defaultDir = BackupUtils.getDefaultBackupDir(ctx)?.filePath()
                val first = listOf(defaultDir)
                (runCatching {
                    first + BackupUtils.getCurrentBackupDir(ctx).let {
                        it.first?.filePath() ?: it.second
                    }
                }.getOrNull() ?: first).filterNotNull().distinct()
            }
        } ?: emptyList()
    }
}
