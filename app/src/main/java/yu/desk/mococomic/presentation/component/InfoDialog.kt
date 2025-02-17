package yu.desk.mococomic.presentation.component

import android.content.res.Configuration
import android.os.Bundle
import android.os.Parcelable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.parcelize.Parcelize
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentInfoBottomSheetBinding
import yu.desk.mococomic.utils.setInvisible
import yu.desk.mococomic.utils.setVisible

private const val INFO_DIALOG_KEY = "info_dialog_key"

class InfoDialogBottomSheet : BottomSheetDialogFragment() {
	companion object {
		const val TAG = "InfoDialogBottomSheet"

		fun show(
			dialogData: InfoDialogData,
			fragmentManager: FragmentManager,
		) {
			val dialogFragment =
				fragmentManager.findFragmentByTag(TAG) as InfoDialogBottomSheet?
					?: InfoDialogBottomSheet().apply {
						onPositiveButtonListener = dialogData.positiveButton?.second
						onNegativeButtonListener = dialogData.negativeButton?.second
						arguments =
							Bundle().apply {
								putParcelable(INFO_DIALOG_KEY, dialogData.toParcel())
							}
					}
			if (dialogFragment.dialog?.isShowing != true) {
				dialogFragment.show(fragmentManager, TAG)
			}
		}

		fun hide(fragmentManager: FragmentManager) {
			val dialogFragment = fragmentManager.findFragmentByTag(TAG) as InfoDialogBottomSheet?
			dialogFragment?.dismiss()
		}
	}

	private lateinit var binding: FragmentInfoBottomSheetBinding
	private var onPositiveButtonListener: (() -> Unit)? = null
	private var onNegativeButtonListener: (() -> Unit)? = null
	private lateinit var data: InfoDialogDataParcel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		data = arguments?.let {
			BundleCompat.getParcelable(
				it,
				INFO_DIALOG_KEY,
				InfoDialogDataParcel::class.java,
			)
		} ?: InfoDialogDataParcel()
		isCancelable = data.isCancellable
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
		binding.initView(data)
		initListener()
	}

	private fun initListener() {
		binding.apply {
			btnPositive.setOnClickListener {
				onPositiveButtonListener?.invoke()
				dismiss()
			}

			btnNegative.setOnClickListener {
				onNegativeButtonListener?.invoke()
				dismiss()
			}
		}
	}
}

class InfoDialog : DialogFragment() {
	companion object {
		const val TAG = "InfoDialog"

		fun show(
			dialogData: InfoDialogData,
			fragmentManager: FragmentManager,
		) {
			val dialogFragment =
				fragmentManager.findFragmentByTag(TAG) as InfoDialog?
					?: InfoDialog().apply {
						onPositiveButtonListener = dialogData.positiveButton?.second
						onNegativeButtonListener = dialogData.negativeButton?.second
						arguments =
							Bundle().apply {
								putParcelable(INFO_DIALOG_KEY, dialogData.toParcel())
							}
					}
			if (dialogFragment.dialog?.isShowing != true) {
				dialogFragment.show(fragmentManager, TAG)
			}
		}

		fun hide(fragmentManager: FragmentManager) {
			val dialogFragment =
				fragmentManager.findFragmentByTag(TAG) as InfoDialog?
			dialogFragment?.dismiss()
		}
	}

	private lateinit var binding: FragmentInfoBottomSheetBinding
	private var onPositiveButtonListener: (() -> Unit)? = null
	private var onNegativeButtonListener: (() -> Unit)? = null
	private lateinit var data: InfoDialogDataParcel

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		data = arguments?.let {
			BundleCompat.getParcelable(
				it,
				INFO_DIALOG_KEY,
				InfoDialogDataParcel::class.java,
			)
		} ?: InfoDialogDataParcel()
		isCancelable = data.isCancellable
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
		binding.initView(data)
		binding.dragHandle.setInvisible(true)
		initListener()
	}

	override fun onStart() {
		super.onStart()
		dialog?.window?.let {
			val width =
				resources.displayMetrics.widthPixels.let { w ->
					if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
						w
					} else {
						w.times(
							0.5,
						).toInt()
					}
				}
			it.decorView.setBackgroundResource(R.drawable.bg_popup_menu)
			it.setGravity(Gravity.CENTER)
			it.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
		}
	}

	private fun initListener() {
		binding.apply {
			btnPositive.setOnClickListener {
				onPositiveButtonListener?.invoke()
				dismiss()
			}

			btnNegative.setOnClickListener {
				onNegativeButtonListener?.invoke()
				dismiss()
			}
		}
	}
}

private fun FragmentInfoBottomSheetBinding.initView(data: InfoDialogDataParcel) {
	ivIcon.setImageResource(data.image)
	var title = data.title
	if (title.isEmpty()) {
		title = root.context.getString(R.string.text_something_went_wrong)
	}
	tvTitle.text = title
	tvDescription.setVisible(data.description.isNotEmpty())
	tvDescription.text = data.description
	data.negativeButton?.let {
		btnNegative.text = it
	}
	btnNegative.setVisible(data.negativeButton != null)
	data.positiveButton?.let {
		btnPositive.text = it
	}
	btnPositive.setVisible(data.positiveButton != null)
}

@Parcelize
private class InfoDialogDataParcel(
	val title: String = "",
	val description: String = "",
	@DrawableRes val image: Int = R.drawable.img_choice,
	val negativeButton: String? = null,
	val positiveButton: String? = "Yes",
	val isCancellable: Boolean = true,
) : Parcelable

private fun InfoDialogData.toParcel() =
	InfoDialogDataParcel(
		title,
		description = description,
		image = image,
		negativeButton = negativeButton?.first,
		positiveButton = positiveButton?.first,
		isCancellable = isCancellable,
	)

data class InfoDialogData(
	val title: String = "",
	val description: String = "",
	@DrawableRes val image: Int = R.drawable.img_choice,
	val negativeButton: (Pair<String, () -> Unit>)? = null,
	val positiveButton: (Pair<String, () -> Unit>)? = "Yes" to {},
	val isCancellable: Boolean = true,
)