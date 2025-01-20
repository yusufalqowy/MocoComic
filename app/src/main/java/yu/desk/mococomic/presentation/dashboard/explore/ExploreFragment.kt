package yu.desk.mococomic.presentation.dashboard.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentExploreBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.ComicAdapter
import yu.desk.mococomic.presentation.adapter.TabFilterAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.component.ComicFilterBottomSheet
import yu.desk.mococomic.presentation.component.ComicFilterSideSheet
import yu.desk.mococomic.presentation.component.TabFilter
import yu.desk.mococomic.presentation.component.TabGenre
import yu.desk.mococomic.presentation.dashboard.DashboardViewModel
import yu.desk.mococomic.utils.FilterOrder
import yu.desk.mococomic.utils.FilterStatus
import yu.desk.mococomic.utils.FilterType
import yu.desk.mococomic.utils.UIState
import yu.desk.mococomic.utils.apiResponseHandler
import yu.desk.mococomic.utils.findNavController
import yu.desk.mococomic.utils.initRecyclerView
import yu.desk.mococomic.utils.isSizeMobile
import yu.desk.mococomic.utils.navigateWithAnimation
import yu.desk.mococomic.utils.setVisible

class ExploreFragment : Fragment() {
    private lateinit var binding: FragmentExploreBinding
    private val viewModel by viewModels<DashboardViewModel>({ requireParentFragment() })
    private val rvExploreAdapter by lazy { ComicAdapter() }
    private lateinit var comicFilterBottomSheet: ComicFilterBottomSheet
    private lateinit var comicFilterSideSheet: ComicFilterSideSheet

    companion object{
        const val TAG = "ExploreFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.exploreResponse.value !is UIState.Success) {
            viewModel.getExplore()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.exploreResponse.flowWithLifecycle(lifecycle).collectLatest {
                apiResponseHandler(
                    uiState = it,
                    onLoading = {
                        onLoading()
                    },
                    onSuccess = { data ->
                        onSuccess(data)
                    },
                    onError = { message ->
                        onError(message)
                    }

                )
            }
        }
    }

    private fun onError(message: String?) {
        binding.apply {
            errorView.setVisible(true)
            rv.rvComic.setVisible(false)
            swipeRefresh.isRefreshing = false
        }
    }

    private fun onSuccess(data: List<Comic>) {
        binding.apply {
            rvExploreAdapter.setItems(data)
            rv.rvComic.setVisible(true)
            swipeRefresh.isRefreshing = false
        }
    }

    private fun onLoading() {
        binding.apply {
            rvExploreAdapter.setItems((1..12).toList())
            errorView.setVisible(false)
            rv.rvComic.setVisible(true)
        }
    }

    private fun initListener() {
        binding.apply {
            swipeRefresh.setOnRefreshListener {
                viewModel.getExplore()
            }

            rvExploreAdapter.setOnItemClickListener {
                navigateToComicDetail(it)
            }

            btnFilter.setOnClickListener {
                btnFilter.isChecked = isFilterEnable()
                viewModel.apply {
                    if (requireContext().isSizeMobile()) {
                        comicFilterBottomSheet.showBottomSheet(filterStatus, filterType, filterOrder, filterGenres) { status, type, order, genres ->
                            filterStatus = status
                            filterType = type
                            filterOrder = order
                            filterGenres = genres
                            btnFilter.isChecked = isFilterEnable()
                        }
                    } else {
                        comicFilterSideSheet.showSideSheet(filterStatus, filterType, filterOrder, filterGenres) { status, type, order, genres ->
                            filterStatus = status
                            filterType = type
                            filterOrder = order
                            filterGenres = genres
                            btnFilter.isChecked = isFilterEnable()
                        }
                    }
                }

            }
        }
    }

    private fun initView() {
        binding.apply {
            comicFilterSideSheet = ComicFilterSideSheet(requireContext(), childFragmentManager)
            comicFilterBottomSheet = ComicFilterBottomSheet(requireContext(), childFragmentManager)
            val tabFilter = TabFilter().also {
                it.filterStatus = viewModel.filterStatus
                it.filterType = viewModel.filterType
                it.filterOrder = viewModel.filterOrder
            }

            val tabGenre = TabGenre().also {
                it.filterGenres = viewModel.filterGenres
            }

            val tabAdapter = TabFilterAdapter(childFragmentManager, lifecycle)
            tabAdapter.setItems(listOf(tabFilter, tabGenre))

            rv.rvComic.initRecyclerView {
                return@initRecyclerView rvExploreAdapter.apply {
                    setItems((1..12).toList())
                }
            }
        }
    }

    private fun isFilterEnable() = viewModel.run { filterStatus != FilterStatus.All || filterType != FilterType.All || filterOrder != FilterOrder.LastUpdate || filterGenres.isNotEmpty() }
    private fun navigateToComicDetail(comic: Comic) {
        findNavController(R.id.navHostMain).navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle())
    }
}
