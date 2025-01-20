package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import yu.desk.mococomic.databinding.LayoutComicReaderBottomSheetBinding
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.presentation.comic.ComicReaderListener
import yu.desk.mococomic.presentation.comic.ComicViewModel
import yu.desk.mococomic.utils.loadImage

class ComicReaderBottomSheet(context: Context, private val viewModel: ComicViewModel, private val listener: ComicReaderListener) : BottomSheetDialog(context) {
    lateinit var binding: LayoutComicReaderBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutComicReaderBottomSheetBinding.inflate(layoutInflater)
        initViews()
        setContentView(binding.root)
    }

    override fun show() {
        super.show()
        if(this::binding.isInitialized){
            binding.chapterSlider.value = (viewModel.getChapterList().reversed().indexOf(viewModel.chapter) + 1).toFloat()
        }
    }

    private fun initViews() {
        binding.apply {
            chapterSlider.addOnChangeListener { slider, value, fromUser ->
                slider.value = (viewModel.getChapterList().reversed().indexOf(viewModel.chapter) + 1).toFloat()
            }
            chapterSlider.valueFrom = 1f
            chapterSlider.valueTo = (viewModel.getChapterList().size).toFloat()
            chapterSlider.value = (viewModel.getChapterList().reversed().indexOf(viewModel.chapter) + 1).toFloat()

            viewModel.comic?.let {
                ivCover.loadImage(it.cover)
                tvTitle.text = it.title
            }

            viewModel.chapter?.let {
                tvChapter.text = it.chapter
            }

            btnChapter.setOnClickListener {
                listener.onDrawerClickListener()
                dismiss()
            }

            btnBefore.setOnClickListener {
                listener.onBeforeClickListener(Chapter())
                dismiss()
            }

            btnNext.setOnClickListener {
                listener.onNextClickListener(Chapter())
                dismiss()
            }

            btnFavorite.setOnClickListener {
                listener.onFavoriteClickListener(Chapter())
            }

        }
    }
}
