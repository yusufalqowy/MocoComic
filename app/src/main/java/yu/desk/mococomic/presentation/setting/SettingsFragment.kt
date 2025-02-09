package yu.desk.mococomic.presentation.setting

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.fragment.app.Fragment
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentSettingsBinding
import yu.desk.mococomic.presentation.component.AppThemeBottomSheet
import yu.desk.mococomic.presentation.component.ColorSchemeBottomSheet
import yu.desk.mococomic.presentation.component.LanguageBottomSheet
import yu.desk.mococomic.utils.*
import java.util.*

class SettingsFragment : Fragment() {
	private lateinit var binding: FragmentSettingsBinding
	private val biometricHelper by lazy { BiometricHelper(requireActivity()) }
	private val enrollLauncher by lazy {
		registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
			if (it.resultCode == Activity.RESULT_OK) {
				if (::binding.isInitialized) {
					binding.itemBiometric.performClick()
				}
			}
		}
	}
	private var biometricCheckedChange: CompoundButton.OnCheckedChangeListener? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		binding = FragmentSettingsBinding.inflate(inflater, container, false)
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
			biometricHelper.promptResults.flowWithLifecycle(lifecycle).collectLatest {
				when (it) {
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
						}
					}

					BiometricHelper.BiometricResult.AuthenticationSuccess -> {
						requireContext().setEnableBiometric(true)
						binding.itemBiometric.subtitle = getString(R.string.text_on)
						binding.swBiometric.setOnCheckedChangeListener(null)
						binding.swBiometric.isChecked = true
						binding.swBiometric.setOnCheckedChangeListener(biometricCheckedChange)
					}

					else -> Unit
				}
			}
		}
	}

	private fun initListener() {
		binding.apply {
			biometricCheckedChange =
				CompoundButton.OnCheckedChangeListener { _, _ ->
					binding.apply {
						swBiometric.isChecked = biometricHelper.isEnableBiometric()
						if (biometricHelper.isEnableBiometric()) {
							requireContext().setEnableBiometric(false)
							itemBiometric.subtitle = getString(R.string.text_off)
							swBiometric.setOnCheckedChangeListener(null)
							swBiometric.isChecked = false
							swBiometric.setOnCheckedChangeListener(biometricCheckedChange)
						} else {
							itemBiometric.performClick()
						}
					}
				}

			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}

			itemLanguage.setOnClickListener {
				LanguageBottomSheet.show(requireContext())
			}

			itemAppTheme.setOnClickListener {
				AppThemeBottomSheet.show(requireContext()) {
					itemAppTheme.trailingIcon = it.icon
					itemAppTheme.subtitle = getString(it.title)
					when (it) {
						AppThemeBottomSheet.AppTheme.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
						AppThemeBottomSheet.AppTheme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
						AppThemeBottomSheet.AppTheme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
					}
				}
			}

			itemColorScheme.setOnClickListener {
				ColorSchemeBottomSheet.show(requireContext()) {
					itemColorScheme.subtitle = it.title
					val color = if (it == ColorSchemeBottomSheet.ColorScheme.DYNAMIC) requireContext().primarySystemColor() else it.color
					itemColorScheme.trailingIconTint = color
					requireActivity().theme.applyStyle(it.style, true)
					refreshFragment()
				}
			}

			swShowBlockedComic.setOnCheckedChangeListener { _, checked ->
				itemEnableBlockedComic.subtitle = if (checked) getString(R.string.text_on) else getString(R.string.text_off)
				requireContext().setEnableBlockedComic(checked)
			}

			swBiometric.setOnCheckedChangeListener(biometricCheckedChange)

			itemBiometric.setOnClickListener {
				if (!biometricHelper.isEnableBiometric()) {
					biometricHelper.showBiometricPrompt(title = "Secure", description = "Use your biometrics to secure app.")
				} else {
					requireContext().setEnableBiometric(false)
					swBiometric.isChecked = false
				}
			}
		}
	}

	private fun initView() {
		val appTheme = requireContext().getAppTheme()
		val colorScheme = requireContext().getColorScheme()
		val isBlockedComicEnable = requireContext().getEnableBlockedComic()
		val language = Locale.getDefault()
		val isEnableBiometric = biometricHelper.isEnableBiometric()

		binding.apply {
			itemLanguage.subtitle = language.displayName
			itemLanguage.trailingIcon = getLanguageIcon(language)
			itemLanguage.trailingIconTint = null

			itemAppTheme.trailingIcon = appTheme.icon
			itemAppTheme.subtitle = getString(appTheme.title)

			itemColorScheme.subtitle = colorScheme.title
			val color = if (colorScheme == ColorSchemeBottomSheet.ColorScheme.DYNAMIC) requireContext().primarySystemColor() else colorScheme.color
			itemColorScheme.trailingIconTint = color
			itemEnableBlockedComic.subtitle = if (isBlockedComicEnable) getString(R.string.text_on) else getString(R.string.text_off)
			swShowBlockedComic.isChecked = isBlockedComicEnable

			itemBiometric.setVisible(biometricHelper.isBiometricAvailable())
			swBiometric.isChecked = isEnableBiometric
			itemBiometric.subtitle = if (isEnableBiometric) getString(R.string.text_on) else getString(R.string.text_off)
		}
	}

	private fun refreshFragment() {
		val id = findNavController(R.id.navHostMain).currentDestination?.id
		if (id != null) {
			findNavController(R.id.navHostMain).popBackStack(id, true)
			findNavController(R.id.navHostMain).navigate(id)
		}
	}
}