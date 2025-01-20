package yu.desk.mococomic.presentation.component

import android.animation.LayoutTransition
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.core.view.updateLayoutParams
import com.google.android.material.shape.ShapeAppearanceModel
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.LayoutShimmerContainerBinding
import kotlin.math.roundToInt

class ShimmerContainer
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr) {
        private var binding: LayoutShimmerContainerBinding

        @StyleRes
        private var shapeAppearance: Int = com.google.android.material.R.style.ShapeAppearance_Material3_Corner_Medium

        @StyleRes
        private var shapeAppearanceOverlay: Int = com.google.android.material.R.style.ShapeAppearance_Material3_Corner_Medium
        private var shimmerWidth: Int = ViewGroup.LayoutParams.MATCH_PARENT
        private var shimmerHeight: Int = ViewGroup.LayoutParams.MATCH_PARENT
        private var shimmerWidthPercent: Float = 1f
        private var shimmerHeightPercent: Float = 1f

        init {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ShimmerContainer, 0, 0)
            try {
                binding = LayoutShimmerContainerBinding.inflate(LayoutInflater.from(context), this)
                shapeAppearance =
                    typedArray.getResourceId(
                        R.styleable.ShimmerContainer_shapeAppearance,
                        com.google.android.material.R.style.ShapeAppearance_Material3_Corner_Medium
                    )
                shapeAppearanceOverlay =
                    typedArray.getResourceId(
                        R.styleable.ShimmerContainer_shapeAppearanceOverlay,
                        com.google.android.material.R.style.ShapeAppearance_Material3_Corner_Medium
                    )
                shimmerWidth =
                    typedArray.getDimensionPixelSize(
                        R.styleable.ShimmerContainer_shimmerWidth,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                shimmerHeight =
                    typedArray.getDimensionPixelSize(
                        R.styleable.ShimmerContainer_shimmerHeight,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                shimmerHeightPercent = typedArray.getFloat(R.styleable.ShimmerContainer_shimmerHeightPercent, 1f)
                shimmerWidthPercent = typedArray.getFloat(R.styleable.ShimmerContainer_shimmerWidthPercent, 1f)
            } finally {
                typedArray.recycle()
            }
            initView()
        }

        private fun initView() {
            this.layoutTransition =
                LayoutTransition().also {
                    it.enableTransitionType(LayoutTransition.CHANGING)
                }
            binding.cvShimmer.shapeAppearanceModel =
                ShapeAppearanceModel.builder(context, shapeAppearance, shapeAppearanceOverlay).build()
            binding.shimmerLayout.updateLayoutParams {
                this.width = shimmerWidth
                this.height = shimmerHeight
            }
        }

        override fun onMeasure(
            widthMeasureSpec: Int,
            heightMeasureSpec: Int,
        ) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val totalWidth =
                (
                    if (shimmerWidth ==
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ) {
                        measuredWidth
                    } else {
                        shimmerWidth
                    }
                ).times(shimmerWidthPercent).roundToInt()
            val totalHeight =
                (
                    if (shimmerHeight ==
                        ViewGroup.LayoutParams.MATCH_PARENT
                    ) {
                        measuredHeight
                    } else {
                        shimmerHeight
                    }
                ).times(shimmerHeightPercent).roundToInt()
            binding.shimmerLayout.measure(
                MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY)
            )
        }

        fun startShimmer() {
            val visibility = if (shimmerHeight == ViewGroup.LayoutParams.MATCH_PARENT) INVISIBLE else GONE
            this.getChildAt(1).visibility = visibility
            binding.shimmerLayout.visibility = VISIBLE
            binding.shimmerLayout.startShimmer()
        }

        fun stopShimmer() {
            this.getChildAt(1).visibility = VISIBLE
            binding.shimmerLayout.visibility = GONE
            binding.shimmerLayout.stopShimmer()
        }
    }