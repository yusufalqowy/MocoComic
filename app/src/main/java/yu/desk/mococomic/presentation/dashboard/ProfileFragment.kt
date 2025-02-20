package yu.desk.mococomic.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentProfileBinding
import yu.desk.mococomic.presentation.component.InfoDialogData
import yu.desk.mococomic.utils.*

@AndroidEntryPoint
class ProfileFragment : Fragment() {
	private lateinit var binding: FragmentProfileBinding
	private val viewModel by viewModels<DashboardViewModel>(ownerProducer = { requireParentFragment() })

	companion object {
		const val TAG = "ProfileFragment"
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentProfileBinding.inflate(inflater, container, false)
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
		viewModel.clearDbResponse.launchAndCollectLatest(viewLifecycleOwner) {
			apiResponseHandler(
				uiState = it,
				onLoading = {
					showLoading()
				},
				onSuccess = {
					hideLoading()
					lifecycleScope.launch {
						AuthHelper.signOut(requireContext()) {
							navigateToLogin()
						}
					}
				},
				onError = {
					hideLoading()
					lifecycleScope.launch {
						AuthHelper.signOut(requireContext()) {
							navigateToLogin()
						}
					}
				},
			)
		}
	}

	private fun initListener() {
		binding.apply {
			itemLogout.setOnClickListener {
				showInfoBottomSheet(
					infoData =
						InfoDialogData(
							title = getString(R.string.text_sure_want_to_logout),
							negativeButton = getString(R.string.text_no) to {},
							positiveButton =
								getString(R.string.text_yes) to {
									viewModel.logout()
								},
						),
				)
			}

			itemLogin.setOnClickListener {
				lifecycleScope.launch {
					AuthHelper.signOut(requireContext()) {
						navigateToLogin()
					}
				}
			}

			itemChapterHistory.setOnClickListener {
				navigateToChapterHistory()
			}

			itemSetting.setOnClickListener {
				findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_settingsFragment)
			}

			itemPrivacyPolicy.setOnClickListener {
				findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_privacyPolicyFragment)
			}

			itemTermCondition.setOnClickListener {
				findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_termsAndConditionsFragment)
			}

			itemContactUs.setOnClickListener {
				findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_contactFragment)
			}
		}
	}

	private fun initView() {
		binding.apply {
			itemChapterHistory.setVisible(viewModel.currentUser != null)
			itemLogout.setVisible(viewModel.currentUser != null)
			itemLogin.setVisible(viewModel.currentUser == null)

			if (viewModel.currentUser != null) {
				viewModel.currentUser?.let {
					ivProfile.loadImage(it.photoUrl.toString())
					tvName.text = it.displayName
					tvEmail.text = it.email
				}
			} else {
				ivProfile.setImageResource(R.drawable.ic_profile_fill)
				tvName.text = "Guest"
				tvEmail.text = "-"
			}
		}
	}

	private fun navigateToLogin() {
		findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_authLogin)
	}

	private fun navigateToChapterHistory() {
		findNavController(R.id.navHostMain).navigateWithAnimation(R.id.action_dashboardMain_to_chapterHistoryFragment)
	}
}