package yu.desk.mococomic.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import yu.desk.mococomic.BuildConfig
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentLoginBinding
import yu.desk.mococomic.presentation.component.InfoDialogData
import yu.desk.mococomic.utils.*
import java.text.SimpleDateFormat
import java.util.*

class LoginFragment : Fragment() {
	private lateinit var binding: FragmentLoginBinding

	private val demoManager by lazy { DemoManager(requireContext()) }

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			if (AuthHelper.isUserLogin() || AuthHelper.isLogin(requireContext())) {
				if (BuildConfig.FLAVOR == "demo") {
					checkDemoStatus { isOver ->
						if (!isOver) {
							navigateToDashboard()
						}
					}
				} else {
					navigateToDashboard()
				}
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentLoginBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(
		view: View,
		savedInstanceState: Bundle?,
	) {
		super.onViewCreated(view, savedInstanceState)
		checkDemoStatus()
		initViewListener()
	}

	override fun onResume() {
		super.onResume()
		checkDemoStatus()
	}

	private fun initViewListener() {
		binding.btnLogin.setOnClickListener {
			showDemoDialog {
				showLoading()
				lifecycleScope.launch {
					AuthHelper.signInWithGoogle(
						context = requireContext(),
						onSuccess = {
							hideLoading()
							navigateToDashboard()
						},
						onError = {
							hideLoading()
							showErrorDialog(it.message.toString())
						},
					)
				}
			}
		}

		binding.btnLoginGuest.setOnClickListener {
			showDemoDialog {
				showLoading()
				lifecycleScope.launch {
					AuthHelper.signInAsGuest(
						context = requireContext(),
						onError = {
							hideLoading()
							showErrorDialog(it.message.toString())
						},
						onSuccess = {
							hideLoading()
							navigateToDashboard()
						},
					)
				}
			}
		}
	}

	private fun showErrorDialog(message: String) {
		showInfoBottomSheet(
			InfoDialogData(
				description = message,
				positiveButton =
					"Close" to {
					},
			),
		)
	}

	private fun checkDemoStatus(onResult: ((isDemoOver: Boolean) -> Unit)? = null) {
		demoManager.checkDemoStatus {
			when (it) {
				DemoManager.Companion.StatusDemo.OVER -> {
					showDemoOverDialog()
					onResult?.invoke(true)
				}

				else -> onResult?.invoke(false)
			}
		}
	}

	private fun showDemoDialog(onResponse: () -> Unit) {
		if (BuildConfig.FLAVOR == "demo") {
			val endDemoDate = requireContext().getEndDemo() ?: Calendar.getInstance().also { it.add(Calendar.DATE, 7) }.time
			val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
			val start = LocalDate.parse(dateFormat.format(Date()))
			val end = LocalDate.parse(dateFormat.format(endDemoDate))
			val remainingDays = start.daysUntil(end)
			if (remainingDays > 0) {
				showInfoDialog(
					InfoDialogData(
						title = getString(R.string.text_demo_app),
						description = getString(R.string.text_this_is_demo_app_this_app_will_expire_in, "$remainingDays"),
						positiveButton =
							"Oke" to {
								lifecycleScope.launch {
									demoManager.startDemo {
										when (it) {
											DemoManager.Companion.StatusDemo.OVER -> showDemoOverDialog()
											else -> onResponse.invoke()
										}
									}
								}
							},
					),
				)
			} else {
				showDemoOverDialog()
			}
		} else {
			onResponse.invoke()
		}
	}

	private fun showDemoOverDialog() {
		showInfoDialog(
			InfoDialogData(
				image = R.drawable.img_attention,
				title = getString(R.string.text_trial_period_expired),
				description = getString(R.string.text_your_trial_period_has_ended),
				positiveButton =
					getString(R.string.text_exit) to {
						requireActivity().finishAffinity()
					},
				isCancellable = false,
			),
		)
	}

	private fun navigateToDashboard() {
		findNavController().navigateWithAnimation(R.id.action_authLogin_to_dashboardMain)
	}
}