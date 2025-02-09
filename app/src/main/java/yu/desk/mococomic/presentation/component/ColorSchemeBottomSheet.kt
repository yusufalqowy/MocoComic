package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.BottomsheetChipFilterBinding
import yu.desk.mococomic.utils.*

class ColorSchemeBottomSheet(context: Context) : BottomSheetDialog(context) {
	enum class ColorScheme(
		val title: String,
		@ColorRes val color: Int,
		@StyleRes val style: Int,
	) {
		DYNAMIC("Dynamic", com.google.android.material.R.color.material_dynamic_primary40, R.style.Theme_MocoComic),
		RED("Red", R.color.red_primary, R.style.Theme_MocoComic_Red),
		YELLOW("Yellow", R.color.yellow_primary, R.style.Theme_MocoComic_Yellow),
		GREEN("Green", R.color.green_primary, R.style.Theme_MocoComic_Green),
		BLUE("Blue", R.color.blue_primary, R.style.Theme_MocoComic_Blue),
		PURPLE("Purple", R.color.purple_primary, R.style.Theme_MocoComic_Purple),
		ORANGE("Orange", R.color.orange_primary, R.style.Theme_MocoComic_Orange),
		GRAY("Gray", R.color.gray_primary, R.style.Theme_MocoComic_Gray),
		;

		companion object {
			fun getByName(name: String) =
				when {
					name.equals(RED.name, true) -> RED
					name.equals(YELLOW.name, true) -> YELLOW
					name.equals(GREEN.name, true) -> GREEN
					name.equals(BLUE.name, true) -> BLUE
					name.equals(PURPLE.name, true) -> PURPLE
					name.equals(ORANGE.name, true) -> ORANGE
					name.equals(GRAY.name, true) -> GRAY
					else -> DYNAMIC
				}

			fun getByTitle(title: String) =
				when {
					title.equals(RED.title, true) -> RED
					title.equals(YELLOW.title, true) -> YELLOW
					title.equals(GREEN.title, true) -> GREEN
					title.equals(BLUE.title, true) -> BLUE
					title.equals(PURPLE.title, true) -> PURPLE
					title.equals(ORANGE.title, true) -> ORANGE
					title.equals(GRAY.title, true) -> GRAY
					else -> DYNAMIC
				}
		}
	}

	private lateinit var binding: BottomsheetChipFilterBinding
	private var selectedTheme = ColorScheme.DYNAMIC
	private var onThemeChanged: ((ColorScheme) -> Unit)? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = BottomsheetChipFilterBinding.inflate(layoutInflater)
		selectedTheme = context.getColorScheme()
		initView()
		setContentView(binding.root)
	}

	private fun initView() {
		binding.apply {
			toolbar.title = context.getString(R.string.text_color_scheme)
			ColorScheme.entries.forEach {
				val chip =
					Chip(context).apply {
						val drawable =
							ChipDrawable.createFromAttributes(
								context,
								null,
								0,
								com.google.android.material.R.style.Widget_Material3_Chip_Filter,
							)
						setChipDrawable(drawable)
						val color = context.primarySystemColor()
						closeIcon = ContextCompat.getDrawable(context, R.drawable.bg_circle_menu)
						closeIconTint = if (it != ColorScheme.DYNAMIC) ContextCompat.getColorStateList(context, it.color) else ContextCompat.getColorStateList(context, color)
						isCloseIconVisible = true
						text = it.title.plus(" ")
						isChecked = it == selectedTheme
						setOnClickListener { _ ->
							onThemeChanged?.invoke(it)
							context.appSharedPreferences().putString(SharedPrefKeys.COLOR_SCHEME, it.name)
							dismiss()
						}
					}
				chipGroup.addView(chip)
			}
		}
	}

	companion object {
		fun show(context: Context, onThemeChangeListener: ((ColorScheme) -> Unit)? = null) {
			ColorSchemeBottomSheet(context).apply {
				onThemeChanged = onThemeChangeListener
			}.show()
		}
	}
}