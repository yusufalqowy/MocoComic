package yu.desk.mococomic.presentation.history

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentChapterHistoryBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.FirebaseChapter
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
	private val deletedItems = mutableListOf<FirebaseChapter>()

	companion object {
		const val RESULT_KEY = "chapter_history_result_key"
	}

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
						stateView.setError(description = error.localizedMessage)
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

			pagingAdapter.onRemoveItemClickListener {
				viewModel.deleteChapterHistory(it)
			}

			swipeRefresh.setOnRefreshListener {
				pagingAdapter.refresh()
			}

			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			etSearch.doOnTextChanged { text, _, _, _ ->
				ilSearch.endIconMode = if (!text.isNullOrEmpty()) TextInputLayout.END_ICON_CLEAR_TEXT else TextInputLayout.END_ICON_NONE
				ilSearch.setEndIconDrawable(if (!text.isNullOrEmpty()) R.drawable.ic_close_circle_fill else R.drawable.ic_search)
			}

			etSearch.setOnEditorActionListener { _, actionId, _ ->
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if (!etSearch.text.isNullOrBlank()) {
						viewModel.searchChapterHistory(etSearch.text.toString())
					} else {
						stateView.setState(StateView.EMPTY)
					}
					hideKeyboard()
					return@setOnEditorActionListener true
				}
				return@setOnEditorActionListener false
			}

			btnSearch.setOnClickListener {
				ilSearch.isEndIconVisible = true
				btnSearch.setVisible(false)
				etSearch.setText(viewModel.query.value)
				etSearch.requestFocus()
				showKeyboard()
				ilSearch.setVisible(true)
			}

			ilSearch.setStartIconOnClickListener {
				btnSearch.setVisible(true)
				ilSearch.setVisible(false)
				hideKeyboard()
				if (etSearch.text.isNullOrBlank()) {
					viewModel.searchChapterHistory("")
				}
			}
		}
	}

	private fun initObserver() {
		viewModel.chapterHistoryPagingData.launchAndCollectLatest(viewLifecycleOwner) {
			val pagingData = it.filter { item -> !deletedItems.contains(item) }
			pagingAdapter.submitData(pagingData)
		}

		findNavController().getBackStackEntry(R.id.chapterHistoryFragment).savedStateHandle.let {
			it.getLiveData<Boolean>(RESULT_KEY).observe(viewLifecycleOwner) { result ->
				if (result) {
					pagingAdapter.refresh()
					binding.rvHistory.scrollToPosition(0)
				}
			}
		}

		viewModel.query.launchAndCollectLatest(viewLifecycleOwner) {
			binding.etSearch.setText(it)
		}

		viewModel.deleteChapterHistoryResponse.launchAndCollectLatest(viewLifecycleOwner) {
			apiResponseHandler(
				uiState = it,
				onLoading = {
					showLoading()
				},
				onSuccess = { data ->
					hideLoading()
					binding.root.showSnackBar("Remove from history success")
					pagingAdapter.removeItem(data)
					deletedItems.add(data)
				},
				onError = {
					hideLoading()
					binding.root.showSnackBar("Remove from history failed")
				},
			)
		}
	}

	private fun initView() {
		binding.apply {
			toolbarContainer.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
			rvHistory.initRecyclerView { pagingAdapter.withLoadStateFooter(PagingFooterStateAdapter()) }
		}
	}

	private fun navigateToLogin() {
		lifecycleScope.launch {
			AuthHelper.signOut(requireContext()) {
				findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_chapterHistoryFragment_to_authLogin)
			}
		}
	}

	private fun hideKeyboard() {
		val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
	}

	private fun showKeyboard() {
		val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.showSoftInput(binding.etSearch, 0)
	}

	private fun navigateToDetail(comic: Comic) {
		findNavController(
			R.id.navHostMain,
		).navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle())
	}
}