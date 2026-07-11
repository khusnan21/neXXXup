package com.lagradost.cloudstream3.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.lagradost.cloudstream3.MainActivity.Companion.afterPluginsLoadedEvent
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.databinding.FragmentDiscoverBinding
import com.lagradost.cloudstream3.ui.BaseFragment
import com.lagradost.cloudstream3.ui.home.HomeFragment
import com.lagradost.cloudstream3.ui.home.HomeViewModel
import com.lagradost.cloudstream3.ui.settings.Globals.isLandscape
import com.lagradost.cloudstream3.utils.AppContextUtils.filterProviderByPreferredMedia
import com.lagradost.cloudstream3.utils.Coroutines.main
import com.lagradost.cloudstream3.utils.DataStoreHelper
import com.lagradost.cloudstream3.utils.UIHelper.fixSystemBarsPadding
import com.lagradost.cloudstream3.utils.UIHelper.hideKeyboard

class DiscoverFragment : BaseFragment<FragmentDiscoverBinding>(
    BaseFragment.BindingCreator.Bind(FragmentDiscoverBinding::bind)
) {
    private val searchViewModel: SearchViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    class ProviderAdapter(
        private var providers: List<com.lagradost.cloudstream3.MainAPI>,
        private val clickCallback: (com.lagradost.cloudstream3.MainAPI) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {
        inner class ProviderViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
            val nameText: android.widget.TextView = view.findViewById(R.id.provider_name)
            val urlText: android.widget.TextView = view.findViewById(R.id.provider_url)
            val langBadge: android.widget.TextView = view.findViewById(R.id.provider_lang_badge)
            val adultBadge: android.widget.TextView = view.findViewById(R.id.provider_adult_badge)
            val card: android.view.View = view
            
            fun bind(api: com.lagradost.cloudstream3.MainAPI) {
                nameText.text = api.name
                urlText.text = api.mainUrl.substringAfter("://")
                langBadge.text = api.lang

                if (api.supportedTypes.contains(com.lagradost.cloudstream3.TvType.NSFW)) {
                    adultBadge.visibility = View.VISIBLE
                } else {
                    adultBadge.visibility = View.GONE
                }
                card.setOnClickListener {
                    clickCallback(api)
                }
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.provider_card_item, parent, false)
            return ProviderViewHolder(view)
        }
        override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
            holder.bind(providers[position])
        }
        override fun getItemCount() = providers.size
        fun updateData(newProviders: List<com.lagradost.cloudstream3.MainAPI>) {
            providers = newProviders
            notifyDataSetChanged()
        }
    }

    var selectedSearchTypes = mutableListOf<TvType>()

    override fun pickLayout(): Int? = R.layout.fragment_discover

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        )
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        hideKeyboard()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        afterPluginsLoadedEvent += ::reloadRepos
    }

    override fun onStop() {
        super.onStop()
        afterPluginsLoadedEvent -= ::reloadRepos
    }

    var selectedCategory = "all"

    private fun getProviderCategory(api: com.lagradost.cloudstream3.MainAPI): String {
        return when (api.lang) {
            "webcam" -> "webcam"
            "asia", "id" -> "asian"
            else -> "western"
        }
    }

    private fun updateTabStyles() {
        val binding = binding ?: return
        val context = context ?: return
        val activeBg = R.drawable.discover_tab_selected
        val inactiveBg = R.drawable.rounded_button_radius
        val activeTextColor = androidx.core.content.ContextCompat.getColor(context, R.color.white)
        val inactiveTextColor = androidx.core.content.ContextCompat.getColor(context, R.color.text_high_emphasis)
        val inactiveTintList = android.content.res.ColorStateList.valueOf(androidx.core.content.ContextCompat.getColor(context, R.color.obsidian_glass))

        val tabs = listOf(
            Triple(binding.tabAll, "all", R.drawable.ic_baseline_language_24),
            Triple(binding.tabAsian, "asian", R.drawable.ic_baseline_star_24),
            Triple(binding.tabWestern, "western", R.drawable.ic_baseline_tv_24),
            Triple(binding.tabWebcam, "webcam", R.drawable.ic_baseline_ondemand_video_24)
        )

        for ((tab, key, drawableRes) in tabs) {
            val isActive = selectedCategory == key
            tab.setBackgroundResource(if (isActive) activeBg else inactiveBg)
            tab.backgroundTintList = if (isActive) null else inactiveTintList
            tab.setTextColor(if (isActive) activeTextColor else inactiveTextColor)
            tab.setTypeface(null, if (isActive) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            
            val drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableRes)?.mutate()
            if (drawable != null) {
                val color = if (isActive) activeTextColor else inactiveTextColor
                androidx.core.graphics.drawable.DrawableCompat.setTint(drawable, color)
                tab.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
                tab.compoundDrawablePadding = 12
            }
        }
    }

    fun filterProviders() {
        val query = binding?.discoverSearch?.query?.toString()?.trim() ?: ""
        val allApis = com.lagradost.cloudstream3.APIHolder.apis.toMutableList()

        val filteredApis = allApis.filter { api ->
            val matchesCategory = when (selectedCategory) {
                "asian" -> getProviderCategory(api) == "asian"
                "western" -> getProviderCategory(api) == "western"
                "webcam" -> getProviderCategory(api) == "webcam"
                else -> true
            }
            val matchesQuery = query.isEmpty() || api.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }.sortedBy { it.name.lowercase() }

        binding?.discoverProviderListRecycler?.let { recycler ->
            (recycler.adapter as? ProviderAdapter)?.updateData(filteredApis)
        }
    }

    private fun reloadRepos(success: Boolean = false) = main {
        searchViewModel.reloadRepos()
        filterProviders()
    }

    override fun fixLayout(view: View) {
        fixSystemBarsPadding(
            view,
            padBottom = isLandscape(),
            padLeft = false
        )
    }

    override fun onBindingCreated(
        binding: FragmentDiscoverBinding,
        savedInstanceState: Bundle?
    ) {
        reloadRepos()
        binding.apply {
            val apis = com.lagradost.cloudstream3.APIHolder.apis.toMutableList().sortedBy { it.name.lowercase() }
            
            discoverProviderListRecycler.adapter = ProviderAdapter(apis) { api ->
                val act = activity
                if (act != null) {
                    DataStoreHelper.currentHomePage = api.name
                    homeViewModel.loadAndCancel(api.name, forceReload = true, fromUI = true)
                    findNavController().navigate(R.id.navigation_single_provider_home)
                }
            }

            tabAll.setOnClickListener {
                selectedCategory = "all"
                updateTabStyles()
                filterProviders()
            }
            tabAsian.setOnClickListener {
                selectedCategory = "asian"
                updateTabStyles()
                filterProviders()
            }
            tabWestern.setOnClickListener {
                selectedCategory = "western"
                updateTabStyles()
                filterProviders()
            }
            tabWebcam.setOnClickListener {
                selectedCategory = "webcam"
                updateTabStyles()
                filterProviders()
            }
        }

        selectedSearchTypes = DataStoreHelper.searchPreferenceTags.toMutableList()
        updateTabStyles()

        binding.discoverSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterProviders()
                binding.discoverSearch.let {
                    hideKeyboard(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterProviders()
                return true
            }
        })
    }
}
