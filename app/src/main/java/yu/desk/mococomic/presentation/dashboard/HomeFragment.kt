package yu.desk.mococomic.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navOptions
import com.google.android.material.carousel.CarouselSnapHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentHomeBinding
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.Summary
import yu.desk.mococomic.presentation.adapter.CarouselComicAdapter
import yu.desk.mococomic.presentation.adapter.ComicAdapter
import yu.desk.mococomic.presentation.comic.ComicDetailFragmentArgs
import yu.desk.mococomic.presentation.component.StateView
import yu.desk.mococomic.utils.*

@AndroidEntryPoint
class HomeFragment : Fragment() {
	private lateinit var binding: FragmentHomeBinding
	private val viewModel by viewModels<DashboardViewModel>(ownerProducer = { requireParentFragment() })
	private val rvCarouselAdapter by lazy { CarouselComicAdapter() }
	private val rvPopularAdapter by lazy { ComicAdapter() }
	private val rvNewAdapter by lazy { ComicAdapter() }
	private val rvUpdateAdapter by lazy { ComicAdapter() }

	companion object {
		const val TAG = "HomeFragment"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (viewModel.homeResponse.value !is UIState.Success) {
			viewModel.getHome()
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentHomeBinding.inflate(inflater, container, false)
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

		findNavController(R.id.navHostMain).currentBackStackEntry?.savedStateHandle?.let { stateHandle ->
			stateHandle.getLiveData<Comic>(MyConstants.BLOCKED_COMIC).observe(viewLifecycleOwner) {
				rvPopularAdapter.removeItem(it)
				rvNewAdapter.removeItem(it)
				rvUpdateAdapter.removeItem(it)
			}
		}
	}

	private fun onError(message: String?) {
		binding.apply {
			stateView.setState(StateView.ERROR)
			stateView.setError(description = message, actionButtonText = "")
			swipeRefresh.isRefreshing = false
		}
	}

	private fun onSuccess(data: Summary) {
		binding.apply {
			stateView.setState(StateView.CONTENT)
			rvCarouselAdapter.setItems(data.trending)
			rvPopular.getStateView()?.setIsEmpty(data.popular.isEmpty())
			rvPopularAdapter.setItems(data.popular)
			rvNew.getStateView()?.setIsEmpty(data.newManga.isEmpty())
			rvNewAdapter.setItems(data.newManga)
			rvUpdate.rvComic.getStateView()?.setIsEmpty(data.newChapter.isEmpty())
			rvUpdateAdapter.setItems(data.newChapter)
			swipeRefresh.isRefreshing = false
		}
	}

	private fun onLoading() {
		binding.apply {
			stateView.setState(StateView.CONTENT)
			rvCarouselAdapter.setItems((1..5).toList())
			rvPopularAdapter.setItems((1..5).toList())
			rvNewAdapter.setItems((1..5).toList())
			rvUpdateAdapter.setItems((1..5).toList())
		}
	}

	private fun initListener() {
		binding.apply {
			swipeRefresh.setOnRefreshListener {
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
			btnSearch.setOnClickListener {
				findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_searchComicFragment)
			}
		}
	}

	private fun initView() {
		binding.apply {
			tvGreeting.text = getGreetingMessage()

			if (viewModel.currentUser != null) {
				viewModel.currentUser?.let {
					ivUserAvatar.loadImage(it.photoUrl.toString())
					tvName.text = it.displayName
				}
			} else {
				ivUserAvatar.setImageResource(R.drawable.ic_profile_fill)
				tvName.text = "Guest"
			}

			rvCarousel.initRecyclerView {
				return@initRecyclerView rvCarouselAdapter.apply {
					setItems((1..5).toList())
				}
			}
			CarouselSnapHelper().attachToRecyclerView(rvCarousel)

			val itemCount = if (requireContext().isSizeMobile()) 2.5f else 5f
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
			.navigateWithAnimation(
				R.id.comicNavigation,
				ComicDetailFragmentArgs.Builder(comic).build().toBundle(),
				navOptions {
					popUpTo(R.id.dashboardMain)
					launchSingleTop = true
				},
			)
	}
}