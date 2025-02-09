package yu.desk.mococomic.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentExploreBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.ComicAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.component.ComicFilterBottomSheet
import yu.desk.mococomic.presentation.component.ComicFilterSideSheet
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.utils.*

@AndroidEntryPoint
class ExploreFragment : Fragment() {
	private lateinit var binding: FragmentExploreBinding
	private val viewModel by viewModels<DashboardViewModel>({ requireParentFragment() })
	private val rvExploreAdapter by lazy { ComicAdapter(showFavoriteIcon = true, showBlockMenu = true) }
	private lateinit var comicFilterBottomSheet: ComicFilterBottomSheet
	private lateinit var comicFilterSideSheet: ComicFilterSideSheet

	companion object {
		const val TAG = "ExploreFragment"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (viewModel.exploreResponse.value !is UIState.Success) {
			viewModel.getExplore()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		// Inflate the layout for this fragment
		binding = FragmentExploreBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
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
					},
				)
			}
		}

		lifecycleScope.launch {
			viewModel.blockedComicResponse.flowWithLifecycle(lifecycle).collectLatest {
				apiResponseHandler(
					uiState = it,
					onLoading = {
						showLoading()
					},
					onSuccess = { data ->
						hideLoading()
						binding.root.showSnackBar("Comic '${data.title}' blocked successfully!")
						rvExploreAdapter.removeItem(data)
					},
					onError = { msg ->
						hideLoading()
						binding.root.showSnackBar("Comic blocked failed!\n$msg")
					},
				)
			}
		}

		findNavController(R.id.navHostMain).currentBackStackEntry?.savedStateHandle?.let { stateHandle ->
			stateHandle.getLiveData<Comic>(MyConstants.FAVORITE_COMIC).observe(viewLifecycleOwner) {
				rvExploreAdapter.changeItem(it)
			}
			stateHandle.getLiveData<Comic>(MyConstants.BLOCKED_COMIC).observe(viewLifecycleOwner) {
				rvExploreAdapter.removeItem(it)
			}
		}
	}

	private fun onError(message: String?) {
		binding.apply {
			stateView.setState(StateView.ERROR)
			stateView.setError(description = message, actionButtonText = "")
			swipeRefresh.isRefreshing = false
		}
	}

	private fun onSuccess(data: List<Comic>) {
		binding.apply {
			stateView.setState(StateView.CONTENT)
			rvExploreAdapter.setItems(data)
			swipeRefresh.isRefreshing = false
			(rv.rvComic.layoutManager as GridLayoutManager).let {
				it.spanSizeLookup =
					object : GridLayoutManager.SpanSizeLookup() {
						override fun getSpanSize(position: Int): Int =
							if (position == rvExploreAdapter.itemCount - 1 && viewModel.canLoadMore) {
								it.spanCount
							} else {
								1
							}
					}
			}
		}
	}

	private fun onLoading() {
		binding.apply {
			stateView.setState(StateView.CONTENT)
			rvExploreAdapter.setItems((1..12).toList())
			(rv.rvComic.layoutManager as GridLayoutManager).spanSizeLookup = GridLayoutManager.DefaultSpanSizeLookup()
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
			rvExploreAdapter.setOnLoadMoreClickListener {
				viewModel.loadMoreExplore()
			}

			rvExploreAdapter.setOnBlockComicClickListener {
				viewModel.blockedComic(it)
			}

			btnFilter.setOnClickListener {
				btnFilter.isChecked = isFilterEnable()
				viewModel.apply {
					if (requireContext().isSizeMobile()) {
						comicFilterBottomSheet.showBottomSheet(
							filterStatus,
							filterType,
							filterOrder,
							filterGenres,
						) { status, type, order, genres ->
							filterStatus = status
							filterType = type
							filterOrder = order
							filterGenres = genres
							btnFilter.isChecked = isFilterEnable()
							getExplore()
						}
					} else {
						comicFilterSideSheet.showSideSheet(
							filterStatus,
							filterType,
							filterOrder,
							filterGenres,
						) { status, type, order, genres ->
							filterStatus = status
							filterType = type
							filterOrder = order
							filterGenres = genres
							btnFilter.isChecked = isFilterEnable()
							getExplore()
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

			rv.rvComic.initRecyclerView {
				return@initRecyclerView rvExploreAdapter.apply {
					setItems((1..12).toList())
				}
			}
		}
	}

	private fun isFilterEnable() =
		viewModel.run {
			filterStatus != FilterStatus.All ||
				filterType != FilterType.All ||
				filterOrder != FilterOrder.LastUpdate ||
				filterGenres.isNotEmpty()
		}

	private fun navigateToComicDetail(comic: Comic) {
		findNavController(
			R.id.navHostMain,
		).navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle())
	}
}