package com.unan.nexxxup.ui.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unan.nexxxup.databinding.ResultSelectionBinding
import com.unan.nexxxup.ui.BaseDiffCallback
import com.unan.nexxxup.ui.NoStateAdapter
import com.unan.nexxxup.ui.ViewHolderState
import com.unan.nexxxup.ui.settings.Globals.TV
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.utils.UiText
import com.unan.nexxxup.utils.setText

typealias SelectData = Pair<UiText?, Any>

class SelectAdaptor(val callback: (Any) -> Unit) :
    NoStateAdapter<SelectData>(diffCallback = BaseDiffCallback(itemSame = { a, b ->
        a.second == b.second
    }, contentSame = { a, b ->
        a == b
    })) {
    private var selectedIndex: Int = -1

    override fun onCreateContent(parent: ViewGroup): ViewHolderState<Any> {
        return ViewHolderState(
            ResultSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindContent(holder: ViewHolderState<Any>, item: SelectData, position: Int) {
        when (val binding = holder.view) {
            is ResultSelectionBinding -> {
                binding.root.apply {
                    if (isLayout(TV)) {
                        isFocusable = true
                        isFocusableInTouchMode = true
                    }

                    isSelected = position == selectedIndex
                    setText(item.first)
                    setOnClickListener {
                        callback.invoke(item.second)
                    }
                }
            }
        }
    }

    override fun onViewDetachedFromWindow(holder: ViewHolderState<Any>) {
        if (holder.itemView.hasFocus()) {
            holder.itemView.clearFocus()
        }
    }

    fun select(newIndex: Int, recyclerView: RecyclerView?) {
        if (recyclerView == null) return
        if (newIndex == selectedIndex) return
        val oldIndex = selectedIndex
        selectedIndex = newIndex

        notifyItemChanged(selectedIndex)
        notifyItemChanged(oldIndex)
    }
}
