package com.unan.nexxxup.ui.settings.extractors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.unan.nexxxup.CommonActivity.showToast
import com.unan.nexxxup.R
import com.unan.nexxxup.databinding.AddRepoInputBinding
import com.unan.nexxxup.databinding.FragmentExtractorsBinding
import com.unan.nexxxup.mvvm.safeApiCall
import com.unan.nexxxup.plugins.JsExtractorRepoData
import com.unan.nexxxup.plugins.JsExtractorRepositoryManager
import com.unan.nexxxup.amap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ExtractorsFragment : Fragment() {
    private var _binding: FragmentExtractorsBinding? = null
    private val binding get() = _binding!!
    
    private var extractorsAdapter: ExtractorsAdapter? = null
    private var reposAdapter: JsExtractorReposAdapter? = null

    private val selectJsFileLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (!uris.isNullOrEmpty()) {
            val context = context ?: return@registerForActivityResult
            GlobalScope.launch(Dispatchers.IO) {
                val successNames = java.util.Collections.synchronizedList(mutableListOf<String>())
                val failedNames = java.util.Collections.synchronizedList(mutableListOf<String>())
                
                uris.amap { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { inputStream ->
                            val content = inputStream.bufferedReader().readText()
                            
                            val extractorsDir = File(context.filesDir, "extractors")
                            if (!extractorsDir.exists()) extractorsDir.mkdirs()
                            
                            val nameRegex = """var\s+name\s*=\s*["']([^"']+)["']""".toRegex()
                            val extName = nameRegex.find(content)?.groupValues?.get(1) ?: ("local_" + System.currentTimeMillis())
                            
                            val fileName = "$extName.js"
                            val outFile = File(extractorsDir, fileName)
                            outFile.writeText(content)
                            com.unan.nexxxup.plugins.JsExtractorLoader.loadCode(content, fileName)
                            successNames.add(extName)
                        }
                    } catch (e: Exception) {
                        failedNames.add(e.message ?: "Gagal")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    if (successNames.isNotEmpty()) {
                        val successMsg = "Berhasil menginstal dan memuat: ${successNames.joinToString(", ")}"
                        showToast(activity, successMsg, android.widget.Toast.LENGTH_LONG)
                    }
                    if (failedNames.isNotEmpty()) {
                        val failedMsg = "Gagal menginstal ${failedNames.size} extractor: ${failedNames.joinToString(", ")}"
                        showToast(activity, failedMsg, android.widget.Toast.LENGTH_SHORT)
                    }
                    loadData()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtractorsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.recyclerRepos.layoutManager = LinearLayoutManager(context)
        binding.recyclerExtractors.layoutManager = LinearLayoutManager(context)

        binding.btnInstallLocal.setOnClickListener {
            selectJsFileLauncher.launch(arrayOf("application/javascript", "text/javascript", "application/x-javascript", "*/*"))
        }

        binding.btnAddRepo.setOnClickListener {
            showAddRepoDialog()
        }
        
        loadData()
    }
    
    private fun loadData() {
        val context = context ?: return
        val extractors = JsExtractorRepositoryManager.getInstalledExtractors(context)
        val repos = JsExtractorRepositoryManager.getRepositories()
        
        if (extractorsAdapter == null) {
            extractorsAdapter = ExtractorsAdapter(extractors) { file ->
                JsExtractorRepositoryManager.removeExtractor(context, file.name)
                val extName = file.name.removeSuffix(".js")
                com.unan.nexxxup.utils.extractorApis.removeAll {
                    it is com.unan.nexxxup.plugins.JsExtractor && 
                    (it.name.equals(extName, ignoreCase = true) || it.name.equals(file.name, ignoreCase = true))
                }
                loadData()
            }
            binding.recyclerExtractors.adapter = extractorsAdapter
        } else {
            extractorsAdapter?.extractors = extractors
            extractorsAdapter?.notifyDataSetChanged()
        }
        
        if (reposAdapter == null) {
            reposAdapter = JsExtractorReposAdapter(repos, onDelete = { repo ->
                JsExtractorRepositoryManager.removeRepository(repo.url)
                loadData()
            }, onClick = { repo ->
                showRepoExtractorsDialog(repo)
            })
            binding.recyclerRepos.adapter = reposAdapter
        } else {
            reposAdapter?.repos = repos
            reposAdapter?.notifyDataSetChanged()
        }
    }
    
    private fun showAddRepoDialog() {
        val ctx = context ?: return
        val dialogBinding = AddRepoInputBinding.inflate(LayoutInflater.from(ctx), null, false)
        val builder = AlertDialog.Builder(ctx, R.style.AlertDialogCustom).setView(dialogBinding.root)
        val dialog = builder.create()
        dialog.show()
        
        dialogBinding.applyBtt.setOnClickListener {
            val url = dialogBinding.repoUrlInput.text?.toString() ?: ""
            var name = dialogBinding.repoNameInput.text?.toString() ?: ""
            
            if (url.isNotBlank()) {
                GlobalScope.launch(Dispatchers.IO) {
                    val repo = JsExtractorRepositoryManager.fetchRepository(url)
                    withContext(Dispatchers.Main) {
                        if (repo != null) {
                            val finalRepo = if (name.isNotBlank()) repo.copy(name = name) else repo
                            JsExtractorRepositoryManager.addRepository(finalRepo)
                            showToast(activity, "Repository ditambahkan", android.widget.Toast.LENGTH_SHORT)
                            dialog.dismiss()
                            loadData()
                        } else {
                            showToast(activity, "Gagal mengambil data repository", android.widget.Toast.LENGTH_SHORT)
                        }
                    }
                }
            }
        }
    }
    
    private fun showRepoExtractorsDialog(repo: JsExtractorRepoData) {
        val ctx = context ?: return
        val names = repo.extractors.map { it.name }.toTypedArray()
        
        AlertDialog.Builder(ctx, R.style.AlertDialogCustom)
            .setTitle(repo.name)
            .setItems(names) { _, which ->
                val extractor = repo.extractors[which]
                downloadAndInstallExtractor(extractor.name, extractor.url)
            }
            .setNegativeButton("Tutup", null)
            .show()
    }
    
    private fun downloadAndInstallExtractor(name: String, url: String) {
        val ctx = context ?: return
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val code = com.unan.nexxxup.app.get(url).text
                val extractorsDir = File(ctx.filesDir, "extractors")
                if (!extractorsDir.exists()) extractorsDir.mkdirs()
                
                val fileName = "$name.js"
                val outFile = File(extractorsDir, fileName)
                outFile.writeText(code)
                com.unan.nexxxup.plugins.JsExtractorLoader.loadCode(code, fileName)
                
                withContext(Dispatchers.Main) {
                    showToast(activity, "Berhasil mengunduh dan memuat $name", android.widget.Toast.LENGTH_SHORT)
                    loadData()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(activity, "Gagal mengunduh: ${e.message}", android.widget.Toast.LENGTH_SHORT)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
