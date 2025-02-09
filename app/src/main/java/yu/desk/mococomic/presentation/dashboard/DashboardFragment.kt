package yu.desk.mococomic.presentation.dashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentDashboardBinding

@AndroidEntryPoint
class DashboardFragment : Fragment() {
	private lateinit var binding: FragmentDashboardBinding
	private val viewModel by viewModels<DashboardViewModel>()
	private val dashboardHome by lazy { HomeFragment() }
	private val dashboardExplore by lazy { ExploreFragment() }
	private val dashboardFavorite by lazy { FavoriteFragment() }
	private val dashboardProfile by lazy { ProfileFragment() }
	private var activeFragmentTag: String = HomeFragment.TAG
	private var doubleBackToExitPressedOnce = false

	companion object {
		private const val ACTIVE_FRAGMENT_KEY = "ACTIVE_FRAGMENT"
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			viewModel.syncBlockedComicFirebase()
			viewModel.syncFavoriteComicFirebase()
			viewModel.syncChapterHistoryFirebase()
		} else {
			activeFragmentTag = savedInstanceState.getString(ACTIVE_FRAGMENT_KEY, HomeFragment.TAG)
		}
		childFragmentManager
			.beginTransaction()
			.apply {
				addFragment(dashboardHome, HomeFragment.TAG)
				addFragment(dashboardExplore, ExploreFragment.TAG)
				addFragment(dashboardFavorite, FavoriteFragment.TAG)
				addFragment(dashboardProfile, ProfileFragment.TAG)
			}.commitNow()
		savedInstanceState?.let {
			activeFragmentTag = it.getString(ACTIVE_FRAGMENT_KEY, HomeFragment.TAG)
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString(ACTIVE_FRAGMENT_KEY, activeFragmentTag)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentDashboardBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		initListener()
	}

	private fun initView() {
//        binding.navView?.setupWithNavController(navController)
//        binding.bottomNavView?.setupWithNavController(navController)
		childFragmentManager.beginTransaction().show(findFragmentByTag(activeFragmentTag)).commitNow()
		binding.bottomNavView?.selectedItemId = getSelectedItemId()
		binding.navView?.selectedItemId = getSelectedItemId()
	}

	private fun initListener() {
		binding.apply {
			bottomNavView?.setOnItemSelectedListener {
				if (it.itemId == bottomNavView.selectedItemId) return@setOnItemSelectedListener false
				return@setOnItemSelectedListener onSelectedItemListener(it.itemId)
			}
			navView?.setOnItemSelectedListener {
				if (it.itemId == navView.selectedItemId) return@setOnItemSelectedListener false
				return@setOnItemSelectedListener onSelectedItemListener(it.itemId)
			}
		}
		requireActivity().onBackPressedDispatcher.addCallback(
			viewLifecycleOwner,
			object : OnBackPressedCallback(true) {
				override fun handleOnBackPressed() {
					if (activeFragmentTag != HomeFragment.TAG) {
						childFragmentManager.beginTransaction().navigateTo(dashboardHome)
						activeFragmentTag = HomeFragment.TAG
						binding.bottomNavView?.selectedItemId = R.id.dashboardHome
						binding.navView?.selectedItemId = R.id.dashboardHome
					} else {
						if (doubleBackToExitPressedOnce) {
							requireActivity().finishAffinity()
						} else {
							doubleBackToExitPressedOnce = true
							Toast.makeText(context, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
							Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
						}
					}
				}
			},
		)
	}

	private fun onSelectedItemListener(itemId: Int): Boolean =
		when (itemId) {
			R.id.dashboardHome -> {
				childFragmentManager.beginTransaction().navigateTo(dashboardHome)
				activeFragmentTag = HomeFragment.TAG
				true
			}

			R.id.dashboardExplore -> {
				childFragmentManager.beginTransaction().navigateTo(dashboardExplore)
				activeFragmentTag = ExploreFragment.TAG
				true
			}

			R.id.dashboardFavorite -> {
				childFragmentManager.beginTransaction().navigateTo(dashboardFavorite)
				activeFragmentTag = FavoriteFragment.TAG
				true
			}

			R.id.dashboardProfile -> {
				childFragmentManager.beginTransaction().navigateTo(dashboardProfile)
				activeFragmentTag = ProfileFragment.TAG
				true
			}

			else -> false
		}

	private fun FragmentTransaction.navigateTo(fragment: Fragment) =
		this
			.setCustomAnimations(
				R.anim.slide_in_right,
				R.anim.slide_out_left,
				R.anim.slide_in_left,
				R.anim.slide_out_right,
			).hide(findFragmentByTag(activeFragmentTag))
			.show(fragment)
			.commit()

	private fun FragmentTransaction.addFragment(
		fragment: Fragment,
		tag: String,
	): FragmentTransaction {
		val frag = childFragmentManager.findFragmentByTag(tag)
		if (frag != null) this.remove(frag)
		this.add(R.id.navHostDashboard, fragment, tag).hide(fragment)
		return this
	}

	private fun findFragmentByTag(tag: String) = childFragmentManager.findFragmentByTag(tag) ?: dashboardHome

	private fun getSelectedItemId() =
		when (activeFragmentTag) {
			HomeFragment.TAG -> R.id.dashboardHome
			ExploreFragment.TAG -> R.id.dashboardExplore
			FavoriteFragment.TAG -> R.id.dashboardFavorite
			ProfileFragment.TAG -> R.id.dashboardProfile
			else -> R.id.dashboardHome
		}
}