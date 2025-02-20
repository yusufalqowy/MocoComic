package yu.desk.mococomic.presentation.comic

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.MarginLayoutParams
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentComicReaderBinding
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.ListChapterAdapter
import yu.desk.mococomic.presentation.adapter.ListImageChapterAdapter
import yu.desk.mococomic.presentation.component.ComicReaderBottomSheet
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.presentation.component.SwipeRightDetector
import yu.desk.mococomic.utils.*

@AndroidEntryPoint
class ComicReaderFragment :
	Fragment(),
	ComicReaderListener {
	private lateinit var binding: FragmentComicReaderBinding
	private val viewModel: ComicViewModel by hiltNavGraphViewModels(R.id.comicNavigation)
	private val navArgs: ComicReaderFragmentArgs by navArgs()
	private val bottomSheet by lazy { ComicReaderBottomSheet(requireContext(), viewModel, this) }
	private val listImageChapterAdapter by lazy { ListImageChapterAdapter() }
	private val autoScrollHandler = Handler(Looper.getMainLooper())
	private lateinit var autoScrollRunnable: Runnable
	private var minScrollSpeed = 20
	private var maxScrollSpeed = 100
	private val scrollStep = 20
	private val scrollSpeed = MutableStateFlow(minScrollSpeed)
	private val isAutoScrollEnable = MutableStateFlow(false)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (viewModel.chapter.slug.isEmpty()) {
			viewModel.setCurrentChapter(navArgs.currentChapter)
		}
		viewModel.getChapterDetail()
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentComicReaderBinding.inflate(inflater, container, false)
		binding.apply {
			ViewCompat.setOnApplyWindowInsetsListener(root) { _, inset ->
				val systemBar = inset.getInsets(WindowInsetsCompat.Type.systemBars())
				val displayCutout = inset.getInsets(WindowInsetsCompat.Type.displayCutout())
				toolbar.updateLayoutParams<MarginLayoutParams> {
					topMargin = systemBar.top
					rightMargin = systemBar.right + displayCutout.right
					leftMargin = systemBar.left + displayCutout.left
				}
				btnScrollTop.updateLayoutParams<MarginLayoutParams> {
					leftMargin = systemBar.left + displayCutout.left + 16.dp
					bottomMargin = systemBar.bottom + 16.dp
				}

				autoScrollLayout.root.updateLayoutParams<MarginLayoutParams> {
					rightMargin = systemBar.right + displayCutout.right + 16.dp
					bottomMargin = systemBar.bottom + 16.dp
				}

				navView.drawerContainer.updateLayoutParams<MarginLayoutParams> {
					topMargin = systemBar.top
					bottomMargin = systemBar.bottom
					leftMargin = systemBar.left + displayCutout.left
					rightMargin = systemBar.right + displayCutout.right
				}
				inset
			}
		}

		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		initObserver()
		initView()
		initViewListener()
	}

	private fun initObserver() {
		scrollSpeed.launchAndCollectLatest(viewLifecycleOwner) {
			binding.autoScrollLayout.apply {
				btnMinus.isEnabled = it > minScrollSpeed
				btnPlus.isEnabled = it < maxScrollSpeed
			}
		}

		isAutoScrollEnable.launchAndCollectLatest(viewLifecycleOwner) {
			binding.autoScrollLayout.apply {
				btnPlay.isChecked = it
				binding.autoScrollLayout.root.layoutParams.apply {
					if (this is CoordinatorLayout.LayoutParams) {
						behavior =
							if (it) {
								null
							} else {
								HideBottomViewOnScrollBehavior<View>()
							}
					}
				}
			}
		}

		viewModel.chapterList.launchAndCollectLatest(viewLifecycleOwner) { list ->
			binding.navView.rvListChapter.initRecyclerView {
				return@initRecyclerView ListChapterAdapter().apply {
					setItem(list)
					setSelectedChapter(viewModel.chapter)
					setOnChapterClickListener { item ->
						viewModel.setCurrentChapter(item)
						viewModel.getChapterDetail()
						binding.drawer.close()
					}
				}
			}
		}

		viewModel.chapterDetailResponse.launchAndCollectLatest(viewLifecycleOwner) {
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

		viewModel.saveFavoriteResponse.launchAndCollectLatest(viewLifecycleOwner) {
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
					if (data.first == "save") {
						binding.root.showSnackBar("Comic '${data.second.title}' saved to favorite successfully!")
					} else {
						binding.root.showSnackBar("Comic '${data.second.title}' deleted from favorite successfully!")
					}
				},
			)
		}
	}

	@SuppressLint("SetTextI18n")
	private fun initView() {
		binding.apply {
			autoScrollRunnable =
				object : Runnable {
					override fun run() {
						binding.rvImageChapter.smoothScrollBy(0, scrollSpeed.value)
						autoScrollHandler.postDelayed(this, 10) // Adjust delay for speed
					}
				}
			val startOffset = toolbar.layoutParams.height
			swipeRefresh.setProgressViewOffset(true, startOffset, startOffset + swipeRefresh.progressViewEndOffset)
			toolbar.title = viewModel.comic.title
			toolbar.subtitle = viewModel.chapter.title
			stateView.setToolbar(title = viewModel.comic.title, subtitle = viewModel.chapter.title)
			autoScrollLayout.tvScrollSpeed.text = (scrollSpeed.value / scrollStep).toString()
			rvImageChapter.initRecyclerView {
				return@initRecyclerView listImageChapterAdapter
			}

			if (requireContext().isSizeTablet()) {
				val padding = (resources.displayMetrics.widthPixels - resources.displayMetrics.heightPixels).div(4)
				rvImageChapter.setPadding(padding, 0, padding, 0)
			} else {
				rvImageChapter.setPadding(0, 0, 0, 0)
			}
		}
	}

	@SuppressLint("SetTextI18n", "ClickableViewAccessibility")
	private fun initViewListener() {
		binding.apply {
			binding.rvImageChapter.addOnScrollListener(
				object : RecyclerView.OnScrollListener() {
					override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
						super.onScrolled(recyclerView, dx, dy)
						swipeRefresh.isEnabled = !recyclerView.canScrollVertically(-1)
						if (!recyclerView.canScrollVertically(1)) {
							stopAutoScroll()
						}
					}
				},
			)

			autoScrollLayout.apply {
				cvExpand.layoutTransition.apply {
					enableTransitionType(LayoutTransition.CHANGING)
					setDuration(500)
				}

				btnExpand.setOnClickListener {
					cvExpand.updateLayoutParams {
						width = if (width == LayoutParams.WRAP_CONTENT) height else LayoutParams.WRAP_CONTENT
					}
					val isExpanded = cvExpand.layoutParams.width == LayoutParams.WRAP_CONTENT
					btnExpand.scaleX = if (isExpanded) -1f else 1f
				}

				btnMinus.setOnClickListener {
					if (scrollSpeed.value > minScrollSpeed) {
						scrollSpeed.value -= scrollStep
						tvScrollSpeed.text = (scrollSpeed.value / scrollStep).toString()
					}
				}

				btnPlus.setOnClickListener {
					if (scrollSpeed.value < maxScrollSpeed) {
						scrollSpeed.value += scrollStep
						tvScrollSpeed.text = (scrollSpeed.value / scrollStep).toString()
					}
				}

				btnPlay.setOnClickListener {
					if (btnPlay.isChecked) {
						startAutoScroll()
					} else {
						stopAutoScroll()
					}
				}
			}

			btnScrollTop.setOnClickListener {
				rvImageChapter.layoutManager?.scrollToPosition(0)
			}

			navView.btnClose.setOnClickListener {
				drawer.close()
			}

			val onClickDetector =
				GestureDetector(
					requireContext(),
					object : GestureDetector.SimpleOnGestureListener() {
						override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
							bottomSheet.show()
							return false
						}
					},
				)
			val onSwipeDetector =
				SwipeRightDetector(
					requireContext(),
					object : SwipeRightDetector.OnSwipeRightListener {
						override fun onSwipeRight() {
							drawer.open()
						}
					},
				)

			rvImageChapter.addOnItemTouchListener(
				object : RecyclerView.OnItemTouchListener {
					override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
						val holder = rv.findChildViewUnder(e.x, e.y)?.let { rv.findContainingViewHolder(it) }
						if (holder is ListImageChapterAdapter.ViewHolder) {
							if (!isTouchInView(holder.binding.btnRetryImage, e.rawX, e.rawY)) {
								onClickDetector.onTouchEvent(e)
							}
						}
						onSwipeDetector.onTouchEvent(e)
						return false
					}

					override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
					}

					override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
					}
				},
			)

			swipeRefresh.setOnRefreshListener {
				viewModel.getChapterDetail()
			}

			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			stateView.addOnActionClickListener {
				viewModel.getChapterDetail()
			}

			stateView.addOnBackClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}
		}
	}

	fun isTouchInView(view: View, rawX: Float, rawY: Float): Boolean {
		val location = IntArray(2)
		view.getLocationOnScreen(location)
		val viewX = location[0]
		val viewY = location[1]
		return rawX >= viewX && rawX <= viewX + view.width && rawY >= viewY && rawY <= viewY + view.height
	}

	private fun onError(error: String?) {
		binding.apply {
			stateView.setState(StateView.ERROR)
			stateView.setError(description = error)
			swipeRefresh.isRefreshing = false
		}
	}

	private fun onSuccess(data: List<String>) {
		binding.apply {
			stateView.setState(StateView.CONTENT)
			swipeRefresh.isRefreshing = false
			toolbar.title = viewModel.comic.title
			toolbar.subtitle = viewModel.chapter.title
			listImageChapterAdapter.setItem(data)
			binding.navView.rvListChapter.initRecyclerView {
				return@initRecyclerView ListChapterAdapter().apply {
					setItem(viewModel.chapterList.value)
					setSelectedChapter(viewModel.chapter)
					setOnChapterClickListener { item ->
						viewModel.setCurrentChapter(item)
						viewModel.getChapterDetail()
						binding.drawer.close()
					}
				}
			}
		}
	}

	private fun onLoading() {
		binding.apply {
			stateView.setState(StateView.LOADING)
			rvImageChapter.layoutManager?.scrollToPosition(0)
		}
	}

	private fun LayoutManager.scrollToPositionCenter(position: Int) {
		if (this is LinearLayoutManager) {
			val offset = height.div(2)
			if (position > 0) {
				val scrollPosition = if (position < itemCount - 1) position + 1 else position
				scrollToPositionWithOffset(scrollPosition, offset)
			}
		}
	}

	override fun onDrawerClickListener() {
		if (this::binding.isInitialized) {
			binding.drawer.open()
			val item = viewModel.chapterList.value.find { it.slug == viewModel.chapter.slug }
			val position = viewModel.chapterList.value.indexOf(item)
			binding.navView.rvListChapter.layoutManager
				?.scrollToPositionCenter(position)
		}
	}

	override fun onBeforeClickListener(chapter: Chapter) {
		viewModel.setCurrentChapter(chapter)
		viewModel.getChapterDetail()
	}

	override fun onNextClickListener(chapter: Chapter) {
		viewModel.setCurrentChapter(chapter)
		viewModel.getChapterDetail()
	}

	override fun onCommentClickListener(chapter: Chapter) {
		showLoading()
		lifecycleScope.launch {
			val url = chapter.getCommentUrl()
			requireContext().openTab(url)
			hideLoading()
		}
	}

	override fun onFavoriteClickListener(comic: Comic) {
		viewModel.setFavorite(viewModel.comic)
	}

	override fun onPause() {
		super.onPause()
		stopAutoScroll()
	}

	override fun onStop() {
		super.onStop()
		stopAutoScroll()
	}

	private fun startAutoScroll() {
		isAutoScrollEnable.value = true
		autoScrollHandler.post(autoScrollRunnable)
	}

	private fun stopAutoScroll() {
		isAutoScrollEnable.value = false
		if (this::autoScrollRunnable.isInitialized) {
			autoScrollHandler.removeCallbacks(autoScrollRunnable)
		}
	}

	override fun onDetach() {
		super.onDetach()
		// requireActivity().window.showSystemBar()
	}
}