package com.unan.nexxxup.ui.settings.extensions

import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.unan.nexxxup.CommonActivity.showToast
import com.unan.nexxxup.MainActivity.Companion.afterRepositoryLoadedEvent
import com.unan.nexxxup.R
import com.unan.nexxxup.databinding.AddRepoInputBinding
import com.unan.nexxxup.databinding.FragmentExtensionsBinding
import com.unan.nexxxup.mvvm.observe
import com.unan.nexxxup.mvvm.observeNullable
import com.unan.nexxxup.plugins.PluginManager
import com.unan.nexxxup.plugins.RepositoryManager
import com.unan.nexxxup.ui.BaseFragment
import com.unan.nexxxup.ui.result.FOCUS_SELF
import com.unan.nexxxup.ui.result.setLinearListLayout
import com.unan.nexxxup.ui.setRecycledViewPool
import com.unan.nexxxup.ui.settings.Globals.TV
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setSystemBarsPadding
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setToolBarScrollFlags
import com.unan.nexxxup.ui.settings.SettingsFragment.Companion.setUpToolbar
import com.unan.nexxxup.utils.AppContextUtils.addRepositoryDialog
import com.unan.nexxxup.utils.AppContextUtils.setDefaultFocus
import com.unan.nexxxup.utils.Coroutines.ioSafe
import com.unan.nexxxup.utils.Coroutines.main
import com.unan.nexxxup.utils.UIHelper.dismissSafe
import com.unan.nexxxup.utils.setText
import com.unan.nexxxup.amap

