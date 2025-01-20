package yu.desk.mococomic.presentation.comic

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.google.android.material.sidesheet.SideSheetBehavior
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentComicReaderBinding
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.presentation.adapter.ListChapterAdapter
import yu.desk.mococomic.presentation.adapter.ListImageChapterAdapter
import yu.desk.mococomic.presentation.component.ComicReaderBottomSheet
import yu.desk.mococomic.presentation.component.ComicReaderSideSheet
import yu.desk.mococomic.utils.apiResponseHandler
import yu.desk.mococomic.utils.close
import yu.desk.mococomic.utils.navigateWithAnimation
import yu.desk.mococomic.utils.setVisible
import yu.desk.mococomic.utils.toggle
import kotlin.math.abs

class ComicReaderFragment : Fragment(), ComicReaderListener {
    lateinit var binding: FragmentComicReaderBinding
    private val viewModel: ComicViewModel by navGraphViewModels(R.id.comicNavigation)
    private val navArgs: ComicReaderFragmentArgs by navArgs()
    private val bottomSheet by lazy { ComicReaderBottomSheet(requireContext(), viewModel, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setCurrentChapter(navArgs.currentChapter)
        viewModel.getChapterDetail()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentComicReaderBinding.inflate(inflater, container, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            binding.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }

            binding.swipeRefresh.setProgressViewOffset(false, 0, systemBars.top)

            binding.navView.drawerContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
                bottomMargin = systemBars.bottom
            }

            insets
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewListener()

    }

    private fun initView() {
        lifecycleScope.launch {
            viewModel.chapterDetailResponse.flowWithLifecycle(lifecycle).collectLatest {
                apiResponseHandler(
                    uiState = it,
                    onLoading = {
                        onLoading()
                    },
                    onSuccess = { data ->
                        onSuccess(data)
                    },
                    onError = {
                        onError()
                    }
                )
            }
        }
    }

    private fun initViewListener() {
        binding.apply {
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
            }

            errorView.addOnBackClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            errorView.addOnRetryClickListener {
                viewModel.getChapterDetail()
            }


            swipeRefresh.setOnRefreshListener {
                viewModel.getChapterDetail()
            }

            toolbar.setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }

            rvImageChapter.setOnClickListener {
                bottomSheet.show()
            }

            rvImageChapter.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                private var startX = 0f
                private var startY = 0f
                private val clickThreshold = 10
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    when (e.action) {
                        MotionEvent.ACTION_UP -> {
                            val endX = e.x
                            val endY = e.y
                            val deltaX = abs(endX - startX)
                            val deltaY = abs(endY - startY)

                            if (deltaX <= clickThreshold && deltaY <= clickThreshold) {
                                // Click detected
                                rv.performClick()
                            }
                        }

                        MotionEvent.ACTION_DOWN -> {
                            startX = e.x
                            startY = e.y
                        }
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                }

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
                }

            })

        }

    }

    private fun onError() {
        binding.apply {
            loadingView.setVisible(false)
            container.setVisible(false)
            errorView.setVisible(true)
        }
    }

    private fun onSuccess(data: Any) {
        binding.apply {
            errorView.setVisible(false)
            loadingView.setVisible(false)
            container.setVisible(true)
            swipeRefresh.isRefreshing = false
            binding.rvImageChapter.apply {
                setHasFixedSize(true)
                setItemViewCacheSize(5)
                val item = listOf(
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/00.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/01.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/02.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/03.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/04.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/05.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/06.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/07.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/08.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/09.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/10.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/11.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/12.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/13.jpg",
                    "https://cdn.uqni.net/images/7/solo-resurrection/chapter-35/14.jpg"
                )
                adapter = ListImageChapterAdapter().apply {
                    setHasFixedSize(true)
                    setItem(item)
                }
            }
            navView.rvListChapter.apply {
                setHasFixedSize(true)
                adapter = ListChapterAdapter().apply {
                    setItem(viewModel.getChapterList())
                    viewModel.chapter?.let {
                        setSelectedChapter(it)
                    }
                    setOnChapterClickListener { item ->
                        val direction = ComicReaderFragmentDirections.actionComicReaderSelf(item)
                        findNavController().navigateWithAnimation(
                            direction,
                            navOptions = navOptions {
                                popUpTo(R.id.comicDetail) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            },
                        )
                    }
                }
            }
        }
    }

    private fun onLoading() {
        binding.apply {
            errorView.setVisible(false)
            container.setVisible(swipeRefresh.isRefreshing)
            loadingView.setVisible(!swipeRefresh.isRefreshing)
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
        if(this::binding.isInitialized){
            binding.drawer.open()
            val position = viewModel.getChapterList().indexOf(viewModel.chapter)
            binding.navView.rvListChapter.layoutManager?.scrollToPositionCenter(position)
        }
    }

    override fun onBeforeClickListener(chapter: Chapter) {

    }

    override fun onNextClickListener(chapter: Chapter) {

    }

    override fun onFavoriteClickListener(chapter: Chapter) {

    }
}
