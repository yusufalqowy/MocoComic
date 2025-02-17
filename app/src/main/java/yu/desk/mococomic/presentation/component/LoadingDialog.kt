package yu.desk.mococomic.presentation.component

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import yu.desk.mococomic.R

class LoadingDialog : DialogFragment() {
	companion object {
		const val TAG = "LoadingDialog"

		fun show(fragmentManager: FragmentManager) {
			val fragment = fragmentManager.findFragmentByTag(TAG) as LoadingDialog? ?: LoadingDialog()
			if (fragment.dialog?.isShowing != true) {
				fragment.show(fragmentManager, TAG)
			}
		}

		fun hide(fragmentManager: FragmentManager) {
			val fragment = fragmentManager.findFragmentByTag(TAG) as LoadingDialog?
			fragment?.dismiss()
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		isCancelable = false
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?,
	): View? {
		dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
		return inflater.inflate(R.layout.fragment_loading_dialog, container, false)
	}
}