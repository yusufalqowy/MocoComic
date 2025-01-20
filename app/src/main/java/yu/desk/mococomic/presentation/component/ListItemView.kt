package yu.desk.mococomic.presentation.component

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutListItemViewBinding
import yu.desk.mococomic.utils.setVisible

class ListItemView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : MaterialCardView(context, attrs, defStyleAttr) {
        var binding: LayoutListItemViewBinding = LayoutListItemViewBinding.inflate(LayoutInflater.from(context), this)
        private val defaultContainerColor = Color.TRANSPARENT
        private val selectedContainerColor =
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorSecondaryContainer,
                context.getColor(com.google.android.material.R.color.design_default_color_secondary)
            )
        private val defaultIconColor =
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorOnSurfaceVariant,
                binding.ivLeadingIcon.solidColor
            )
        private val selectedIconColor =
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorPrimary,
                context.getColor(com.google.android.material.R.color.design_default_color_primary)
            )
        private val defaultTextColor =
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorOnSurface,
                binding.tvLabel.currentTextColor
            )
        private val selectedTextColor =
            MaterialColors.getColor(
                context,
                com.google.android.material.R.attr.colorOnSecondaryContainer,
                binding.tvLabel.currentTextColor
            )
        private val defaultPaddingHorizontal = resources.getDimensionPixelSize(R.dimen.dp_16)

        var label: String = ""
            set(value) {
                field = value
                binding.tvLabel.text = value
                invalidate()
            }

        private var enableDefaultPadding: Boolean = true

        @DrawableRes
        var leadingIcon: Int = 0
            set(value) {
                field = value
                binding.ivLeadingIcon.setImageResource(value)
                invalidate()
            }
        var isLeadingIconVisible: Boolean = true
            set(value) {
                field = value
                binding.ivLeadingIcon.setVisible(value)
                invalidate()
            }

        @DrawableRes
        var trailingIcon: Int = 0
            set(value) {
                field = value
                binding.ivTrailingIcon.setImageResource(value)
                invalidate()
            }
        var isTrailingIconVisible: Boolean = true
            set(value) {
                field = value
                binding.ivTrailingIcon.setVisible(value)
                invalidate()
            }
        var isItemSelected: Boolean = false
            set(value) {
                field = value
                if (value) {
                    this.setCardBackgroundColor(selectedContainerColor)
                    binding.ivLeadingIcon.setColorFilter(defaultIconColor)
                    binding.ivTrailingIcon.setColorFilter(selectedIconColor)
                    binding.tvLabel.setTextColor(selectedTextColor)
                } else {
                    this.setCardBackgroundColor(defaultContainerColor)
                    binding.ivLeadingIcon.setColorFilter(defaultIconColor)
                    binding.ivTrailingIcon.setColorFilter(defaultIconColor)
                    binding.tvLabel.setTextColor(defaultTextColor)
                }
                invalidate()
            }

        init {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListItemView)
            try {
                label = typedArray.getString(R.styleable.ListItemView_label) ?: ""
                leadingIcon = typedArray.getResourceId(R.styleable.ListItemView_leadingIcon, 0)
                trailingIcon = typedArray.getResourceId(R.styleable.ListItemView_trailingIcon, 0)
                enableDefaultPadding = typedArray.getBoolean(R.styleable.ListItemView_enableDefaultPadding, true)
            } finally {
                typedArray.recycle()
            }
            initView()
        }

        private fun initView() {
            binding.apply {
                cardElevation = 0f
                strokeWidth = 0

                if (enableDefaultPadding) {
                    setContentPadding(0, 0, 0, 0)
                    container.setPadding(defaultPaddingHorizontal, 0, defaultPaddingHorizontal, 0)
                }

                tvLabel.text = label
                ivLeadingIcon.setVisible(leadingIcon != 0)
                ivTrailingIcon.setVisible(trailingIcon != 0)
                if (leadingIcon != 0) {
                    ivLeadingIcon.setImageResource(leadingIcon)
                }
                if (trailingIcon != 0) {
                    ivTrailingIcon.setImageResource(trailingIcon)
                }
                if (isItemSelected) {
                    setCardBackgroundColor(selectedContainerColor)
                    binding.ivLeadingIcon.setColorFilter(defaultIconColor)
                    binding.ivTrailingIcon.setColorFilter(selectedIconColor)
                } else {
                    setCardBackgroundColor(defaultContainerColor)
                    binding.ivLeadingIcon.setColorFilter(defaultIconColor)
                    binding.ivTrailingIcon.setColorFilter(defaultIconColor)
                }
            }
        }
    }