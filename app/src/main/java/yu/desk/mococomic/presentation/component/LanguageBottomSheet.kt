package yu.desk.mococomic.presentation.component

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.BottomsheetChipFilterBinding
import yu.desk.mococomic.utils.dp
import yu.desk.mococomic.utils.getLanguageIcon
import java.util.*

class LanguageBottomSheet(context: Context) : BottomSheetDialog(context) {
	private lateinit var binding: BottomsheetChipFilterBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = BottomsheetChipFilterBinding.inflate(layoutInflater)
		initView()
		setContentView(binding.root)
	}

	private fun initView() {
		binding.apply {
			toolbar.title = context.getString(R.string.text_language)
			val currentLocale = Locale.getDefault().language
			val localeList = listOf(Locale.forLanguageTag("en"), Locale.forLanguageTag("in"))
			localeList.forEach {
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
						closeIcon = ContextCompat.getDrawable(context, getLanguageIcon(it))
						isCloseIconVisible = true
						closeIconTint = null
						text = it.displayName
						isChecked = it.language == currentLocale
						setOnClickListener { _ ->
							AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(it.language))
							dismiss()
						}
					}
				chipGroup.addView(chip)
			}
		}
	}

	companion object {
		fun show(context: Context) =
			LanguageBottomSheet(context).also {
				it.show()
			}
	}
}