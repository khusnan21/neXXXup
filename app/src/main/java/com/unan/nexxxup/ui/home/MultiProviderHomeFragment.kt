package com.unan.nexxxup.ui.home

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unan.api.Log
import com.unan.nexxxup.APIHolder.apis
import com.unan.nexxxup.HomePageList
import com.unan.nexxxup.R
import com.unan.nexxxup.databinding.FragmentMultiProviderHomeBinding
import com.unan.nexxxup.mvvm.Resource
import com.unan.nexxxup.ui.APIRepository
import com.unan.nexxxup.ui.APIRepository.Companion.noneApi
import com.unan.nexxxup.ui.BaseFragment
import com.unan.nexxxup.ui.setRecycledViewPool
import com.unan.nexxxup.ui.settings.Globals.EMULATOR
import com.unan.nexxxup.ui.settings.Globals.TV
import com.unan.nexxxup.utils.UIHelper.fixSystemBarsPadding
import com.unan.nexxxup.ui.settings.Globals.isLandscape
import com.unan.nexxxup.ui.settings.Globals.isLayout
import com.unan.nexxxup.MainActivity
import com.unan.nexxxup.MainAPI
import com.unan.nexxxup.utils.AppContextUtils.filterProviderByPreferredMedia
import com.unan.nexxxup.utils.UIHelper.navigate
import com.unan.nexxxup.utils.ImageLoader.loadImage
import com.unan.nexxxup.mvvm.observe
import com.unan.nexxxup.utils.DataStoreHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MultiProviderHomeFragment : BaseFragment<FragmentMultiProviderHomeBinding>(
    BindingCreator.Inflate(FragmentMultiProviderHomeBinding::inflate)
) {
    private val homeViewModel: HomeViewModel by activityViewModels()

    private var homeParentItemAdapter: ParentItemAdapter? = null
    private val loadedItems = mutableListOf<HomeViewModel.ExpandableHomepageList>()
    private var currentLoadedIndex = 0
    private var isLoading = false
    private var hasMore = true

    private fun getSortedHomeApis(): List<MainAPI> {
        val filtered = context?.filterProviderByPreferredMedia(hasHomePageIsRequired = true)
            ?.filter { it.name != noneApi.name }
            ?: synchronized(apis) {
                apis.filter { it.hasMainPage && it.name != noneApi.name }
            }
        return filtered.sortedBy { it.name.lowercase() }
    }

    override fun fixLayout(view: View) {
        fixSystemBarsPadding(
            view,
            padTop = false,
            padBottom = isLandscape(),
            padLeft = isLayout(TV or EMULATOR)
        )
    }

    override fun onResume() {
        super.onResume()
        MainActivity.afterPluginsLoadedEvent += ::onPluginsLoaded
        if (loadedItems.isEmpty()) {
            loadMoreProviders()
        }
    }

    override fun onStop() {
        super.onStop()
        MainActivity.afterPluginsLoadedEvent -= ::onPluginsLoaded
    }

    private fun onPluginsLoaded(success: Boolean) {
        activity?.runOnUiThread {
            if (loadedItems.isEmpty()) {
                currentLoadedIndex = 0
                hasMore = true
                isLoading = false
                homeParentItemAdapter?.submitList(emptyList())
                loadMoreProviders()
            }
        }
    }

    override fun onBindingCreated(binding: FragmentMultiProviderHomeBinding) {
        context?.let { HomeChildItemAdapter.updatePosterSize(it) }

        binding.apply {
            homeSearchIcon.setOnClickListener {
                activity?.navigate(R.id.navigation_quick_search)
            }
            homeDownloadIcon.setOnClickListener {
                activity?.navigate(R.id.navigation_downloads)
            }

            val adapter = ParentItemAdapter(
                id = "MultiProviderHome".hashCode(),
                clickCallback = { callback ->
                    com.unan.nexxxup.ui.search.SearchHelper.handleSearchClickCallback(callback)
                },
                moreInfoClickCallback = { item ->
                    val clickedProviderName = item.list.name
                    val api = com.unan.nexxxup.APIHolder.getApiFromNameNull(clickedProviderName)
                    if (api != null) {
                        com.unan.nexxxup.utils.DataStoreHelper.currentHomePage = api.name
                        homeViewModel.loadAndCancel(api.name, forceReload = true, fromUI = true)
                        findNavController().navigate(R.id.navigation_single_provider_home)
                    }
                }
            )
            homeParentItemAdapter = adapter

            homeMasterRecycler.setRecycledViewPool(ParentItemAdapter.sharedPool)
            homeMasterRecycler.adapter = adapter
            
            if (loadedItems.isNotEmpty()) {
                homeParentItemAdapter?.submitList(loadedItems.toMutableList())
            }

            homeMasterRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && hasMore) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2
                            && firstVisibleItemPosition >= 0
                        ) {
                            loadMoreProviders()
                        }
                    }
                }
            })

            homeReloadConnectionerror.setOnClickListener {
                loadedItems.clear()
                currentLoadedIndex = 0
                hasMore = true
                isLoading = false
                homeParentItemAdapter?.submitList(emptyList())
                loadMoreProviders()
            }
            
            multiHomeSwipeRefresh.setOnRefreshListener {
                loadedItems.clear()
                currentLoadedIndex = 0
                hasMore = true
                isLoading = false
                homeParentItemAdapter?.submitList(emptyList())
                loadMoreProviders()
                multiHomeSwipeRefresh.isRefreshing = false
            }
        }
    }

    private fun loadMoreProviders() {
        if (isLoading) return
        val allApis = getSortedHomeApis()
        if (currentLoadedIndex >= allApis.size) {
            hasMore = false
            
            if (loadedItems.isEmpty()) {
                binding?.homeLoadingError?.isVisible = true
                binding?.resultErrorText?.text = "No providers available. Please adjust your language/media settings."
            }
            
            binding?.homeLoading?.apply {
                stopShimmer()
                isGone = true
            }
            return
        }
        
        isLoading = true

        lifecycleScope.launch {
            binding?.homeLoading?.apply {
                if (loadedItems.isEmpty()) {
                    isVisible = true
                    startShimmer()
                    binding?.homeLoadingError?.isGone = true
                }
            }

            val apisToLoad = allApis.drop(currentLoadedIndex).take(5)
            if (apisToLoad.isEmpty()) {
                hasMore = false
                isLoading = false
                binding?.homeLoading?.apply {
                    stopShimmer()
                    isGone = true
                }
                return@launch
            }

            val deferredLoaded = apisToLoad.map { api ->
                async(Dispatchers.IO) {
                    try {
                        val repo = APIRepository(api)
                        val res = repo.getMainPage(1)
                        if (res is Resource.Success) {
                            val homepageResponses = res.value
                            val items = homepageResponses.flatMap { it?.items ?: emptyList() }
                                .flatMap { it.list }
                                .distinctBy { it.url }
                                .take(5)

                            if (items.isNotEmpty()) {
                                HomeViewModel.ExpandableHomepageList(
                                    list = HomePageList(api.name, items),
                                    currentPage = 1,
                                    hasNext = false
                                )
                            } else {
                                null
                            }
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("MultiProviderHome", "Error loading ${api.name}: ${e.message}")
                        null
                    }
                }
            }

            val results = deferredLoaded.awaitAll().filterNotNull()

            if (results.isNotEmpty()) {
                loadedItems.addAll(results)
                homeParentItemAdapter?.submitList(loadedItems.toMutableList())
                binding?.homeLoadingError?.isGone = true
            } else if (loadedItems.isEmpty() && currentLoadedIndex + apisToLoad.size >= allApis.size) {
                binding?.homeLoadingError?.isVisible = true
                binding?.resultErrorText?.text = "No providers could be loaded. Please check your internet connection."
            }

            currentLoadedIndex += apisToLoad.size
            hasMore = currentLoadedIndex < allApis.size
            isLoading = false
            
            if (results.isEmpty() && hasMore) {
                loadMoreProviders()
            } else {
                binding?.homeLoading?.apply {
                    stopShimmer()
                    isGone = true
                }
            }
        }
    }
}
