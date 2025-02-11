package yu.desk.mococomic.presentation.comic

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentComicDetailBinding
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.ComicDetail
import yu.desk.mococomic.presentation.adapter.ListChapterAdapter
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.utils.*
import kotlin.math.abs

@AndroidEntryPoint
class ComicDetailFragment : Fragment() {
	private lateinit var binding: FragmentComicDetailBinding
	private val viewModel: ComicViewModel by hiltNavGraphViewModels(R.id.comicNavigation)
	private val navArgs by navArgs<ComicDetailFragmentArgs>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentComicDetailBinding.inflate(inflater, container, false)
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
			val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			val displayCutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
			binding.apply {
				toolbar.updateLayoutParams<MarginLayoutParams> {
					topMargin = systemBar.top
					leftMargin = displayCutout.left
					rightMargin = displayCutout.right
				}

				root.updateLayoutParams<MarginLayoutParams> {
					leftMargin = systemBar.left
					rightMargin = systemBar.right
				}

				scrollView.updatePadding(left = displayCutout.left, right = displayCutout.right)

				ivCover.updateLayoutParams<MarginLayoutParams> {
					leftMargin = displayCutout.left + 16.dp
				}

				btnRead.updateLayoutParams<MarginLayoutParams> {
					rightMargin = displayCutout.right + 16.dp
				}

				bottomMenu.updateLayoutParams<MarginLayoutParams> {
					bottomMargin = systemBar.bottom + 16.dp
					leftMargin = displayCutout.left + 16.dp
					rightMargin = displayCutout.right + 16.dp
				}

				navView.drawerContainer.updateLayoutParams<MarginLayoutParams> {
					topMargin = systemBar.top
					bottomMargin = systemBar.bottom
					leftMargin = displayCutout.left
					rightMargin = displayCutout.right
				}
			}

