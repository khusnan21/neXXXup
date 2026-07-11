package com.unan.nexxxup.ui.settings.extractors

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unan.nexxxup.R
import com.unan.nexxxup.databinding.RepositoryItemBinding
import java.io.File

class ExtractorsAdapter(
    var extractors: List<File>,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<ExtractorsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RepositoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(extractors[position])
    }

    override fun getItemCount() = extractors.size

    inner class ViewHolder(private val binding: RepositoryItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: File) {
            binding.mainText.text = file.name
            binding.subText.text = "Local JS Extractor"
            binding.actionButton.setImageResource(R.drawable.ic_baseline_delete_outline_24)
            binding.entryIcon.setImageResource(R.drawable.ic_baseline_extension_24)

            binding.actionButton.setOnClickListener {
                onDelete(file)
            }
        }
    }
}
