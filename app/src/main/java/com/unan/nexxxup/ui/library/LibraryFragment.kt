package com.unan.nexxxup.ui.library

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS
import android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.allViews
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.unan.nexxxup.APIHolder
import com.unan.nexxxup.APIHolder.allProviders
import com.unan.nexxxup.NexxxupApp.Companion.getKey
import com.unan.nexxxup.NexxxupApp.Companion.openBrowser
import com.unan.nexxxup.NexxxupApp.Companion.setKey
import com.unan.nexxxup.MainActivity
import com.unan.nexxxup.R
import com.unan.nexxxup.SearchResponse
import com.unan.nexxxup.databinding.FragmentLibraryBinding
import com.unan.nexxxup.mvvm.Resource
import com.unan.nexxxup.mvvm.debugAssert
import com.unan.nexxxup.mvvm.observe
import com.unan.nexxxup.syncproviders.SyncAPI
import com.unan.nexxxup.syncproviders.SyncIdName
import com.unan.nexxxup.ui.AutofitRecyclerView
import com.unan.nexxxup.ui.quicksearch.QuickSearchFragment
import com.unan.nexxxup.utils.txt
import com.unan.nexxxup.ui.BaseFragment
import com.unan.nexxxup.ui.home.ResumeItemAdapter
import com.unan.nexxxup.ui.home.HomeViewModel
import com.unan.nexxxup.utils.AppContextUtils.setDefaultFocus
import com.unan.nexxxup.utils.DataStoreHelper
import androidx.lifecycle.lifecycleScope
import androidx.appcompat.app.AlertDialog
import com.unan.nexxxup.mvvm.logError
import com.unan.nexxxup.ui.search.SEARCH_ACTION_LOAD
import com.unan.nexxxup.ui.search.SEARCH_ACTION_SHOW_METADATA
import com.unan.nexxxup.ui.settings.Globals.PHONE
import com.unan.nexxxup.ui.settings.Globals.isLandscape
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.utils.AppContextUtils.loadResult
import com.unan.nexxxup.utils.AppContextUtils.loadSearchResult
import com.unan.nexxxup.utils.AppContextUtils.reduceDragSensitivity
import com.unan.nexxxup.utils.DataStoreHelper.currentAccount
import com.unan.nexxxup.utils.SingleSelectionHelper.showBottomDialog
import com.unan.nexxxup.utils.SingleSelectionHelper.showDialog
import com.unan.nexxxup.utils.UIHelper.fixSystemBarsPadding
import com.unan.nexxxup.utils.UIHelper.getSpanCount
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.abs

const val LIBRARY_FOLDER = "library_folder"


enum class LibraryOpenerType(@StringRes val stringRes: Int) {
    Default(R.string.action_default),
    Provider(R.string.none),
    Browser(R.string.browser),
    Search(R.string.search),
    None(R.string.none),
}

/** Used to store how the user wants to open said poster */
data class LibraryOpener(
    val openType: LibraryOpenerType,
    val providerData: ProviderLibraryData?,
)

data class ProviderLibraryData(
    val apiName: String
)

