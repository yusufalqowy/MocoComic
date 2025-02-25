package yu.desk.mococomic.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentFavoriteBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.ComicPagingDataAdapter
import yu.desk.mococomic.presentation.adapter.PagingFooterStateAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.utils.*
import yu.desk.mococomic.utils.AuthHelper
import javax.security.auth.login.LoginException

@AndroidEntryPoint
class FavoriteFragment : Fragment() {
	private lateinit var binding: FragmentFavoriteBinding
	private val viewModel by viewModels<DashboardViewModel>({ requireParentFragment() })
	private val dataAdapter by lazy { ComicPagingDataAdapter() }
	private val footerAdapter: PagingFooterStateAdapter by lazy { PagingFooterStateAdapter() }
	private val deletedItems = mutableListOf<Comic>()

	companion object {
		const val TAG = "FavoriteFragment"
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentFavoriteBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		initObserver()
		initView()
		initListener()
	}

	private fun initListener() {
		binding.apply {
			dataAdapter.onItemClickListener {
				navigateToComicDetail(it)
			}

			dataAdapter.onMenuRemoveClickListener {
				deletedItems.add(it)
				viewModel.deleteUserFavoriteComic(it)
			}

			swipeRefresh.setOnRefreshListener {
				dataAdapter.refresh()
			}
		}
	}

	private fun initObserver() {
		viewModel.favoriteComicPagingData.launchAndCollectLatest(viewLifecycleOwner) {
			val pagingData = it.filter { item -> !deletedItems.contains(item) }
			dataAdapter.submitData(pagingData)
		}

		viewModel.deleteUserFavorite.launchAndCollectLatest(viewLifecycleOwner) {
			apiResponseHandler(
				uiState = it,
				onLoading = {
					showLoading()
				},
				onSuccess = { data ->
					hideLoading()
					binding.root.showSnackBar("Remove from favorite success")
					dataAdapter.removeItem(data)
				},
				onError = {
					hideLoading()
					binding.root.showSnackBar("Remove from favorite failed")
				},
			)
		}

		dataAdapter.loadStateFlow.launchAndCollectLatest(viewLifecycleOwner) {
			binding.apply {
				val state =
					when {
						it.refresh is LoadState.Loading && dataAdapter.itemCount < 1 -> StateView.LOADING

						it.refresh is LoadState.Error -> {
							val error = (it.refresh as LoadState.Error).error
							if (error is LoginException) {
								StateView.ALERT
							} else {
								StateView.ERROR
							}
						}

						it.append.endOfPaginationReached && dataAdapter.itemCount < 1 -> StateView.EMPTY
						else -> StateView.CONTENT
					}
				swipeRefresh.isRefreshing = it.refresh is LoadState.Loading && dataAdapter.itemCount > 0
				stateView.setState(state)
				when (state) {
					StateView.ERROR -> {
						val error = (it.refresh as LoadState.Error).error
						stateView.setError(description = error.message)
						stateView.addOnActionClickListener {
							dataAdapter.refresh()
						}
					}

					StateView.ALERT -> {
						stateView.setAlert(
							title = getString(R.string.text_login_required),
							iconRes = R.drawable.ic_profile_outline,
							description = getString(R.string.text_need_login_to_access_feature),
							actionButtonText = getString(R.string.text_login),
						)
						stateView.addOnActionClickListener {
							lifecycleScope.launch {
								AuthHelper.signOut(requireContext()) {
									navigateToLogin()
								}
							}
						}
					}

					StateView.EMPTY -> {
						stateView.setEmpty()
					}

					else -> Unit
				}
			}
		}

		findNavController(R.id.navHostMain).currentBackStackEntry?.savedStateHandle?.let { stateHandle ->
			stateHandle.getLiveData<Comic>(MyConstants.FAVORITE_COMIC).observe(viewLifecycleOwner) {
				if (!it.isFavorite) {
					dataAdapter.removeItem(it)
				} else {
					dataAdapter.refresh()
				}
			}
			stateHandle.getLiveData<Comic>(MyConstants.BLOCKED_COMIC).observe(viewLifecycleOwner) {
				dataAdapter.removeItem(it)
			}
		}
	}

	private fun initView() {
		binding.apply {
			val state =
				when {
					dataAdapter.itemCount < 1 -> StateView.EMPTY
					else -> StateView.CONTENT
				}
			stateView.setState(state)
			(rvFavorite.rvComic.layoutManager as GridLayoutManager).apply {
				spanSizeLookup =
					object : SpanSizeLookup() {
						override fun getSpanSize(position: Int): Int =
							if (position == itemCount - 1 &&
								(
									footerAdapter.loadState is LoadState.Loading ||
										footerAdapter.loadState is LoadState.Error
								)
							) {
								spanCount
							} else {
								1
							}
					}
			}
			rvFavorite.rvComic.initRecyclerView {
				return@initRecyclerView dataAdapter.withLoadStateFooter(footerAdapter)
			}
		}
	}

	private fun navigateToLogin() {
		findNavController(R.id.navHostMain).navigate(R.id.action_dashboardMain_to_authLogin)
	}

	private fun navigateToComicDetail(comic: Comic) {
		findNavController(
			R.id.navHostMain,
		).navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle())
	}
}