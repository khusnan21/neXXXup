package com.lagradost.cloudstream3.ui.settings.extractors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.databinding.RepositoryItemBinding
import com.lagradost.cloudstream3.plugins.JsExtractorRepoData

class JsExtractorReposAdapter(
    var repos: List<JsExtractorRepoData>,
    private val onDelete: (JsExtractorRepoData) -> Unit,
    private val onClick: (JsExtractorRepoData) -> Unit
) : RecyclerView.Adapter<JsExtractorReposAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RepositoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(repos[position])
    }

    override fun getItemCount() = repos.size

    inner class ViewHolder(private val binding: RepositoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(repo: JsExtractorRepoData) {
            binding.mainText.text = repo.name
            binding.subText.text = repo.url
            binding.actionButton.setImageResource(R.drawable.ic_baseline_delete_outline_24)
            binding.entryIcon.setImageResource(R.drawable.ic_baseline_extension_24)

            binding.actionButton.setOnClickListener {
                onDelete(repo)
            }
            
            binding.repositoryItemRoot.setOnClickListener {
                onClick(repo)
            }
        }
    }
}