			insets
		}
		return binding.root
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		viewModel.setCurrentComic(navArgs.currentComic)
		viewModel.getComicDetail()
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

	private fun initObserver() {
		lifecycleScope.launch {
			viewModel.comicDetailResponse
				.flowWithLifecycle(lifecycle)
				.collectLatest {
					apiResponseHandler(
						uiState = it,
						onLoading = {
							onLoading()
						},
						onSuccess = { data ->
							onLoading(false)
							onSuccess(data)
						},
						onError = { msg ->
							onError(msg)
						},
					)
				}
		}

		lifecycleScope.launch {
			viewModel.saveFavoriteResponse
				.flowWithLifecycle(lifecycle)
				.collectLatest {
					apiResponseHandler(
						uiState = it,
						onLoading = {
							showLoading()
						},
						onError = { msg ->
							hideLoading()
							binding.root.showSnackBar(msg.toString())
						},
						onSuccess = { data ->
							hideLoading()
							binding.btnFavorite.isChecked = data.first == "save"
							binding.btnBottomFavorite.isChecked = data.first == "save"
							if (data.first == "save") {
								binding.root.showSnackBar(getString(R.string.text_comic_saved_favorite_successfully, data.second.title))
								findNavController().previousBackStackEntry?.savedStateHandle?.set(MyConstants.FAVORITE_COMIC, data.second.copy(isFavorite = true))
							} else {
								binding.root.showSnackBar(getString(R.string.text_comic_deleted_favorite_successfully, data.second.title))
								findNavController().previousBackStackEntry?.savedStateHandle?.set(MyConstants.FAVORITE_COMIC, data.second.copy(isFavorite = false))
							}
						},
					)
				}
		}

		lifecycleScope.launch {
			viewModel.chapterList.flowWithLifecycle(lifecycle).collectLatest { list ->
				binding.apply {
					navView.rvListChapter.initRecyclerView {
						return@initRecyclerView ListChapterAdapter().apply {
							setItem(list)
							setOnChapterClickListener { item ->
								binding.drawer.close()
								navigateToChapterDetail(item)
							}
						}
					}

					rvChapters.initRecyclerView {
						return@initRecyclerView ListChapterAdapter().apply {
							setItem(list.take(5))
							setOnChapterClickListener { item ->
								navigateToChapterDetail(item)
							}
						}
					}
				}
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
						binding.root.showSnackBar(getString(R.string.text_comic_blocked_successfully, data.title))
						findNavController().previousBackStackEntry?.savedStateHandle?.set(MyConstants.BLOCKED_COMIC, data)
						findNavController().popBackStack()
					},
					onError = { msg ->
						hideLoading()
						binding.root.showSnackBar(getString(R.string.text_comic_blocked_failed, msg))
					},
				)
			}
		}
	}

	private fun initView() {
		binding.apply {
			viewModel.comic.let {
				ivCoverBackground.loadImage(it.cover, enableLoading = false) {
					ivCoverBackground.scaleCropTop()
				}
				ivCover.loadImage(it.cover)
				tvTitle.text = it.title
				collapsingToolbar.title = it.title
				stateView.setToolbar(title = it.title)
			}
		}
	}

	private fun initListener() {
		binding.apply {
			scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
				// Handle swipe refresh layout not easy to scroll
				swipeRefresh.isEnabled = scrollY == 0
			}

			bottomMenu.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
			appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
				// Handle visible bottom menu when appbar collapse
				bottomMenu.setVisible((abs(verticalOffset) - appBarLayout.totalScrollRange) == 0)
				// Handle swipe refresh layout not easy to scroll
				swipeRefresh.isEnabled = verticalOffset == 0
			}

			btnMore.setOnClickListener { it ->
				val popupMenu = PopupMenu(requireContext(), it)
				popupMenu.menu.add(getString(R.string.text_share))
				popupMenu.menu.add(getString(R.string.text_block_comic))
				popupMenu.setOnMenuItemClickListener { menu ->
					when (menu.title) {
						getString(R.string.text_share) -> {}

						getString(R.string.text_block_comic) -> {
							viewModel.blockedComic(viewModel.comic)
						}
					}
					return@setOnMenuItemClickListener true
				}
				popupMenu.show()
			}

			tvTitle.setOnLongClickListener {
				requireContext().copyTextToClipboard(tvTitle.text.toString())
				return@setOnLongClickListener true
			}

			swipeRefresh.setOnRefreshListener {
				viewModel.getComicDetail()
			}

			btnBack.setOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			cvSynopsis.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)

			btnShowMore.setOnClickListener {
				if (btnShowMore.text == getString(R.string.text_show_more)) {
					tvSynopsis.maxLines = Integer.MAX_VALUE
					btnShowMore.text = getString(R.string.text_show_less)
				} else {
					tvSynopsis.maxLines = 10
					btnShowMore.text = getString(R.string.text_show_more)
				}
			}

			btnChapter.setOnClickListener {
				drawer.open()
			}

			btnBottomChapter.setOnClickListener {
				btnChapter.performClick()
			}

			btnRead.setOnClickListener {
				val chapter =
					viewModel.chapterList.value.firstOrNull { it.isAlreadyRead } ?: viewModel.chapterList.value.last()
				navigateToChapterDetail(chapter)
			}

			btnBottomRead.setOnClickListener {
				btnRead.performClick()
			}

			btnFavorite.setOnClickListener {
				btnFavorite.isChecked = viewModel.comic.isFavorite
				viewModel.setFavorite(viewModel.comic)
			}

			btnBottomFavorite.setOnClickListener {
				btnBottomFavorite.isChecked = viewModel.comic.isFavorite
				viewModel.setFavorite(viewModel.comic)
			}

			btnBottomComment.setOnClickListener {
				showLoading()
				lifecycleScope.launch {
					val url = viewModel.comic.getCommentUrl()
					requireContext().openTab(url)
					hideLoading()
				}
			}

			navView.btnClose.setOnClickListener {
				drawer.close()
			}

			stateView.addOnActionClickListener {
				viewModel.getComicDetail()
			}

			stateView.addOnBackClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}
		}
	}

	private fun onError(message: String?) {
		binding.apply {
			setSwipeRefreshOffset(StateView.ERROR)
			stateView.setState(StateView.ERROR)
			stateView.setError(description = message)
			swipeRefresh.isRefreshing = false
		}
	}

	private fun onLoading(isLoading: Boolean = true) {
		binding.apply {
			setSwipeRefreshOffset(StateView.CONTENT)
			stateView.setState(StateView.CONTENT)
			btnFavorite.isEnabled = !isLoading
			btnChapter.isEnabled = !isLoading
			btnRead.isEnabled = !isLoading
			btnBottomRead.isEnabled = !isLoading
			btnBottomChapter.isEnabled = !isLoading
			btnBottomFavorite.isEnabled = !isLoading
			btnBottomComment.isEnabled = !isLoading
			btnShowMore.setVisible(!isLoading)
			tvArtist.loadingShimmer(isLoading)
			ratingBar.loadingShimmer(isLoading)
			tvRating.loadingShimmer(isLoading)
			tvSynopsis.loadingShimmer(isLoading)
			tvAlternative.loadingShimmer(isLoading)
			tvGenre.loadingShimmer(isLoading)
			tvAuthor.loadingShimmer(isLoading)
			tvPublishDate.loadingShimmer(isLoading)
			tvPublisher.loadingShimmer(isLoading)
			tvStatus.loadingShimmer(isLoading)
			tvType.loadingShimmer(isLoading)
			rvChapters.loadingShimmer(isLoading)
		}
	}

	private fun onSuccess(data: ComicDetail) {
		binding.apply {
			ivCoverBackground.loadImage(data.cover, false) {
				ivCoverBackground.scaleCropTop()
				ivCover.loadImage(data.cover)
			}
			swipeRefresh.isRefreshing = false
			tvTitle.text = data.title.ifEmpty { "-" }
			collapsingToolbar.title = data.title.ifEmpty { "-" }
			tvArtist.text = getString(R.string.text_comic_by, data.author)
			ratingBar.rating = data.score.toFloatOrNull()?.div(2) ?: 0f
			tvRating.text = (data.score.toFloatOrNull()?.div(2) ?: "").toString().ifEmpty { "-" }
			btnFavorite.isChecked = data.comic.isFavorite
			btnBottomFavorite.isChecked = data.comic.isFavorite
			val isAlreadyRead = data.chapters.find { it.isAlreadyRead }?.isAlreadyRead ?: false
			val text = if (isAlreadyRead) R.string.text_continue else R.string.text_read
			btnRead.setText(text)
			btnBottomRead.setText(text)

			tvSynopsis.text = data.synopsis.ifEmpty { "-" }
			tvSynopsis.isTextOverflowing {
				btnShowMore.setVisible(it)
			}
			tvAlternative.text = data.subtitle.ifEmpty { "-" }
			tvGenre.text = data.genres.joinToString(", ").ifEmpty { "-" }
			tvAuthor.text = data.author.ifEmpty { "-" }
			tvPublishDate.text = data.published.ifEmpty { "-" }
			tvPublisher.text = data.serialization.ifEmpty { "-" }
			tvStatus.text = data.status.ifEmpty { "-" }
			tvType.text = data.comic.type.title

			navView.rvListChapter.initRecyclerView {
				return@initRecyclerView ListChapterAdapter().apply {
					setItem(viewModel.chapterList.value)
					setOnChapterClickListener { item ->
						binding.drawer.close()
						navigateToChapterDetail(item)
					}
				}
			}

			rvChapters.initRecyclerView {
				return@initRecyclerView ListChapterAdapter().apply {
					setItem(viewModel.chapterList.value.take(5))
					setOnChapterClickListener { item ->
						navigateToChapterDetail(item)
					}
				}
			}
		}
	}

	private fun setSwipeRefreshOffset(state: StateView.State) {
		binding.apply {
			if (state == StateView.ERROR) {
				val startOffset = binding.toolbar.layoutParams.height
				swipeRefresh.setProgressViewOffset(true, startOffset, startOffset + 64.dp)
			} else {
				val startOffset = getResourceStatusBarHeight()
				swipeRefresh.setProgressViewOffset(true, startOffset, startOffset + 64.dp)
			}
		}
	}

	private fun navigateToChapterDetail(chapter: Chapter) {
		// Reset chapter
		viewModel.setCurrentChapter(Chapter())
		val direction = ComicDetailFragmentDirections.actionComicDetailToComicReader(chapter)
		findNavController().navigateWithAnimation(direction)
	}
}