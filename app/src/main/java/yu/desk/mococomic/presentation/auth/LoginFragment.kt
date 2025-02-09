package yu.desk.mococomic.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentLoginBinding
import yu.desk.mococomic.presentation.component.InfoDialogBottomSheetArgs
import yu.desk.mococomic.presentation.component.InfoDialogData
import yu.desk.mococomic.utils.AuthHelper
import yu.desk.mococomic.utils.hideLoading
import yu.desk.mococomic.utils.navigateWithAnimation
import yu.desk.mococomic.utils.showLoading

class LoginFragment : Fragment() {
	private lateinit var binding: FragmentLoginBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		if (savedInstanceState == null) {
			if (AuthHelper.isUserLogin() || AuthHelper.isLogin(requireContext())) {
				navigateToDashboard()
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
		initViewListener()
	}

	private fun initViewListener() {
		binding.btnLogin.setOnClickListener {
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

		binding.btnLoginGuest.setOnClickListener {
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

	private fun showErrorDialog(message: String) {
		val data =
			InfoDialogBottomSheetArgs.Builder(
				InfoDialogData(
					description = message,
					positiveButton = "Close" to {},
				),
			)
		findNavController().navigate(R.id.dialogInfoBottomSheet, data.build().toBundle())
	}

	private fun navigateToDashboard() {
		findNavController().navigateWithAnimation(R.id.action_authLogin_to_dashboardMain)
	}
}