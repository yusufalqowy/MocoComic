package yu.desk.mococomic.presentation.dashboard.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import com.google.android.material.carousel.CarouselSnapHelper
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentHomeBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.adapter.CarouselComicAdapter
import yu.desk.mococomic.presentation.adapter.ComicAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.dashboard.DashboardViewModel
import yu.desk.mococomic.utils.UIState
import yu.desk.mococomic.utils.apiResponseHandler
import yu.desk.mococomic.utils.findNavController
import yu.desk.mococomic.utils.getGreetingMessage
import yu.desk.mococomic.utils.initRecyclerView
import yu.desk.mococomic.utils.isSizeMobile
import yu.desk.mococomic.utils.navigateWithAnimation
import yu.desk.mococomic.utils.setVisible

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private var doubleBackToExitPressedOnce = false
    private val viewModel by viewModels<DashboardViewModel>(ownerProducer = { requireParentFragment() })
    private val rvCarouselAdapter by lazy { CarouselComicAdapter() }
    private val rvPopularAdapter by lazy { ComicAdapter() }
    private val rvNewAdapter by lazy { ComicAdapter() }
    private val rvUpdateAdapter by lazy { ComicAdapter() }

    companion object{
        const val TAG = "HomeFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (viewModel.homeResponse.value !is UIState.Success) {
            viewModel.getHome()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initListener()
        initObserver()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            viewModel.homeResponse.flowWithLifecycle(lifecycle).collectLatest {
                apiResponseHandler(
                    uiState = it,
                    onLoading = {
                        onLoading()
                    },
                    onSuccess = { data ->
                        onSuccess(data)
                    },
                    onError = { message ->
                        onError(message)
                    },
                )
            }
        }
    }

    private fun onError(message: String?) {
        binding.apply {
            errorView.setVisible(true)
            content.setVisible(false)
            swipeRefresh.isRefreshing = false
        }
    }

    private fun onSuccess(data: List<Comic>) {
        binding.apply {
            rvCarouselAdapter.setItems(data.take(5))
            rvPopularAdapter.setItems(data.take(8))
            rvNewAdapter.setItems(data.take(8))
            rvUpdateAdapter.setItems(data.take(12))
            swipeRefresh.isRefreshing = false
        }
    }

    private fun onLoading() {
        binding.apply {
            rvCarouselAdapter.setItems((1..5).toList())
            rvPopularAdapter.setItems((1..5).toList())
            rvNewAdapter.setItems((1..5).toList())
            rvUpdateAdapter.setItems((1..5).toList())
            errorView.setVisible(false)
            content.setVisible(true)
        }
    }

    private fun initListener() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (doubleBackToExitPressedOnce) {
                activity?.finishAffinity()
            } else {
                doubleBackToExitPressedOnce = true
                Toast.makeText(context, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
                Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
            return@addCallback
        }
        binding.apply {
            binding.swipeRefresh.setOnRefreshListener {
                viewModel.getHome()
            }
            rvCarouselAdapter.setOnItemClickListener {
                navigateToComicDetail(it)
            }
            rvNewAdapter.setOnItemClickListener {
                navigateToComicDetail(it)
            }
            rvPopularAdapter.setOnItemClickListener {
                navigateToComicDetail(it)
            }
            rvUpdateAdapter.setOnItemClickListener {
                navigateToComicDetail(it)
            }
        }
    }

    private fun initView() {
        binding.apply {
            tvGreeting.text = getGreetingMessage()
            CarouselSnapHelper().attachToRecyclerView(rvCarousel)
            val itemCount = if (requireContext().isSizeMobile()) 2.5f else 5f

            rvCarousel.initRecyclerView {
                return@initRecyclerView rvCarouselAdapter.apply {
                    setItems((1..5).toList())
                }
            }
            rvPopular.initRecyclerView {
                rvPopularAdapter.setVisibleItem(itemCount)
                return@initRecyclerView rvPopularAdapter.apply {
                    setItems((1..5).toList())
                }
            }
            rvNew.initRecyclerView {
                rvNewAdapter.setVisibleItem(itemCount)
                return@initRecyclerView rvNewAdapter.apply {
                    setItems((1..5).toList())
                }
            }
            rvUpdate.rvComic.initRecyclerView(hasFixedSize = false) {
                return@initRecyclerView rvUpdateAdapter.apply {
                    setItems((1..6).toList())
                }
            }
        }
    }

    private fun navigateToComicDetail(comic: Comic) {
        findNavController(R.id.navHostMain)
            .navigateWithAnimation(R.id.comicNavigation, ComicDetailFragmentArgs.Builder(comic).build().toBundle(), navOptions {
                popUpTo(R.id.dashboardMain)
                launchSingleTop = true
            })
    }
}