@Suppress("DEPRECATION_ERROR")
class ExtensionsFragment : BaseFragment<FragmentExtensionsBinding>(
    BaseFragment.BindingCreator.Inflate(FragmentExtensionsBinding::inflate)
) {

    private val extensionViewModel: ExtensionsViewModel by activityViewModels()
    private val pluginViewModel: PluginsViewModel by activityViewModels()

    private val localPluginPicker = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNullOrEmpty()) return@registerForActivityResult
        val context = context ?: return@registerForActivityResult
        ioSafe {
            val successList = java.util.Collections.synchronizedList(mutableListOf<String>())
            val failedList = java.util.Collections.synchronizedList(mutableListOf<Pair<String, String>>())
            
            uris.amap { uri ->
                val fileName = com.unan.safefile.SafeFile.fromUri(context, uri)?.name() ?: "plugin.cs3"
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream == null) {
                        failedList.add(fileName to "Gagal membaca file dari penyimpanan.")
                        return@amap
                    }
                    

                    if (!fileName.endsWith(".cs3", ignoreCase = true) && !fileName.endsWith(".zip", ignoreCase = true)) {
                        failedList.add(fileName to "Gagal: Format tidak didukung. Hanya mendukung file .cs3.")
                        return@amap
                    }
                    
                    // Get the app-specific safe/writable directory
                    val pluginDirectory = java.io.File(context.filesDir, "plugins")
                    if (!pluginDirectory.exists()) {
                        pluginDirectory.mkdirs()
                    }
                    val destinationFile = java.io.File(pluginDirectory, fileName)
                    
                    // Copy to the safe app-specific directory (which is always writable)
                    destinationFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }

                    val success = PluginManager.loadLocalPluginFile(context, destinationFile)
                    if (success) {
                        // Also attempt to copy to the legacy public directory for backwards compatibility, but ignore failures silently
                        try {
                            val cloudStreamFolder = android.os.Environment.getExternalStorageDirectory().absolutePath + "/Nexxxup"
                            val localPluginsDir = java.io.File(cloudStreamFolder, "plugins")
                            if (!localPluginsDir.exists()) {
                                localPluginsDir.mkdirs()
                            }
                            val legacyFile = java.io.File(localPluginsDir, fileName)
                            destinationFile.inputStream().use { input ->
                                legacyFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                        } catch (e: Exception) {
                            // Ignore silently since external legacy storage might not be writable
                        }
                        successList.add(fileName)
                    } else {
                        try {
                            destinationFile.delete()
                        } catch (e: Exception) {
                            // Ignore
                        }
                        failedList.add(fileName to "Gagal memuat plugin: File tidak valid atau rusak.")
                    }
                } catch (e: Exception) {
                    com.unan.nexxxup.mvvm.logError(e)
                    failedList.add(fileName to "Error: ${e.message}")
                }
            }

            pluginViewModel.updatePluginListLocal()
            showInstallationResultsDialog(successList, failedList)
        }
    }


    private fun showInstallationResultsDialog(
        successList: List<String>,
        failedList: List<Pair<String, String>>
    ) {
        val context = context ?: return
        main {
            val builder = AlertDialog.Builder(context, R.style.AlertDialogCustom)
            builder.setTitle("Hasil Instalasi")

            val message = StringBuilder()
            if (successList.isNotEmpty()) {
                message.append("Berhasil diinstal:\n")
                successList.forEach { message.append("• $it\n") }
                message.append("\n")
            }
            if (failedList.isNotEmpty()) {
                message.append("Gagal diinstal:\n")
                failedList.forEach { (name, reason) ->
                    message.append("• $name: $reason\n")
                }
            }

            if (failedList.isEmpty()) {
                builder.setIcon(R.drawable.ic_baseline_extension_24)
            } else {
                builder.setIcon(android.R.drawable.ic_dialog_alert)
            }

            builder.setMessage(message.toString().trim())
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show().setDefaultFocus()
        }
    }

    private fun View.setLayoutWidth(weight: Int) {
        val param = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            weight.toFloat()
        )
        this.layoutParams = param
    }

    override fun onResume() {
        super.onResume()
        afterRepositoryLoadedEvent += ::reloadRepositories
    }

    override fun onStop() {
        super.onStop()
        afterRepositoryLoadedEvent -= ::reloadRepositories
    }

    private fun reloadRepositories(success: Boolean = true) {
        extensionViewModel.loadStats()
        extensionViewModel.loadRepositories()
    }

    override fun fixLayout(view: View) {
        setSystemBarsPadding()
    }

    override fun onBindingCreated(binding: FragmentExtensionsBinding) {
        setUpToolbar(R.string.extensions)
        setToolBarScrollFlags()

        binding.repoRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextUp = R.id.settings_toolbar, // FOCUS_SELF, // back has no id so we cant :pensive:
                nextDown = R.id.installed_recycler_view,
                nextRight = FOCUS_SELF,
                nextLeft = R.id.nav_rail_view
            )

            adapter = RepoAdapter(false, {
                findNavController().navigate(
                    R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                    PluginsFragment.newInstance(
                        it.name,
                        it.url,
                        false
                    )
                )
            }, { repo ->
                // Prompt user before deleting repo
                main {
                    val builder = AlertDialog.Builder(context ?: binding.root.context)
                    val dialogClickListener =
                        DialogInterface.OnClickListener { _, which ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE -> {
                                    ioSafe {
                                        RepositoryManager.removeRepository(binding.root.context, repo)
                                        RepositoryManager.removeNexRepository(binding.root.context, repo)
                                        extensionViewModel.loadStats()
                                        extensionViewModel.loadRepositories()
                                    }
                                }

                                DialogInterface.BUTTON_NEGATIVE -> {}
                            }
                        }

                    builder.setTitle(R.string.delete_repository)
                        .setMessage(
                            context?.getString(R.string.delete_repository_plugins)
                        )
                        .setPositiveButton(R.string.delete, dialogClickListener)
                        .setNegativeButton(R.string.cancel, dialogClickListener)
                        .show().setDefaultFocus()
                }
            })
        }

        binding.installedRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextUp = R.id.repo_recycler_view,
                nextDown = FOCUS_SELF,
                nextRight = FOCUS_SELF,
                nextLeft = R.id.nav_rail_view
            )
            adapter = PluginAdapter { plugin, isUpdate ->
                pluginViewModel.handlePluginAction(activity, emptyList(), plugin, true, isUpdate)
            }
        }

        observe(pluginViewModel.localPlugins) { (_, list) ->
            binding.installedLoading.isVisible = true
            binding.installedRecyclerView.isVisible = false
            binding.blankInstalledScreen.isVisible = false
            
            main {
                kotlinx.coroutines.delay(100) // Small delay for "loading tak kasat mata"
                binding.installedLoading.isVisible = false
                binding.installedRecyclerView.isVisible = list.isNotEmpty()
                binding.blankInstalledScreen.isVisible = list.isEmpty()
                (binding.installedRecyclerView.adapter as? PluginAdapter)?.submitList(list)
            }
        }

        observe(extensionViewModel.repositories) { repos ->
            binding.repoRecyclerView.isVisible = repos.isNotEmpty()
            binding.blankRepoScreen.isVisible = repos.isEmpty()
            (binding.repoRecyclerView.adapter as? RepoAdapter)?.submitList(repos.toList())
            pluginViewModel.updatePluginList(binding.root.context, repos.map { it.url })
        }

        observeNullable(extensionViewModel.pluginStats) { value ->
            binding.apply {
                if (value == null) {
                    pluginStorageAppbar.isVisible = false
                    return@observeNullable
                }

                pluginStorageAppbar.isVisible = true
                if (value.total == 0) {
                    pluginDownload.setLayoutWidth(1)
                    pluginDisabled.setLayoutWidth(0)
                    pluginNotDownloaded.setLayoutWidth(0)
                } else {
                    pluginDownload.setLayoutWidth(value.downloaded)
                    pluginDisabled.setLayoutWidth(value.disabled)
                    pluginNotDownloaded.setLayoutWidth(value.notDownloaded)
                }
                pluginNotDownloadedTxt.setText(value.notDownloadedText)
                pluginDisabledTxt.setText(value.disabledText)
                pluginDownloadTxt.setText(value.downloadedText)
            }
        }

        binding.pluginStorageAppbar.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_settings_extensions_to_navigation_settings_plugins,
                PluginsFragment.newInstance(
                    getString(R.string.extensions),
                    "",
                    true
                )
            )
        }

        binding.pluginRecyclerView.apply {
            setLinearListLayout(
                isHorizontal = false,
                nextDown = FOCUS_SELF,
                nextRight = FOCUS_SELF,
            )
            setRecycledViewPool(PluginAdapter.sharedPool)
            adapter =
                PluginAdapter { plugin, isUpdate ->
                    val urls = extensionViewModel.repositories.value?.map { repo -> repo.url }
                        ?: emptyList()
                    pluginViewModel.handlePluginAction(activity, urls, plugin, false, isUpdate)
                }
        }

        observe(pluginViewModel.filteredPlugins) { (scrollToTop, list) ->
            (binding.pluginRecyclerView.adapter as? PluginAdapter)?.submitList(list)
            if (scrollToTop) {
                binding.pluginRecyclerView.scrollToPosition(0)
            }
        }

        binding.settingsToolbar.apply {
            val searchItem = menu?.findItem(R.id.search_button)
            val searchView = searchItem?.actionView as? SearchView

            searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionCollapse(p0: MenuItem): Boolean {
                    binding.pluginRecyclerView.isVisible = false
                    binding.repoRecyclerView.isVisible = true
                    return true

                }

                override fun onMenuItemActionExpand(p0: MenuItem): Boolean {
                    binding.pluginRecyclerView.isVisible = true
                    binding.repoRecyclerView.isVisible = false
                    return true
                }
            })

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


        val addRepositoryClick = View.OnClickListener {
            val ctx = context ?: return@OnClickListener
            val binding = AddRepoInputBinding.inflate(LayoutInflater.from(ctx), null, false)
            val builder =
                AlertDialog.Builder(ctx, R.style.AlertDialogCustom)
                    .setView(binding.root)

            val dialog = builder.create()
            dialog.show()
            (activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.primaryClip?.getItemAt(
                0
            )?.text?.toString()?.let { copiedText ->
                if (copiedText.contains(RepoAdapter.SHAREABLE_REPO_SEPARATOR)) {
                    // text is of format <repository name> : <repository url>
                    val (name, url) = copiedText.split(
                        RepoAdapter.SHAREABLE_REPO_SEPARATOR,
                        limit = 2
                    )
                    binding.repoUrlInput.setText(url.trim())
                    binding.repoNameInput.setText(name.trim())
                } else {
                    binding.repoUrlInput.setText(copiedText)
                }
            }

            binding.applyBtt.setOnClickListener secondListener@{
                val name = binding.repoNameInput.text?.toString()
                ioSafe {
                    val url = binding.repoUrlInput.text?.toString()
                        ?.let { it1 -> RepositoryManager.parseRepoUrl(it1) }
                    if (url.isNullOrBlank()) {
                        main {
                            showToast(R.string.error_invalid_data, Toast.LENGTH_SHORT)
                        }
                    } else {
                        val repository = RepositoryManager.parseRepository(url)

                        // Exit if wrong repository
                        if (repository == null) {
                            showToast(R.string.no_repository_found_error, Toast.LENGTH_LONG)
                            return@ioSafe
                        }

                        val fixedName = if (!name.isNullOrBlank()) name
                        else repository.name
                        val newRepo = RepositoryData(repository.iconUrl, fixedName, url)
                        RepositoryManager.addRepository(newRepo)
                        extensionViewModel.loadStats()
                        extensionViewModel.loadRepositories()

                        val plugins = RepositoryManager.getRepoPlugins(url, true)
                        if (plugins.isNullOrEmpty()) {
                            showToast(R.string.no_plugins_found_error, Toast.LENGTH_LONG)
                        } else {
                            this@ExtensionsFragment.activity?.addRepositoryDialog(
                                fixedName,
                                url,
                            )
                        }
                    }
                }
                dialog.dismissSafe(activity)
            }
            binding.cancelBtt.setOnClickListener {
                dialog.dismissSafe(activity)
            }
        }


        val isTv = isLayout(TV)
        binding.apply {
            addRepoButton.isGone = true
            addRepoButtonImageviewHolder.isVisible = isTv

            val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(root.context)
            nsfwSwitch.isChecked = prefs.getBoolean("enable_nsfw_on_providers_key", false)
            nsfwSwitch.setOnCheckedChangeListener { _, isChecked ->
                prefs.edit().putBoolean("enable_nsfw_on_providers_key", isChecked).apply()
                pluginViewModel.updatePluginListLocal()
            }

            installLocalCard.setOnClickListener {
                localPluginPicker.launch("*/*")
            }
            addRepoCard.setOnClickListener(addRepositoryClick)

            // Band-aid for Fire TV
            pluginStorageAppbar.isFocusableInTouchMode = isTv
            addRepoButtonImageview.isFocusableInTouchMode = isTv

            addRepoButton.setOnClickListener(addRepositoryClick)
            addRepoButtonImageview.setOnClickListener(addRepositoryClick)
        }
        pluginViewModel.updatePluginListLocal()
        reloadRepositories()
    }
}
