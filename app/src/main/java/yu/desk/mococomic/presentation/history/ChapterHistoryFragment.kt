package yu.desk.mococomic.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentChapterHistoryBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.ChapterHistoryPagingDataAdapter
import yu.desk.mococomic.presentation.adapter.PagingFooterStateAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.presentation.dashboard.DashboardViewModel
import yu.desk.mococomic.utils.*
import javax.security.auth.login.LoginException

@AndroidEntryPoint
class ChapterHistoryFragment : Fragment() {
	private lateinit var binding: FragmentChapterHistoryBinding
	private val viewModel by viewModels<DashboardViewModel>()
	private val pagingAdapter: ChapterHistoryPagingDataAdapter by lazy { ChapterHistoryPagingDataAdapter() }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentChapterHistoryBinding.inflate(inflater, container, false)
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

	private fun initListener() {
		binding.apply {
			pagingAdapter.addLoadStateListener {
				val state =
					when {
						it.refresh is LoadState.Loading && pagingAdapter.itemCount < 1 -> StateView.LOADING

						it.refresh is LoadState.Error -> {
							val error = (it.refresh as LoadState.Error).error
							if (error is LoginException) {
								StateView.ALERT
							} else {
								StateView.ERROR
							}
						}

						it.append.endOfPaginationReached && pagingAdapter.itemCount < 1 -> StateView.EMPTY
						else -> StateView.CONTENT
					}
				swipeRefresh.isRefreshing = it.refresh is LoadState.Loading && pagingAdapter.itemCount > 0
				stateView.setState(state)

				when (state) {
					StateView.ERROR -> {
						val error = (it.refresh as LoadState.Error).error
						stateView.setError(description = error.message)
						stateView.addOnActionClickListener {
							pagingAdapter.refresh()
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
							navigateToLogin()
						}
					}

					StateView.EMPTY -> {
						stateView.setEmpty()
					}

					else -> Unit
				}
			}

			pagingAdapter.onItemClickListener {
				navigateToDetail(it)
			}

			swipeRefresh.setOnRefreshListener {
				pagingAdapter.refresh()
			}
			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}
		}
	}

	private fun initObserver() {
		viewModel.chapterHistoryPagingData.launchAndCollectLatest(viewLifecycleOwner) {
			pagingAdapter.submitData(it)
		}
	}

	private fun initView() {
		binding.apply {
			rvHistory.initRecyclerView { pagingAdapter.withLoadStateFooter(PagingFooterStateAdapter()) }
		}
	}

	private fun navigateToLogin() {
		lifecycleScope.launch {
			AuthHelper.signOut(requireContext()) {
				findNavController(R.id.navHostMain).navigateWithAnimation(
					R.id.authLogin,
					null,
					navOptions {
						popUpTo(R.id.authLogin) {
							inclusive = true
						}
						launchSingleTop = true
					},
				)
			}
		}
	}

	private fun navigateToDetail(comic: Comic) {
		findNavController(
			R.id.navHostMain,
		).navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle())
	}
}