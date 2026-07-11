package com.lagradost.cloudstream3.ui.settings

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.services.BackupWorkManager
import com.lagradost.cloudstream3.ui.BasePreferenceFragmentCompat
import com.lagradost.cloudstream3.ui.settings.Globals.EMULATOR
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.getPref
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.hideOn
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.lagradost.cloudstream3.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.lagradost.cloudstream3.ui.settings.utils.getChooseFolderLauncher
import com.lagradost.cloudstream3.utils.BackupUtils
import com.lagradost.cloudstream3.utils.BackupUtils.restorePrompt
import com.lagradost.cloudstream3.utils.Coroutines.ioSafe
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showBottomDialog
import com.lagradost.cloudstream3.utils.SingleSelectionHelper.showDialog
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard
import com.lagradost.cloudstream3.mvvm.logError
import com.lagradost.cloudstream3.mvvm.safe

class SettingsBackup : androidx.fragment.app.Fragment(com.lagradost.cloudstream3.R.layout.fragment_settings_backup) {
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

        if (com.lagradost.cloudstream3.ui.settings.Globals.isLayout(EMULATOR)) view.findViewById<android.view.View>(R.id.btn_backup_path_key)?.visibility = android.view.View.GONE
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
