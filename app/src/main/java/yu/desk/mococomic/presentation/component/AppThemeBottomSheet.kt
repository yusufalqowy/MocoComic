package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.BottomsheetChipFilterBinding
import yu.desk.mococomic.utils.*

class AppThemeBottomSheet(context: Context) : BottomSheetDialog(context) {
	enum class AppTheme(
		@StringRes
		val title: Int,
		@DrawableRes val icon: Int,
	) {
		SYSTEM(R.string.text_follow_by_system, R.drawable.ic_theme_system),
		LIGHT(R.string.text_light_mode, R.drawable.ic_sun),
		DARK(R.string.text_dark_mode, R.drawable.ic_moon),
		;

		companion object {
			fun getByName(name: String) =
				when {
					name.equals(LIGHT.name, true) -> LIGHT
					name.equals(DARK.name, true) -> DARK
					else -> SYSTEM
				}

			fun getByTitle(
				@StringRes title: Int,
			) =
				when (title) {
					R.string.text_follow_by_system -> SYSTEM
					R.string.text_light_mode -> LIGHT
					R.string.text_dark_mode -> DARK
					else -> SYSTEM
				}
		}
	}

	private lateinit var binding: BottomsheetChipFilterBinding
	private var selectedTheme = AppTheme.SYSTEM
	private var onThemeChanged: ((AppTheme) -> Unit)? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = BottomsheetChipFilterBinding.inflate(layoutInflater)
		selectedTheme = context.getAppTheme()
		initView()

		setContentView(binding.root)
	}

	private fun initView() {
		binding.apply {
			AppTheme.entries.forEach {
				val chip =
					Chip(context).apply {
						layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 64.dp)
						val drawable =
							ChipDrawable.createFromAttributes(
								context,
								null,
								0,
								com.google.android.material.R.style.Widget_Material3_Chip_Filter,
							)
						setChipDrawable(drawable)
						closeIcon = ContextCompat.getDrawable(context, it.icon)
						isCloseIconVisible = true
						text = context.getString(it.title).plus(" ")
						isChecked = it == selectedTheme
						setOnClickListener { _ ->
							onThemeChanged?.invoke(it)
							context.appSharedPreferences().putString(SharedPrefKeys.APP_THEME, it.name)
							dismiss()
						}
					}
				chipGroup.addView(chip)
			}
		}
	}

	companion object {
		fun show(context: Context, onThemeChangeListener: ((AppTheme) -> Unit)? = null) {
			AppThemeBottomSheet(context).apply {
				onThemeChanged = onThemeChangeListener
			}.show()
		}
	}
}