package yu.desk.mococomic.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutAlertViewBinding
import yu.desk.mococomic.utils.setVisible

class AlertView
	@JvmOverloads
	constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0,
	) : ConstraintLayout(context, attrs, defStyleAttr) {
		private data class AlertData(
			@DrawableRes var iconRes: Int = R.drawable.ic_error,
			var toolbarTitle: String = "",
			var toolbarSubtitle: String = "",
			var title: String = "",
			var description: String = "",
			var actionButtonText: String = "",
		)

		private val binding = LayoutAlertViewBinding.inflate(LayoutInflater.from(context), this)
		private var alertData =
			AlertData(
				iconRes = R.drawable.ic_error,
				toolbarTitle = "",
				title = context.getString(R.string.text_something_went_wrong),
				description = context.getString(R.string.text_unexpected_error),
				actionButtonText = context.getString(R.string.text_retry),
			)

		@ColorInt
		private var iconTint: Int = 0

		@Dimension
		private var imageSize: Int = ViewGroup.LayoutParams.WRAP_CONTENT

		private var onActionClickListener: (() -> Unit)? = null
		private var onBackClickListener: (() -> Unit)? = null

		init {
			val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AlertView)
			try {
				iconTint = typedArray.getColor(R.styleable.AlertView_iconTint, 0)
				imageSize = typedArray.getDimensionPixelSize(R.styleable.AlertView_iconSize, 0)
				alertData.apply {
					toolbarTitle = typedArray.getString(R.styleable.AlertView_toolbarTitle) ?: ""
					iconRes = typedArray.getResourceId(R.styleable.AlertView_icon, R.drawable.ic_error)
					title = typedArray.getString(R.styleable.AlertView_title) ?: context.getString(R.string.text_something_went_wrong)
					description = typedArray.getString(R.styleable.AlertView_description) ?: context.getString(R.string.text_unexpected_error)
					actionButtonText = typedArray.getString(R.styleable.AlertView_actionButtonText) ?: context.getString(R.string.text_retry)
				}
			} finally {
				typedArray.recycle()
			}
			initView()
		}

		private fun initView() {
			binding.apply {
				ivIcon.apply {
					if (alertData.iconRes != 0) setImageDrawable(ContextCompat.getDrawable(context, alertData.iconRes))
					if (iconTint != 0) setColorFilter(iconTint)
					if (imageSize > 0) {
						updateLayoutParams {
							this.width = imageSize
							this.height = imageSize
						}
					}
				}

				tvAlertTitle.text = alertData.title
				tvAlertDescription.text = alertData.description

				btnAction.setVisible(alertData.actionButtonText.isNotEmpty())
				btnAction.text = alertData.actionButtonText
				btnAction.setOnClickListener {
					onActionClickListener?.invoke()
				}

				if (fitsSystemWindows && alertData.toolbarTitle.isNotBlank()) {
					ViewCompat.setOnApplyWindowInsetsListener(this.root) { _, insets ->
						val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
						alertToolbar.updateLayoutParams<MarginLayoutParams> {
							topMargin = systemBar.top
						}
						insets
					}
				}
				alertToolbar.setVisible(alertData.toolbarTitle.isNotBlank())
				alertToolbar.title = alertData.toolbarTitle
				alertToolbar.subtitle = alertData.toolbarSubtitle
				alertToolbar.setNavigationOnClickListener {
					onBackClickListener?.invoke()
				}
			}
			invalidate()
		}

		fun setAlertData(
			toolbarTitle: String? = null,
			toolbarSubtitle: String? = null,
			@DrawableRes iconRes: Int = 0,
			title: String? = null,
			description: String? = null,
			actionButtonText: String? = null,
		) {
			alertData.apply {
				toolbarTitle?.let {
					this.toolbarTitle = it
				}
				toolbarSubtitle?.let {
					this.toolbarSubtitle = it
				}
				iconRes.let {
					this.iconRes = it
				}
				title?.let {
					this.title = it
				}
				this.description = description ?: this.description
				this.actionButtonText = actionButtonText ?: ""
				initView()
			}
		}

		fun setIconTint(
			@ColorInt color: Int,
		) {
			iconTint = color
			initView()
		}

		fun setIconSize(
			@Dimension size: Int,
		) {
			imageSize = size
			initView()
		}

		fun addOnActionClickListener(listener: () -> Unit) {
			onActionClickListener = listener
		}

		fun addOnBackClickListener(listener: () -> Unit) {
			onBackClickListener = listener
		}
	}