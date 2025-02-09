package yu.desk.mococomic.presentation.component

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.parcelize.Parcelize
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentInfoBottomSheetBinding
import yu.desk.mococomic.utils.setVisible

class InfoDialogBottomSheet : BottomSheetDialogFragment() {
	private lateinit var binding: FragmentInfoBottomSheetBinding

	private val args: InfoDialogBottomSheetArgs by navArgs()
	val data: InfoDialogData by lazy {
		args.data
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View {
		binding = FragmentInfoBottomSheetBinding.inflate(inflater, container, false)
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

	private fun initListener() {
		binding.apply {
			data.positiveButton?.let { (title, action) ->
				btnPositive.setOnClickListener {
					action.invoke()
					dismiss()
				}
			}

			data.negativeButton?.let { (title, action) ->
				btnNegative.setOnClickListener {
					action.invoke()
					dismiss()
				}
			}
		}
	}

	private fun initView() {
		binding.apply {
			ivIcon.setImageResource(data.image)
			var title = data.title
			if (title.isEmpty()) {
				title = getString(R.string.text_something_went_wrong)
			}
			tvTitle.text = title
			tvDescription.setVisible(data.description.isNotEmpty())
			tvDescription.text = data.description
			data.negativeButton?.let {
				btnNegative.text = it.first
			}
			btnNegative.setVisible(data.negativeButton != null)
			data.positiveButton?.let {
				btnPositive.text = it.first
			}
			btnPositive.setVisible(data.positiveButton != null)
		}
	}
}

@Parcelize
data class InfoDialogData(
	val title: String = "",
	val description: String = "",
	@DrawableRes val image: Int = R.drawable.img_choice,
	val negativeButton: (Pair<String, () -> Unit>)? = null,
	val positiveButton: (Pair<String, () -> Unit>)? = "Yes" to {},
) : Parcelable