package com.unan.nexxxup.ui.result

import android.app.SearchManager
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.unan.nexxxup.ActorData
import com.unan.nexxxup.ActorRole
import com.unan.nexxxup.R
import com.unan.nexxxup.databinding.CastItemBinding
import com.unan.nexxxup.ui.BaseDiffCallback
import com.unan.nexxxup.ui.NoStateAdapter
import com.unan.nexxxup.ui.ViewHolderState
import com.unan.nexxxup.ui.newSharedPool
import com.unan.nexxxup.ui.settings.Globals.PHONE
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.utils.ImageLoader.loadImage

class ActorAdaptor(
    private var nextFocusUpId: Int? = null,
    private val focusCallback: (View?) -> Unit = {}
) : NoStateAdapter<ActorData>(diffCallback = BaseDiffCallback(itemSame = { a, b ->
    a.actor.name == b.actor.name
})) {
    companion object {
        val sharedPool =
            newSharedPool { setMaxRecycledViews(CONTENT, 10) }
    }

    // Easier to store it here than to store it in the ActorData
    val inverted: HashMap<ActorData, Boolean> = hashMapOf()

    override fun onCreateContent(parent: ViewGroup): ViewHolderState<Any> {
        return ViewHolderState(
            CastItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onClearView(holder: ViewHolderState<Any>) {
        when (val binding = holder.view) {
            is CastItemBinding -> {
                clearImage(binding.actorImage)
            }
        }
    }

    override fun onBindContent(holder: ViewHolderState<Any>, item: ActorData, position: Int) {
        when (val binding = holder.view) {
            is CastItemBinding -> {
                val itemView = binding.root
                val isInverted = inverted.getOrDefault(item, false)

                val (mainImg, vaImage) = if (!isInverted || item.voiceActor?.image.isNullOrBlank()) {
                    Pair(item.actor.image, item.voiceActor?.image)
                } else {
                    Pair(item.voiceActor?.image, item.actor.image)
                }

                // Fix tv focus escaping the recyclerview
                if (position == 0) {
                    itemView.nextFocusLeftId = R.id.result_cast_items
                } else if ((position - 1) == itemCount) {
                    itemView.nextFocusRightId = R.id.result_cast_items
                }
                nextFocusUpId?.let {
                    itemView.nextFocusUpId = it
                }

                itemView.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        focusCallback(v)
                    }
                }

                itemView.setOnClickListener {
                    inverted[item] = !isInverted
                    this.onUpdateContent(holder, getItem(position), position)
                }

                itemView.setOnLongClickListener {
                    if (isLayout(PHONE)) {
                        Intent(Intent.ACTION_WEB_SEARCH).apply {
                            putExtra(SearchManager.QUERY, item.actor.name)
                        }.also { intent ->
                            itemView.context.packageManager?.let { pm ->
                                if (intent.resolveActivity(pm) != null) {
                                    itemView.context.startActivity(intent)
                                }
                            }
                        }
                    }
                    true
                }

                binding.apply {
                    actorImage.loadImage(mainImg)

                    actorName.text = item.actor.name
                    item.role?.let {
                        actorExtra.context?.getString(
                            when (it) {
                                ActorRole.Main -> {
                                    R.string.actor_main
                                }

                                ActorRole.Supporting -> {
                                    R.string.actor_supporting
                                }

                                ActorRole.Background -> {
                                    R.string.actor_background
                                }
                            }
                        )?.let { text ->
                            actorExtra.isVisible = true
                            actorExtra.text = text
                        }
                    } ?: item.roleString?.let {
                        actorExtra.isVisible = true
                        actorExtra.text = it
                    } ?: run {
                        actorExtra.isVisible = false
                    }

                    if (item.voiceActor == null) {
                        voiceActorImageHolder.isVisible = false
                        voiceActorName.isVisible = false
                    } else {
                        voiceActorName.text = item.voiceActor?.name
                        if (!vaImage.isNullOrEmpty())
                            voiceActorImageHolder.isVisible = true
                        voiceActorImage.loadImage(vaImage)
                    }
                }
            }
        }
    }
}