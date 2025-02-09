package yu.desk.mococomic.presentation.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutListItemViewBinding
import yu.desk.mococomic.utils.loadImage
import yu.desk.mococomic.utils.scaleCropTop
import yu.desk.mococomic.utils.setVisible

class ListItemView
	@JvmOverloads
	constructor(
		context: Context,
		attrs: AttributeSet? = null,
		defStyleAttr: Int = 0,
	) : MaterialCardView(context, attrs, defStyleAttr) {
		private var binding: LayoutListItemViewBinding =
			LayoutListItemViewBinding.inflate(
				LayoutInflater.from(context),
				this,
			)
		private val defaultContainerColor = Color.TRANSPARENT
		private val selectedContainerColor =
			MaterialColors.getColor(
				context,
				com.google.android.material.R.attr.colorSecondaryContainer,
				context.getColor(com.google.android.material.R.color.design_default_color_secondary),
			)
		private val defaultIconColor =
			MaterialColors.getColor(
				context,
				com.google.android.material.R.attr.colorOnSurfaceVariant,
				binding.ivLeadingIcon.solidColor,
			)
		private val selectedIconColor =
			MaterialColors.getColor(
				context,
				com.google.android.material.R.attr.colorPrimary,
				context.getColor(com.google.android.material.R.color.design_default_color_primary),
			)
		private val defaultTextColor =
			MaterialColors.getColor(
				context,
				com.google.android.material.R.attr.colorOnSurface,
				binding.tvTitle.currentTextColor,
			)
		private val selectedTextColor =
			MaterialColors.getColor(
				context,
				com.google.android.material.R.attr.colorOnSecondaryContainer,
				binding.tvTitle.currentTextColor,
			)

		private val defaultPadding = resources.getDimensionPixelSize(R.dimen.dp_16)
		private val defaultSize = resources.getDimensionPixelSize(R.dimen.dp_56)

		var title: String = ""
			set(value) {
				field = value
				binding.tvTitle.setVisible(true)
				binding.tvTitle.text = value
				invalidate()
			}

		var subtitle: String = ""
			set(value) {
				field = value
				binding.tvSubtitle.setVisible(value.isNotEmpty())
				binding.tvSubtitle.text = value
				invalidate()
			}

		@DrawableRes
		var leadingIcon: Int = 0
			set(value) {
				field = value
				binding.ivLeadingIcon.setVisible(value != 0)
				binding.ivLeadingIcon.setImageResource(value)
				invalidate()
			}

		@ColorRes
		var leadingIconTint: Int = 0
			set(value) {
				field = value
				if (value != 0) {
					binding.ivLeadingIcon.setColorFilter(context.getColor(value))
					invalidate()
				}
			}

		@DrawableRes
		var leadingImageResource: Int = 0
			set(value) {
				field = value
				binding.ivImageItem.setVisible(value != 0)
				binding.ivImageItem.setImageResource(value)
				invalidate()
			}

		var isLeadingIconVisible: Boolean = leadingIcon != 0 && leadingImageResource == 0
			set(value) {
				field = value
				binding.ivLeadingIcon.setVisible(value)
				invalidate()
			}
			get() = leadingIcon != 0 && leadingImageResource == 0

		var isLeadingImageVisible: Boolean = leadingImageResource != 0
			set(value) {
				field = value
				binding.ivImageItem.setVisible(value)
				invalidate()
			}
			get() = leadingImageResource != 0

		@DrawableRes
		var trailingIcon: Int = 0
			set(value) {
				field = value
				binding.ivTrailingIcon.setVisible(value != 0)
				binding.ivTrailingIcon.setImageResource(value)
				invalidate()
			}

		@ColorRes
		var trailingIconTint: Int? = null
			set(value) {
				field = value
				if (value != null && value != 0) {
					binding.ivTrailingIcon.setColorFilter(context.getColor(value))
				} else {
					binding.ivTrailingIcon.clearColorFilter()
				}
				invalidate()
			}
		var isTrailingIconVisible: Boolean = trailingIcon != 0
			set(value) {
				field = value
				binding.ivTrailingIcon.setVisible(value)
				invalidate()
			}
			get() = trailingIcon != 0

		var isItemSelected: Boolean = false
			set(value) {
				field = value
				if (value) {
					this.setCardBackgroundColor(selectedContainerColor)
					binding.ivLeadingIcon.setColorFilter(defaultIconColor)
					binding.ivTrailingIcon.setColorFilter(selectedIconColor)
					binding.tvTitle.setTextColor(selectedTextColor)
				} else {
					this.setCardBackgroundColor(defaultContainerColor)
					binding.ivLeadingIcon.setColorFilter(defaultIconColor)
					binding.ivTrailingIcon.setColorFilter(defaultIconColor)
					binding.tvTitle.setTextColor(defaultTextColor)
				}
				invalidate()
			}

		init {
			val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListItemView)
			try {
				title = typedArray.getString(R.styleable.ListItemView_title) ?: ""
				leadingIcon = typedArray.getResourceId(R.styleable.ListItemView_leadingIcon, 0)
				leadingIconTint = typedArray.getResourceId(R.styleable.ListItemView_leadingIconTint, 0)
				trailingIcon = typedArray.getResourceId(R.styleable.ListItemView_trailingIcon, 0)
				trailingIconTint = typedArray.getResourceId(R.styleable.ListItemView_trailingIconTint, 0)
				subtitle = typedArray.getString(R.styleable.ListItemView_subtitle) ?: ""
				leadingImageResource = typedArray.getResourceId(R.styleable.ListItemView_leadingImage, 0)
			} finally {
				typedArray.recycle()
			}

			initView()
		}

		private fun initView() {
			binding.apply {
				cardElevation = 0f
				strokeWidth = 0
				setContentPadding(defaultPadding, 0, defaultPadding, 0)

				tvTitle.text = title
				tvSubtitle.text = subtitle
				tvSubtitle.setVisible(subtitle.isNotEmpty())
				ivLeadingIcon.setVisible(isLeadingIconVisible)
				ivTrailingIcon.setVisible(isTrailingIconVisible)
				ivImageItem.setVisible(isLeadingImageVisible)
				if (leadingIcon != 0) {
					ivLeadingIcon.setImageResource(leadingIcon)
				}
				if (leadingIconTint != 0) {
					ivLeadingIcon.setColorFilter(context.getColor(leadingIconTint))
				}
				if (trailingIcon != 0) {
					ivTrailingIcon.setImageResource(trailingIcon)
				}

				if (trailingIconTint != null && trailingIconTint != 0) {
					binding.ivTrailingIcon.setColorFilter(context.getColor(trailingIconTint!!))
				} else {
					binding.ivTrailingIcon.clearColorFilter()
				}

				if (leadingImageResource != 0) {
					ivImageItem.setImageResource(leadingImageResource)
				}
				if (isItemSelected) {
					setCardBackgroundColor(selectedContainerColor)
					ivLeadingIcon.setColorFilter(defaultIconColor)
					ivTrailingIcon.setColorFilter(selectedIconColor)
					tvTitle.setTextColor(selectedTextColor)
					tvTitle.setTextColor(defaultTextColor)
				} else {
					setCardBackgroundColor(defaultContainerColor)
					ivLeadingIcon.setColorFilter(defaultIconColor)
					ivTrailingIcon.setColorFilter(defaultIconColor)
					tvTitle.setTextColor(defaultTextColor)
					tvTitle.setTextColor(defaultIconColor)
				}
			}
		}

		fun setLabelActive(active: Boolean) {
			if (active) {
				binding.tvTitle.setTextColor(selectedTextColor)
			} else {
				binding.tvTitle.setTextColor(defaultTextColor)
			}
			invalidate()
		}

		fun setLeadingImageUrl(url: String) {
			binding.ivImageItem.setVisible(true)
			binding.ivImageItem.loadImage(url) {
				binding.ivImageItem.scaleCropTop()
				// I'm not sure why, but this is needed to refresh the image on first init in the RecyclerView.
				binding.ivImageItem.post {
					binding.ivImageItem.scaleCropTop()
				}
			}
			invalidate()
		}
	}