package com.unan.nexxxup.syncproviders.providers

import com.unan.nexxxup.R
import com.unan.nexxxup.syncproviders.AuthData
import com.unan.nexxxup.syncproviders.SyncAPI
import com.unan.nexxxup.syncproviders.SyncIdName
import com.unan.nexxxup.ui.WatchType
import com.unan.nexxxup.ui.library.ListSorting
import com.unan.nexxxup.ui.settings.Globals.TV
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.utils.Coroutines.ioWork
import com.unan.nexxxup.utils.DataStoreHelper.getAllFavorites
import com.unan.nexxxup.utils.DataStoreHelper.getAllSubscriptions
import com.unan.nexxxup.utils.DataStoreHelper.getAllWatchStateIds
import com.unan.nexxxup.utils.DataStoreHelper.getBookmarkedData
import com.unan.nexxxup.utils.DataStoreHelper.getResultWatchState
import com.unan.nexxxup.utils.txt

class LocalList : SyncAPI() {
    override val name = "Local"
    override val idPrefix = "local"

    override val icon: Int = R.drawable.ic_baseline_storage_24
    override val requiresLogin = false
    override val createAccountUrl = null
    override var requireLibraryRefresh = true
    override val syncIdName = SyncIdName.LocalList

    override suspend fun library(auth : AuthData?): SyncAPI.LibraryMetadata? {
        val watchStatusIds = ioWork {
            getAllWatchStateIds()?.map { id ->
                Pair(id, getResultWatchState(id))
            }
        }?.distinctBy { it.first } ?: return null

        val list = ioWork {
            val isTrueTv = isLayout(TV)

            val baseMap = WatchType.entries.filter { it != WatchType.NONE }.associate {
                // None is not something to display
                it.stringRes to emptyList<SyncAPI.LibraryItem>()
            } + mapOf(
                R.string.favorites_list_name to emptyList()
            ) + if (!isTrueTv) {
                mapOf(
                    R.string.subscription_list_name to emptyList()
                )
            } else {
                emptyMap()
            }

            val watchStatusMap = watchStatusIds.groupBy { it.second.stringRes }.mapValues { group ->
                group.value.mapNotNull {
                    getBookmarkedData(it.first)?.toLibraryItem(it.first.toString())
                }
            }

            val favoritesMap = mapOf(R.string.favorites_list_name to getAllFavorites().mapNotNull {
                it.toLibraryItem()
            })

            // Don't show subscriptions on TV
            val result = if (isTrueTv) {
                baseMap + watchStatusMap + favoritesMap
            } else {
                val subscriptionsMap =
                    mapOf(R.string.subscription_list_name to getAllSubscriptions().mapNotNull {
                        it.toLibraryItem()
                    })

                baseMap + watchStatusMap + subscriptionsMap + favoritesMap
            }

            result
        }

        return LibraryMetadata(
            list.map { LibraryList(txt(it.key), it.value) },
            setOf(
                ListSorting.AlphabeticalA,
                ListSorting.AlphabeticalZ,
                ListSorting.UpdatedNew,
                ListSorting.UpdatedOld,
                ListSorting.ReleaseDateNew,
                ListSorting.ReleaseDateOld,
//                ListSorting.RatingHigh,
//                ListSorting.RatingLow,

            )
        )
    }
}