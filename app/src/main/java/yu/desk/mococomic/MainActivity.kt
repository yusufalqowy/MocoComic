package yu.desk.mococomic

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.databinding.ActivityMainBinding
import yu.desk.mococomic.presentation.component.InfoDialog
import yu.desk.mococomic.presentation.component.InfoDialogData
import yu.desk.mococomic.utils.*

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var splashScreen: SplashScreen
	private var isWaiting = true
	private var savedInstanceState: Bundle? = null
	private val biometricHelper by lazy { BiometricHelper(this) }
	private val enrollLauncher by lazy {
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == RESULT_OK) {
				biometricHelper.showBiometricPrompt(title = getString(R.string.text_secure), description = getString(R.string.text_use_biometrics_to_use_app))
			}
		}
	}
	private val demoManager by lazy { DemoManager(this) }

	override fun onCreate(savedInstanceState: Bundle?) {
		theme.applyStyle(getColorScheme().style, true)
		splashScreen = installSplashScreen()
		enableEdgeToEdge()
		super.onCreate(savedInstanceState)
		this.savedInstanceState = savedInstanceState
		initObserver()
		initListener()
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
	}

	private fun initListener() {
		if (biometricHelper.isBiometricAvailable() && biometricHelper.isEnableBiometric() && AuthHelper.isLogin(this) && savedInstanceState == null) {
			biometricHelper.showBiometricPrompt(title = getString(R.string.text_secure), description = getString(R.string.text_use_biometrics_to_use_app))
		} else {
			// Splash screen
			lifecycleScope.launch {
				// Add this to show splash screen for 2 seconds
				delay(1000)
				isWaiting = false
			}
		}

		if (BuildConfig.DEBUG) {
			requestNotificationPermission(this, onGranted = {}, onDenied = {})
		}

		splashScreen.setKeepOnScreenCondition {
			isWaiting
		}
	}

	override fun onResume() {
		super.onResume()
		this.theme.applyStyle(getColorScheme().style, true)
		checkDemoStatus()
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

	private fun showDemoOverDialog() {
		InfoDialog.show(
			dialogData =
				InfoDialogData(
					image = R.drawable.img_attention,
					title = getString(R.string.text_trial_period_expired),
					description = getString(R.string.text_your_trial_period_has_ended),
					positiveButton =
						getString(R.string.text_exit) to {
							finishAffinity()
						},
					isCancellable = false,
				),
			fragmentManager = supportFragmentManager,
		)
	}

	private fun initObserver() {
		lifecycleScope.launch {
			biometricHelper.promptResults.flowWithLifecycle(lifecycle).collectLatest {
				when (it) {
					is BiometricHelper.BiometricResult.AuthenticationError -> {
						finishAffinity()
					}

					BiometricHelper.BiometricResult.AuthenticationNotSet -> {
						if (Build.VERSION.SDK_INT >= 30) {
							val enrollIntent =
								Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
									putExtra(
										Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
										BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
									)
								}
							enrollLauncher.launch(enrollIntent)
						} else {
							isWaiting = false
						}
					}

					BiometricHelper.BiometricResult.AuthenticationSuccess -> {
						isWaiting = false
					}

					BiometricHelper.BiometricResult.AuthenticationFailed -> {
						finishAffinity()
					}

					else -> {
						setEnableBiometric(false)
						isWaiting = false
					}
				}
			}
		}
	}
}