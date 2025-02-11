package yu.desk.mococomic.presentation.privacypolicy

import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING
import androidx.webkit.WebViewFeature
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentWebViewBinding
import yu.desk.mococomic.utils.MyConstants

class PrivacyPolicyFragment : Fragment() {
	lateinit var binding: FragmentWebViewBinding

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentWebViewBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		initView()
		initListener()
	}

	private fun initListener() {
		binding.apply {
			toolbar.setNavigationOnClickListener {
				requireActivity().onBackPressedDispatcher.onBackPressed()
			}
		}
	}

	@Suppress("DEPRECATION")
	private fun initView() {
		binding.apply {
			val nightModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
			if (nightModeFlag == Configuration.UI_MODE_NIGHT_YES) {
				if (SDK_INT >= 33) {
					if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
						WebSettingsCompat.setAlgorithmicDarkeningAllowed(webView.settings, true)
					}
				} else {
					if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
						WebSettingsCompat.setForceDark(
							webView.settings,
							WebSettingsCompat.FORCE_DARK_ON,
						)
					}

					if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
						WebSettingsCompat.setForceDarkStrategy(
							webView.settings,
							DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING,
						)
					}
				}
			}

			toolbar.title = getString(R.string.text_privacy_policy)

			webView.loadData(MyConstants.HTML_PRIVACY_POLICY, "text/html", "UTF-8")
		}
	}
}