package yu.desk.mococomic.presentation.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.color.MaterialColors
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutErrorViewBinding
import yu.desk.mococomic.utils.dp

class ErrorView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var binding: LayoutErrorViewBinding

    @DrawableRes
    private var icon: Int = R.drawable.ic_error

    @ColorRes
    private var iconTint: Int = 0

    @Dimension
    private var iconSize: Int = ViewGroup.LayoutParams.WRAP_CONTENT
    var title: String = "Something Went Wrong"
        set(value) {
            field = value
            binding.tvTitle.text = value
            invalidate()
        }
    var toolbarTitle: String = ""
        set(value) {
            field = value
            binding.toolbar.title = value
            invalidate()
        }
    var description: String = "An unexpected error occurred. Please try again later."
        set(value) {
            field = value
            binding.tvDescription.text = value
            invalidate()
        }
    private var retryButtonVisible: Boolean = false
    private var toolbarVisible: Boolean = false
    private var onRetryClickListener: (() -> Unit)? = null
    private var onBackClickListener: (() -> Unit)? = null

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ErrorView)
        try {
            binding = LayoutErrorViewBinding.inflate(LayoutInflater.from(context), this)

            icon = typedArray.getResourceId(R.styleable.ErrorView_icon, R.drawable.ic_error)
            iconTint = typedArray.getResourceId(R.styleable.ErrorView_iconTint, 0)
            iconSize = typedArray.getDimensionPixelSize(R.styleable.ErrorView_iconSize, 0)
            title = typedArray.getString(R.styleable.ErrorView_title) ?: "Something Went Wrong"
            description = typedArray.getString(R.styleable.ErrorView_description) ?: "An unexpected error occurred. Please try again later."
            retryButtonVisible = typedArray.getBoolean(R.styleable.ErrorView_showRetryButton, false)
            toolbarVisible = typedArray.getBoolean(R.styleable.ErrorView_showToolbar, false)
            toolbarTitle = typedArray.getString(R.styleable.ErrorView_toolbarTitle) ?: ""
        } finally {
            typedArray.recycle()
        }
        initView()
    }

    private fun initView() {

        this.setBackgroundColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurface, ResourcesCompat.getColor(resources, com.google.android.material.R.color.design_default_color_surface, null)))

        if (toolbarVisible) {
            if(fitsSystemWindows){
                ViewCompat.setOnApplyWindowInsetsListener(this.rootView) { _, insets ->
                    val systemBar = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                    binding.toolbar.updateLayoutParams<MarginLayoutParams> {
                        topMargin = systemBar.top
                    }
                    insets
                }
            }
        }

        binding.ivImageHeader.apply {
            setImageDrawable(ContextCompat.getDrawable(context, icon))
            if (iconTint != 0) setColorFilter(ContextCompat.getColor(context, iconTint))
            if(iconSize > 0){
                updateLayoutParams {
                    this.width = iconSize
                    this.height = iconSize
                }
            }

        }
        binding.tvTitle.text = title
        binding.tvDescription.text = description
        binding.btnRetry.visibility = if (retryButtonVisible) VISIBLE else GONE
        binding.btnRetry.setOnClickListener {
            onRetryClickListener?.invoke()
        }
        binding.toolbar.visibility = if (toolbarVisible) VISIBLE else GONE
        binding.toolbar.title = toolbarTitle
        binding.toolbar.setNavigationOnClickListener {
            onBackClickListener?.invoke()
        }

    }

    fun setImageHeaderDrawableRes(drawableRes: Int) {
        icon = drawableRes
        binding.ivImageHeader.setImageDrawable(ContextCompat.getDrawable(context, icon))
    }

    fun addOnRetryClickListener(listener: () -> Unit) {
        onRetryClickListener = listener
    }

    fun addOnBackClickListener(listener: () -> Unit) {
        onBackClickListener = listener
    }
}
