package yu.desk.mococomic.presentation.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.updatePadding
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentSearchComicBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.ComicAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.utils.*

@AndroidEntryPoint
class SearchComicFragment : Fragment() {
	private lateinit var binding: FragmentSearchComicBinding
	private val viewModel: SearchViewModel by viewModels()
	private val comicAdapter by lazy { ComicAdapter(showBlockMenu = true, showFavoriteIcon = true) }

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentSearchComicBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		initListener()
		initObserver()
	}

	private fun initObserver() {
		viewModel.searchResponse.launchAndCollectLatest(viewLifecycleOwner) {
			apiResponseHandler(
				uiState = it,
				onLoading = {
					onLoading()
				},
				onSuccess = { data ->
					onSuccess(data)
				},
				onError = { msg ->
					onError(msg)
				},
			)
		}

		viewModel.blockedComicResponse.launchAndCollectLatest(viewLifecycleOwner) {
			apiResponseHandler(
				uiState = it,
				onLoading = {
					showLoading()
				},
				onSuccess = { data ->
					hideLoading()
					binding.root.showSnackBar("Comic '${data.title}' blocked successfully!")
					comicAdapter.removeItem(data)
				},
				onError = { msg ->
					hideLoading()
					binding.root.showSnackBar("Comic blocked failed!\n$msg")
				},
			)
		}

		findNavController(R.id.navHostMain).currentBackStackEntry?.savedStateHandle?.let { stateHandle ->
			stateHandle.getLiveData<Comic>(MyConstants.FAVORITE_COMIC).observe(viewLifecycleOwner) {
				comicAdapter.changeItem(it)
			}
			stateHandle.getLiveData<Comic>(MyConstants.BLOCKED_COMIC).observe(viewLifecycleOwner) {
				comicAdapter.removeItem(it)
			}
		}
	}

	private fun onError(message: String?) {
		binding.apply {
			stateView.setState(StateView.ERROR)
			stateView.setError(description = message)
			swipeRefresh.isRefreshing = false
		}
	}

	private fun onSuccess(data: List<Comic>) {
		binding.apply {
			swipeRefresh.isRefreshing = false
			stateView.setState(StateView.CONTENT)
			comicAdapter.setItems(data)
			stateView.setIsEmpty(data.isEmpty())
			(rvSearchComic.rvComic.layoutManager as GridLayoutManager).let {
				it.spanSizeLookup =
					object : GridLayoutManager.SpanSizeLookup() {
						override fun getSpanSize(position: Int): Int =
							if (position == comicAdapter.itemCount - 1 && viewModel.canLoadMore) {
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
			comicAdapter.setItems((1..12).toList())
			(rvSearchComic.rvComic.layoutManager as GridLayoutManager).spanSizeLookup = GridLayoutManager.DefaultSpanSizeLookup()
		}
	}

	private fun initView() {
		binding.apply {
			stateView.setState(StateView.EMPTY)
			stateView.setEmpty(description = "Enter Comic title to search...")
			swipeRefresh.isEnabled = false
			val offset = ilSearch.layoutParams.height
			swipeRefresh.setProgressViewOffset(true, offset, offset + swipeRefresh.progressViewEndOffset)
			rvSearchComic.rvComic.updatePadding(top = 16.dp)
			rvSearchComic.rvComic.initRecyclerView {
				return@initRecyclerView comicAdapter
			}
		}
	}

	private fun initListener() {
		binding.apply {
			etSearch.doOnTextChanged { text, _, _, _ ->
				ilSearch.endIconMode = if (!text.isNullOrEmpty()) TextInputLayout.END_ICON_CLEAR_TEXT else TextInputLayout.END_ICON_NONE
				ilSearch.setEndIconDrawable(if (!text.isNullOrEmpty()) R.drawable.ic_close_circle_fill else R.drawable.ic_search)
				swipeRefresh.isEnabled = !text.isNullOrEmpty() || viewModel.query.isNotBlank()
			}

			etSearch.setOnEditorActionListener { _, actionId, _ ->
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					if (!etSearch.text.isNullOrBlank()) {
						viewModel.getSearchComic(etSearch.text.toString())
					} else {
						stateView.setState(StateView.EMPTY)
					}
					hideKeyboard()
					return@setOnEditorActionListener true
				}
				return@setOnEditorActionListener false
			}

			ilSearch.setStartIconOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			swipeRefresh.setOnRefreshListener {
				etSearch.setText(viewModel.query)
				viewModel.getSearchComic(viewModel.query)
			}

			comicAdapter.setOnLoadMoreClickListener {
				etSearch.setText(viewModel.query)
				viewModel.loadMoreSearchComic()
			}

			comicAdapter.setOnItemClickListener {
				navigateToComicDetail(it)
			}

			comicAdapter.setOnBlockComicClickListener {
				viewModel.blockedComic(it)
			}
		}
	}

	private fun hideKeyboard() {
		val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
		imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
	}

	private fun navigateToComicDetail(comic: Comic) {
		findNavController(
			R.id.navHostMain,
		).navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle())
	}
}