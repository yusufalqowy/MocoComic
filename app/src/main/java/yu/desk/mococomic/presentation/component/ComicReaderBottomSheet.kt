package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import yu.desk.mococomic.databinding.LayoutComicReaderBottomSheetBinding
import yu.desk.mococomic.presentation.comic.ComicReaderListener
import yu.desk.mococomic.presentation.comic.ComicViewModel
import yu.desk.mococomic.utils.loadImage

class ComicReaderBottomSheet(
	context: Context,
	private val viewModel: ComicViewModel,
	private val listener: ComicReaderListener,
) : BottomSheetDialog(context) {
	private lateinit var binding: LayoutComicReaderBottomSheetBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = LayoutComicReaderBottomSheetBinding.inflate(layoutInflater)
		initViews()
		initListener()
		behavior.skipCollapsed = true
		/*window?.let {
			ViewCompat.setOnApplyWindowInsetsListener(it.decorView) { _, inset ->
				WindowInsetsCompat.CONSUMED
			}
			it.hideNavigationBars()
		}*/
		setContentView(binding.root)
	}

	override fun show() {
		super.show()
		if (this::binding.isInitialized) {
			// window?.hideNavigationBars()
			initViews()
		}
	}

	private fun initListener() {
		binding.apply {
			chapterSlider.addOnChangeListener { slider, _, _ ->
				val item =
					viewModel.chapterList.value
						.reversed()
						.find { it.slug == viewModel.chapter.slug }
				val currentIndex =
					viewModel.chapterList.value
						.reversed()
						.indexOf(item)
				val value =
					if (viewModel.chapterList.value.size == 1) {
						1
					} else {
						if (currentIndex >= 0) currentIndex else 0
					}
				slider.value = value.toFloat()
			}
			chapterSlider.setLabelFormatter { sliderValue ->
				val percentage =
					sliderValue
						.div(
							viewModel.chapterList.value.size
								.let { if (it > 1) it - 1 else 1 },
						).times(100)
						.toInt()
				"$percentage%"
			}

			btnChapter.setOnClickListener {
				listener.onDrawerClickListener()
				dismiss()
			}

			btnBefore.setOnClickListener {
				var chapter =
					viewModel.chapterList.value
						.reversed()
						.find { it.slug == viewModel.chapter.slug }
				val index =
					viewModel.chapterList.value
						.reversed()
						.indexOf(chapter)
				if (index >= 0) {
					chapter =
						viewModel.chapterList.value
							.reversed()
							.getOrNull(index - 1)
					listener.onBeforeClickListener(chapter ?: return@setOnClickListener)
					dismiss()
				}
			}

			btnNext.setOnClickListener {
				var chapter =
					viewModel.chapterList.value
						.reversed()
						.find { it.slug == viewModel.chapter.slug }
				val index =
					viewModel.chapterList.value
						.reversed()
						.indexOf(chapter)
				if (index >= 0) {
					chapter =
						viewModel.chapterList.value
							.reversed()
							.getOrNull(index + 1)
					listener.onBeforeClickListener(chapter ?: return@setOnClickListener)
					dismiss()
				}
			}

			btnFavorite.setOnClickListener {
				btnFavorite.isChecked = viewModel.comic.isFavorite
				listener.onFavoriteClickListener(viewModel.comic)
				dismiss()
			}

			btnComment.setOnClickListener {
				listener.onCommentClickListener(viewModel.chapter)
				dismiss()
			}
		}
	}

	private fun initViews() {
		binding.apply {
			val item =
				viewModel.chapterList.value
					.reversed()
					.find { it.slug == viewModel.chapter.slug }
			val currentIndex =
				viewModel.chapterList.value
					.reversed()
					.indexOf(item)
			val value =
				if (viewModel.chapterList.value.size == 1) {
					1
				} else {
					if (currentIndex >= 0) currentIndex else 0
				}
			chapterSlider.valueFrom = 0f
			chapterSlider.valueTo =
				(
					viewModel.chapterList.value.size
						.let { if (it > 1) it - 1 else 1 }
				).toFloat()
			chapterSlider.value = value.toFloat()

			btnBefore.isEnabled = currentIndex > 0
			btnNext.isEnabled = currentIndex < viewModel.chapterList.value.size - 1

			viewModel.comic.let {
				ivCover.loadImage(it.cover, false)
				tvTitle.text = it.title
				btnFavorite.isChecked = it.isFavorite
			}
			tvChapter.text = viewModel.chapter.title
		}
	}
}