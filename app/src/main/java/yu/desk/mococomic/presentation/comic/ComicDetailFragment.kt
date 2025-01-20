package yu.desk.mococomic.presentation.comic

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import coil3.asDrawable
import coil3.load
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentComicDetailBinding
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.ComicDetail
import yu.desk.mococomic.presentation.adapter.ListChapterAdapter
import yu.desk.mococomic.utils.apiResponseHandler
import yu.desk.mococomic.utils.isTextOverflowing
import yu.desk.mococomic.utils.loadImage
import yu.desk.mococomic.utils.loadingShimmer
import yu.desk.mococomic.utils.navigateWithAnimation
import yu.desk.mococomic.utils.scaleCropTop
import yu.desk.mococomic.utils.setVisible
import kotlin.math.abs


class ComicDetailFragment : Fragment() {

    lateinit var binding: FragmentComicDetailBinding
    private val viewModel: ComicViewModel by navGraphViewModels(R.id.comicNavigation)
    private val navArgs by navArgs<ComicDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentComicDetailBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarInsets.top
            }
            binding.swipeRefresh.setProgressViewOffset(false, 0, systemBarInsets.top)
            binding.navView.drawerContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBarInsets.top
                bottomMargin = systemBarInsets.bottom
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.comicDetailResponse.flowWithLifecycle(lifecycle)
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
                        onError = {
                            onError()
                        }
                    )
                }
        }
    }

    private fun initView() {
        binding.apply {
            errorView.setVisible(false)
            swipeRefresh.setVisible(true)
            viewModel.comic?.let {
                ivCoverBackground.load(it.cover) {
                    listener(
                        onSuccess = { _, result ->
                            ivCoverBackground.setImageDrawable(result.image.asDrawable(resources))
                            ivCoverBackground.scaleCropTop()
                        }
                    )
                }
                ivCover.loadImage(it.cover)
                tvTitle.text = it.title
            }
        }

    }

    private fun initListener() {
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            //Handle swipe refresh layout not easy to scroll
            binding.swipeRefresh.isEnabled = scrollY == 0
        }

        binding.bottomMenu.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            //Handle visible bottom menu when appbar collapse
            binding.bottomMenu.setVisible((abs(verticalOffset) - appBarLayout.totalScrollRange) == 0)
            //Handle swipe refresh layout not easy to scroll
            binding.swipeRefresh.isEnabled = verticalOffset == 0
        }


        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getComicDetail()
        }

        binding.errorView.addOnRetryClickListener {
            viewModel.getComicDetail()
            initView()
        }

        binding.errorView.addOnBackClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.cvSynopsis.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        binding.tvSynopsis.isTextOverflowing {
            binding.btnShowMore.setVisible(it)
        }

        binding.btnShowMore.setOnClickListener {
            if (binding.btnShowMore.text == getString(yu.desk.mococomic.R.string.show_more)) {
                binding.tvSynopsis.maxLines = Integer.MAX_VALUE
                binding.btnShowMore.text = getString(yu.desk.mococomic.R.string.show_less)
            } else {
                binding.tvSynopsis.maxLines = 10
                binding.btnShowMore.text = getString(yu.desk.mococomic.R.string.show_more)
            }
        }

        binding.btnChapter.setOnClickListener {
            binding.drawer.open()
        }

        binding.btnRead.setOnClickListener {
            navigateToChapterDetail(viewModel.getChapterList().last())
        }

        binding.btnFavorite.setOnClickListener {
            binding.btnFavorite.isChecked = true
        }
    }

    private fun onError() {
        binding.errorView.setVisible(true)
        binding.swipeRefresh.setVisible(false)
        binding.swipeRefresh.isRefreshing = false

    }

    private fun onLoading(isLoading: Boolean = true) {
        binding.btnFavorite.isEnabled = !isLoading
        binding.btnChapter.isEnabled = !isLoading
        binding.btnRead.isEnabled = !isLoading
        binding.btnBottomRead.isEnabled = !isLoading
        binding.btnBottomChapter.isEnabled = !isLoading
        binding.btnBottomFavorite.isEnabled = !isLoading
        binding.btnBottomComment.isEnabled = !isLoading
        binding.btnShowMore.setVisible(!isLoading)
        binding.tvArtist.loadingShimmer(isLoading)
        binding.ratingBar.loadingShimmer(isLoading)
        binding.tvRating.loadingShimmer(isLoading)
        binding.tvSynopsis.loadingShimmer(isLoading)
        binding.tvAlternative.loadingShimmer(isLoading)
        binding.tvGenre.loadingShimmer(isLoading)
        binding.tvAuthor.loadingShimmer(isLoading)
        binding.tvPublishDate.loadingShimmer(isLoading)
        binding.tvPublisher.loadingShimmer(isLoading)
        binding.tvStatus.loadingShimmer(isLoading)
        binding.tvType.loadingShimmer(isLoading)
        binding.rvChapters.loadingShimmer(isLoading)
    }

    @SuppressLint("SetTextI18n")
    private fun onSuccess(comicDetail: ComicDetail) {
        binding.ivCoverBackground.load(comicDetail.cover) {
            listener(
                onSuccess = { _, result ->
                    binding.ivCoverBackground.setImageDrawable(result.image.asDrawable(resources))
                    binding.ivCoverBackground.scaleCropTop()
                }
            )
        }
        binding.swipeRefresh.isRefreshing = false
        binding.ivCover.loadImage(comicDetail.cover)
        binding.tvTitle.text = comicDetail.title.ifEmpty { "-" }
        binding.errorView.toolbarTitle = comicDetail.title.ifEmpty { "-" }
        binding.collapsingToolbar.title = comicDetail.title.ifEmpty { "-" }
        binding.tvArtist.text = "By ${comicDetail.author}"
        binding.ratingBar.rating = comicDetail.score.toFloatOrNull()?.div(2) ?: 0f
        binding.tvRating.text = (comicDetail.score.toFloatOrNull()?.div(2) ?: "").toString().ifEmpty { "-" }
        binding.tvSynopsis.text = comicDetail.synopsis.ifEmpty { "-" }
        binding.tvAlternative.text = comicDetail.subtitle.ifEmpty { "-" }
        binding.tvGenre.text = comicDetail.genres.joinToString(", ").ifEmpty { "-" }
        binding.tvAuthor.text = comicDetail.author.ifEmpty { "-" }
        binding.tvPublishDate.text = comicDetail.published.ifEmpty { "-" }
        binding.tvPublisher.text = comicDetail.serialization.ifEmpty { "-" }
        binding.tvStatus.text = comicDetail.status.ifEmpty { "-" }
        binding.tvType.text = "Manhwa"

        binding.navView.apply {
            btnClose.setOnClickListener {
                binding.drawer.close()
            }
            rvListChapter.apply {
                setHasFixedSize(true)
                adapter = ListChapterAdapter().apply {
                    setItem(viewModel.getChapterList())
                    setOnChapterClickListener { item ->
                        navigateToChapterDetail(item)
                    }
                }
            }
        }

        binding.rvChapters.apply {
            setHasFixedSize(true)
            adapter = ListChapterAdapter().apply {
                setItem(viewModel.getChapterList().take(5))
            }
        }
    }

    private fun navigateToChapterDetail(chapter: Chapter) {
        val direction = ComicDetailFragmentDirections.actionComicDetailToComicReader(chapter)
        findNavController().navigateWithAnimation(direction)
    }
}