class LibraryFragment : BaseFragment<FragmentLibraryBinding>(
    BaseFragment.BindingCreator.Bind(FragmentLibraryBinding::bind)
) {
    companion object {
        fun newInstance() = LibraryFragment()

        /**
         * Store which page was last seen when exiting the fragment and returning
         **/
        const val VIEWPAGER_ITEM_KEY = "viewpager_item"
    }

    private val libraryViewModel: LibraryViewModel by activityViewModels()

    private var toggleRandomButton = false

    override fun pickLayout(): Int? =
        if (isLayout(PHONE)) R.layout.fragment_library else R.layout.fragment_library_tv

    override fun onSaveInstanceState(outState: Bundle) {
        binding?.viewpager?.currentItem?.let { currentItem ->
            outState.putInt(VIEWPAGER_ITEM_KEY, currentItem)
        }
        super.onSaveInstanceState(outState)
    }

    private fun updateRandomVisibility(binding: FragmentLibraryBinding) {
        if (!toggleRandomButton) {
            binding.libraryRandom.isGone = true
            binding.libraryRandomButtonTv.isGone = true
            return
        }
        val position = libraryViewModel.currentPage.value ?: 0
        val pages = (libraryViewModel.pages.value as? Resource.Success)?.value ?: return
        val hasItems = pages[position].items.isNotEmpty()
        val isPhone = isLayout(PHONE)

        binding.libraryRandom.isVisible = isPhone && hasItems
        binding.libraryRandomButtonTv.isVisible = !isPhone && hasItems
    }

    override fun fixLayout(view: View) {
        fixSystemBarsPadding(
            view,
            padBottom = isLandscape(),
            padLeft = !isLayout(PHONE)
        )
    }

    @SuppressLint("ResourceType", "CutPasteId")
    override fun onBindingCreated(
        binding: FragmentLibraryBinding,
        savedInstanceState: Bundle?
    ) {
        binding.sortFab.setOnClickListener(sortChangeClickListener)
        binding.librarySort.setOnClickListener(sortChangeClickListener)

        val historyAdapter = ResumeItemAdapter(
            clickCallback = { callback ->
                if (callback.action == com.unan.nexxxup.ui.search.SEARCH_ACTION_SHOW_METADATA) {
                    val context = binding.root.context ?: return@ResumeItemAdapter
                    if (callback.card is DataStoreHelper.ResumeWatchingResult) {
                        activity?.let { act ->
                            com.unan.nexxxup.utils.SingleSelectionHelper.apply {
                                act.showOptionSelectStringRes(
                                    callback.view,
                                    callback.card.posterUrl,
                                    listOf(
                                        R.string.action_open_watching,
                                        R.string.action_remove_watching
                                    ),
                                    listOf(
                                        R.string.action_open_play,
                                        R.string.action_open_watching,
                                        R.string.action_remove_watching
                                    )
                                ) { (isTv, actionId) ->
                                    when (actionId + if (isTv) 0 else 1) {
                                        0 -> { // play
                                            com.unan.nexxxup.ui.search.SearchHelper.handleSearchClickCallback(
                                                com.unan.nexxxup.ui.search.SearchClickCallback(
                                                    com.unan.nexxxup.ui.result.START_ACTION_RESUME_LATEST,
                                                    callback.view,
                                                    -1,
                                                    callback.card
                                                )
                                            )
                                        }
                                        1 -> { // info
                                            com.unan.nexxxup.ui.search.SearchHelper.handleSearchClickCallback(
                                                com.unan.nexxxup.ui.search.SearchClickCallback(
                                                    com.unan.nexxxup.ui.search.SEARCH_ACTION_LOAD,
                                                    callback.view,
                                                    -1,
                                                    callback.card
                                                )
                                            )
                                        }
                                        2 -> { // remove
                                            val parentId = (callback.card as DataStoreHelper.ResumeWatchingResult).parentId
                                            if (parentId != null) {
                                                DataStoreHelper.removeLastWatched(parentId)
                                                (binding.libraryHistoryRecycler.adapter as? ResumeItemAdapter)?.let { adapter ->
                                                    loadHistoryData(binding, adapter)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return@ResumeItemAdapter
                    }
                }
                com.unan.nexxxup.ui.search.SearchHelper.handleSearchClickCallback(callback)
            },
            removeCallback = { view ->
                try {
                    val context = view.context ?: return@ResumeItemAdapter
                    AlertDialog.Builder(context).apply {
                        setTitle(R.string.clear_history)
                        setMessage(
                            context.getString(R.string.delete_message).format(
                                context.getString(
                                    R.string.continue_watching
                                )
                            )
                        )
                        setNegativeButton(R.string.cancel) { _, _ -> /*NO-OP*/ }
                        setPositiveButton(R.string.delete) { _, _ ->
                            DataStoreHelper.deleteAllResumeStateIds()
                            (binding.libraryHistoryRecycler.adapter as? ResumeItemAdapter)?.let {
                                loadHistoryData(binding, it)
                            }
                        }
                        show().setDefaultFocus()
                    }
                } catch (t: Throwable) {
                    logError(t)
                }
            }
        )

        binding.libraryHistoryRecycler.apply {
            adapter = historyAdapter
            spanCount = context.getSpanCount()
        }

        val mainTabs = binding.libraryMainTabLayout
        val historyTab = mainTabs.newTab().setText(R.string.library_tab_history)
        val favoritesTab = mainTabs.newTab().setText(R.string.library_tab_favorites)
        mainTabs.addTab(historyTab)
        mainTabs.addTab(favoritesTab)

        mainTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab == historyTab) {
                    showHistory(binding, historyAdapter)
                } else {
                    showFavorites(binding)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        // By default select History tab
        mainTabs.selectTab(historyTab)
        showHistory(binding, historyAdapter)

        binding.libraryRoot.findViewById<TextView>(androidx.appcompat.R.id.search_src_text)
            ?.apply {
                tag = "tv_no_focus_tag"
                // Expand the Appbar when search bar is focused, fixing scroll up issue
                setOnFocusChangeListener { _, _ ->
                    binding.searchBar.setExpanded(true)
                }
            }

        val searchCallback = Runnable {
            val newText = binding.mainSearch.query.toString()
            libraryViewModel.sort(ListSorting.Query, newText)
        }

        binding.mainSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                libraryViewModel.sort(ListSorting.Query, query)
                return true
            }

            // This is required to prevent the first text change
            // When this is attached it'll immediately send a onQueryTextChange("")
            // Which we do not want
            var hasInitialized = false
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!hasInitialized) {
                    hasInitialized = true
                    return true
                }

                binding.mainSearch.removeCallbacks(searchCallback)

                // Delay the execution of the search operation by 1 second (adjust as needed)
                // this prevents running search when the user is typing
                binding.mainSearch.postDelayed(searchCallback, 1000)

                return true
            }
        })

        libraryViewModel.reloadPages(false)

        binding.listSelector.setOnClickListener {
            val items = libraryViewModel.availableApiNames
            val currentItem = libraryViewModel.currentApiName.value

            activity?.showBottomDialog(
                items,
                items.indexOf(currentItem),
                txt(R.string.select_library).asString(it.context),
                false,
                {}) { index ->
                val selectedItem = items.getOrNull(index) ?: return@showBottomDialog
                libraryViewModel.switchList(selectedItem)
            }
        }

        //Load value for toggling Random button. Hide at startup
        context?.let {
            val settingsManager = PreferenceManager.getDefaultSharedPreferences(it)
            toggleRandomButton =
                settingsManager.getBoolean(
                    getString(R.string.random_button_key),
                    false
                )
            binding.libraryRandom.visibility = View.GONE
            binding.libraryRandomButtonTv.visibility = View.GONE
        }

        /**
         * Shows a plugin selection dialogue and saves the response
         **/
        fun Activity.showPluginSelectionDialog(
            key: String,
            syncId: SyncIdName,
            apiName: String? = null,
        ) {
            val availableProviders = synchronized(allProviders) {
                allProviders.filter {
                    it.supportedSyncNames.contains(syncId)
                }.map { it.name } +
                        // Add the api if it exists
                        (APIHolder.getApiFromNameNull(apiName)?.let { listOf(it.name) }
                            ?: emptyList())
            }
            val baseOptions = listOf(
                LibraryOpenerType.Default,
                LibraryOpenerType.None,
                LibraryOpenerType.Browser,
                LibraryOpenerType.Search
            )

            val items = baseOptions.map { txt(it.stringRes).asString(this) } + availableProviders

            val savedSelection = getKey<LibraryOpener>("$currentAccount/$LIBRARY_FOLDER", key)
            val selectedIndex =
                when {
                    savedSelection == null -> 0
                    // If provider
                    savedSelection.openType == LibraryOpenerType.Provider
                            && savedSelection.providerData?.apiName != null -> {
                        availableProviders.indexOf(savedSelection.providerData.apiName)
                            .takeIf { it != -1 }
                            ?.plus(baseOptions.size) ?: 0
                    }
                    // Else base option
                    else -> baseOptions.indexOf(savedSelection.openType)
                }

            this.showBottomDialog(
                items,
                selectedIndex,
                txt(R.string.open_with).asString(this),
                false,
                {},
            ) {
                val savedData = if (it < baseOptions.size) {
                    LibraryOpener(
                        baseOptions[it],
                        null
                    )
                } else {
                    LibraryOpener(
                        LibraryOpenerType.Provider,
                        ProviderLibraryData(items[it])
                    )
                }

                setKey(
                    "$currentAccount/$LIBRARY_FOLDER",
                    key,
                    savedData,
                )
            }
        }

        binding.providerSelector.setOnClickListener {
            val syncName = libraryViewModel.currentSyncApi?.syncIdName ?: return@setOnClickListener
            activity?.showPluginSelectionDialog(syncName.name, syncName)
        }

        binding.viewpager.setPageTransformer(LibraryScrollTransformer())

        binding.viewpager.adapter = ViewpagerAdapter(
            { isScrollingDown: Boolean ->
                if (isScrollingDown) {
                    binding.sortFab.shrink()
                    binding.libraryRandom.shrink()
                } else {
                    binding.sortFab.extend()
                    binding.libraryRandom.extend()
                }
            }) callback@{ searchClickCallback ->
            // To prevent future accidents
            debugAssert({
                searchClickCallback.card !is SyncAPI.LibraryItem
            }, {
                "searchClickCallback ${searchClickCallback.card} is not a LibraryItem"
            })

            val syncId = (searchClickCallback.card as SyncAPI.LibraryItem).syncId
            val syncName =
                libraryViewModel.currentSyncApi?.syncIdName ?: return@callback

            when (searchClickCallback.action) {
                SEARCH_ACTION_SHOW_METADATA -> {
                    (activity as? MainActivity)?.loadPopup(
                        searchClickCallback.card,
                        load = false
                    )
                    /*activity?.showPluginSelectionDialog(
                            syncId,
                            syncName,
                            searchClickCallback.card.apiName
                        )*/
                }

                SEARCH_ACTION_LOAD -> {
                    loadLibraryItem(syncName, syncId, searchClickCallback.card)
                }
            }
        }

        binding.apply {
            viewpager.offscreenPageLimit = 2
            viewpager.reduceDragSensitivity()
            searchBar.setExpanded(true)
        }

        val startLoading = Runnable {
            binding.apply {
                gridview.numColumns = root.context.getSpanCount()
                gridview.adapter =
                    context?.let { LoadingPosterAdapter(it, 6 * 3) }
                libraryLoadingOverlay.isVisible = true
                libraryLoadingShimmer.startShimmer()
                emptyListTextview.isVisible = false
            }
        }

        val stopLoading = Runnable {
            binding.apply {
                gridview.adapter = null
                libraryLoadingOverlay.isVisible = false
                libraryLoadingShimmer.stopShimmer()
            }
        }

        val handler = Handler(Looper.getMainLooper())

        observe(libraryViewModel.pages) { resource ->
            when (resource) {
                is Resource.Success -> {
                    handler.removeCallbacks(startLoading)
                    val pages = resource.value
                    val showNotice = pages.all { it.items.isEmpty() }

                    binding.apply {
                        emptyListTextview.isVisible = showNotice
                        if (showNotice) {
                            if (libraryViewModel.availableApiNames.size > 1) {
                                emptyListTextview.setText(R.string.empty_library_logged_in_message)
                            } else {
                                emptyListTextview.setText(R.string.empty_library_no_accounts_message)
                            }
                        }

                        (viewpager.adapter as? ViewpagerAdapter)?.submitList(pages.map {
                            it.copy(
                                items = CopyOnWriteArrayList(it.items)
                            )
                        })
                        //fix focus on the viewpager itself
                        (viewpager.getChildAt(0) as RecyclerView).apply {
                            tag = "tv_no_focus_tag"
                            //isFocusable = false
                        }

                        // Using notifyItemRangeChanged keeps the animations when sorting
                        /*viewpager.adapter?.notifyItemRangeChanged(
                            0,
                            viewpager.adapter?.itemCount ?: 0
                        )*/

                        libraryViewModel.currentPage.value?.let { page ->
                            binding.viewpager.setCurrentItem(page, false)
                            binding.searchBar.setExpanded(true)
                        }

                        // Set up random button click listener
                        if (toggleRandomButton) {
                            val randomClickListener = View.OnClickListener {
                                val position = libraryViewModel.currentPage.value ?: 0
                                val syncIdName = libraryViewModel.currentSyncApi?.syncIdName ?: return@OnClickListener
                                pages[position].items.randomOrNull()?.let { item ->
                                    loadLibraryItem(syncIdName, item.syncId, item)
                                }
                            }
                            libraryRandom.setOnClickListener(randomClickListener)
                            libraryRandomButtonTv.setOnClickListener(randomClickListener)
                        }
                        updateRandomVisibility(binding)

                        // Only stop loading after 300ms to hide the fade effect the viewpager produces when updating
                        // Without this there would be a flashing effect:
                        // loading -> show old viewpager -> black screen -> show new viewpager
                        handler.postDelayed(stopLoading, 300)

                        savedInstanceState?.getInt(VIEWPAGER_ITEM_KEY)?.let { currentPos ->
                            if (currentPos < 0) return@let
                            viewpager.setCurrentItem(currentPos, false)
                            // Using remove() sets the key to 0 instead of removing it
                            savedInstanceState.putInt(VIEWPAGER_ITEM_KEY, -1)
                        }

                        // Since the animation to scroll multiple items is so much its better to just hide
                        // the viewpager a bit while the fastest animation is running
                        fun hideViewpager(distance: Int) {
                            if (distance < 3) return

                            val hideAnimation = AlphaAnimation(1f, 0f).apply {
                                duration = distance * 50L
                                fillAfter = true
                            }
                            val showAnimation = AlphaAnimation(0f, 1f).apply {
                                duration = distance * 50L
                                startOffset = distance * 100L
                                fillAfter = true
                            }
                            viewpager.startAnimation(hideAnimation)
                            viewpager.startAnimation(showAnimation)
                        }

                        TabLayoutMediator(
                            libraryTabLayout,
                            viewpager,
                        ) { tab, position ->
                            tab.text = pages.getOrNull(position)?.title?.asStringNull(context)
                            tab.view.tag = "tv_no_focus_tag"
                            tab.view.nextFocusDownId = R.id.search_result_root

                            tab.view.setOnClickListener {
                                val currentItem = binding.viewpager.currentItem
                                val distance = abs(position - currentItem)
                                hideViewpager(distance)
                            }
                            //Expand the appBar on tab focus
                            tab.view.setOnFocusChangeListener { _, _ ->
                                binding.searchBar.setExpanded(true)
                            }
                        }.attach()

                        binding.libraryTabLayout.addOnTabSelectedListener(object :
                            TabLayout.OnTabSelectedListener {
                            override fun onTabSelected(tab: TabLayout.Tab?) {
                                binding.libraryTabLayout.selectedTabPosition.let { page ->
                                    libraryViewModel.switchPage(page)
                                }
                            }

                            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
                            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
                        })
                    }
                }

                is Resource.Loading -> {
                    // Only start loading after 200ms to prevent loading cached lists
                    handler.postDelayed(startLoading, 200)
                }

                is Resource.Failure -> {
                    stopLoading.run()
                    // No user indication it failed :(
                    // TODO
                }
            }
        }

        observe(libraryViewModel.currentPage) { position ->
            updateRandomVisibility(binding)
            val all = binding.viewpager.allViews.toList()
                .filterIsInstance<AutofitRecyclerView>()

            all.forEach { view ->
                view.isVisible = view.tag == position
                view.isFocusable = view.tag == position

                if (view.tag == position)
                    view.descendantFocusability = FOCUS_AFTER_DESCENDANTS
                else
                    view.descendantFocusability = FOCUS_BLOCK_DESCENDANTS
            }
        }
    }

    private fun loadLibraryItem(
        syncName: SyncIdName,
        syncId: String,
        card: SearchResponse
    ) {
        // This basically first selects the individual opener and if that is default then
        // selects the whole list opener
        val savedListSelection =
            getKey<LibraryOpener>("$currentAccount/$LIBRARY_FOLDER", syncName.name)

        val savedSelection = getKey<LibraryOpener>(
            "$currentAccount/$LIBRARY_FOLDER",
            syncId
        ).takeIf {
            it?.openType != LibraryOpenerType.Default
        } ?: savedListSelection

        when (savedSelection?.openType) {
            null, LibraryOpenerType.Default -> {
                // Prevents opening MAL/AniList as a provider
                if (APIHolder.getApiFromNameNull(card.apiName) != null) {
                    activity?.loadSearchResult(
                        card
                    )
                } else {
                    // Search when no provider can open
                    QuickSearchFragment.pushSearch(
                        activity,
                        card.name
                    )
                }
            }

            LibraryOpenerType.None -> {}
            LibraryOpenerType.Provider ->
                savedSelection.providerData?.apiName?.let { apiName ->
                    activity?.loadResult(
                        card.url,
                        apiName,
                        card.name
                    )
                }

            LibraryOpenerType.Browser ->
                openBrowser(card.url)

            LibraryOpenerType.Search -> {
                QuickSearchFragment.pushSearch(
                    activity,
                    card.name
                )
            }
        }

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val adapter = binding?.viewpager?.adapter ?: return
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
    }

    private val sortChangeClickListener = View.OnClickListener { view ->
        val methods = libraryViewModel.sortingMethods.map {
            txt(it.stringRes).asString(view.context)
        }

        activity?.showDialog(
            methods,
            libraryViewModel.sortingMethods.indexOf(libraryViewModel.currentSortingMethod),
            txt(R.string.sort_by).asString(view.context),
            false,
            {},
            {
                val method = libraryViewModel.sortingMethods[it]
                libraryViewModel.sort(method)
            })
    }

    fun loadHistoryData(binding: FragmentLibraryBinding, historyAdapter: ResumeItemAdapter) {
        lifecycleScope.launchWhenStarted {
            val resumeWatching = HomeViewModel.getResumeWatching() ?: emptyList()
            val hasHistory = resumeWatching.isNotEmpty()
            binding.libraryHistoryEmptyText.isVisible = !hasHistory
            binding.libraryHistoryRecycler.isVisible = hasHistory
            historyAdapter.submitList(resumeWatching)
        }
    }

    private fun showHistory(binding: FragmentLibraryBinding, historyAdapter: ResumeItemAdapter) {
        binding.libraryHistoryHolder.visibility = View.VISIBLE
        binding.libraryFavoritesHolder.visibility = View.GONE
        binding.libraryTabLayout.visibility = View.GONE
        loadHistoryData(binding, historyAdapter)
    }

    private fun showFavorites(binding: FragmentLibraryBinding) {
        binding.libraryHistoryHolder.visibility = View.GONE
        binding.libraryFavoritesHolder.visibility = View.VISIBLE
        binding.libraryTabLayout.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        binding?.let { b ->
            val historyAdapter = b.libraryHistoryRecycler.adapter as? ResumeItemAdapter
            if (historyAdapter != null) {
                loadHistoryData(b, historyAdapter)
            }
        }
    }
}

class MenuSearchView(context: Context) : SearchView(context)