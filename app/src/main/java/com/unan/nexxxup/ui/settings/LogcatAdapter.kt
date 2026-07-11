package com.unan.nexxxup.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import com.unan.nexxxup.databinding.ItemLogcatBinding
import com.unan.nexxxup.ui.BaseDiffCallback
import com.unan.nexxxup.ui.NoStateAdapter
import com.unan.nexxxup.ui.ViewHolderState

class LogcatAdapter() : NoStateAdapter<String>(
    diffCallback = BaseDiffCallback(
        itemSame = String::equals,
        contentSame = String::equals
    )
) {
    override fun onCreateContent(parent: ViewGroup): ViewHolderState<Any> {
        return ViewHolderState(
            ItemLogcatBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindContent(holder: ViewHolderState<Any>, item: String, position: Int) {
        (holder.view as? ItemLogcatBinding)?.apply {
            logText.text = item
        }
    }
}