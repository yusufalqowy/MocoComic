package yu.desk.mococomic.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.RecyclerView
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowMetricsCalculator
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.sidesheet.SideSheetBehavior
import yu.desk.mococomic.presentation.component.ShimmerContainer
import java.util.Calendar
import kotlin.math.roundToInt

fun getGreetingMessage(): String {
    val c = Calendar.getInstance()
    val timeOfDay = c.get(Calendar.HOUR_OF_DAY)

    return when (timeOfDay) {
        in 0..11 -> "Good Morning"
        in 12..15 -> "Good Afternoon"
        in 16..20 -> "Good Evening"
        in 21..23 -> "Good Night"
        else -> "Hello"
    }
}

fun Context.getWindowSizeClasses(): WindowSizeClass {
    val metrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)

    val width = metrics.bounds.width()
    val height = metrics.bounds.height()
    val density = getDisplayMetrics().density
    return WindowSizeClass.compute(width / density, height / density)
}

fun Context.isSizeMobile() = getWindowSizeClasses().windowWidthSizeClass == WindowWidthSizeClass.COMPACT

fun Context.isSizeTablet() =
    getWindowSizeClasses().windowWidthSizeClass == WindowWidthSizeClass.MEDIUM ||
        getWindowSizeClasses().windowWidthSizeClass == WindowWidthSizeClass.EXPANDED

fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun View.setInvisible(invisible: Boolean) {
    visibility = if (invisible) View.INVISIBLE else View.VISIBLE
}

fun Fragment.getDrawableResource(
    @DrawableRes id: Int,
): Drawable? = ResourcesCompat.getDrawable(resources, id, null)

fun Activity.getDrawableResource(
    @DrawableRes id: Int,
): Drawable? = ResourcesCompat.getDrawable(resources, id, null)

fun Context.getDrawableResource(
    @DrawableRes id: Int,
): Drawable? = ResourcesCompat.getDrawable(resources, id, null)

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Float.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun TextView.isTextOverflowing(isOverflow: (Boolean) -> Unit) {
    this.post {
        this.layout?.let {
            if (it.lineCount > 0) {
                if (it.getEllipsisCount(it.lineCount - 1) > 0) {
                    isOverflow(true)
                } else {
                    isOverflow(false)
                }
            } else {
                isOverflow(false)
            }
        }
    }
}

fun Context.getDisplayMetrics(): DisplayMetrics = this.resources.displayMetrics

fun ImageView.loadImage(
    url: String,
    enableLoading: Boolean = true,
) {
    this.setBackgroundColor(
        MaterialColors.getColor(this.context, com.google.android.material.R.attr.colorOutlineVariant, Color.DKGRAY)
    )
    this.load(url) {
        crossfade(true)
        if (enableLoading) {
            val drawable =
                CircularProgressIndicator(this@loadImage.context).apply {
                    indicatorInset = 16.dp
                    indicatorSize = 8.dp
                    trackCornerRadius = 4.dp
                    trackThickness = 1.dp
                    isIndeterminate = true
                }
            placeholder(drawable.indeterminateDrawable)
        }
    }
}

fun ImageView.scaleCropTop() {
    val drawable = drawable ?: return
    val matrix = imageMatrix
    val viewWidth = width - paddingLeft - paddingRight
    val viewHeight = height - paddingTop - paddingBottom
    val drawableWidth = drawable.intrinsicWidth
    val drawableHeight = drawable.intrinsicHeight

    val scale =
        if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
            viewHeight.toFloat() / drawableHeight.toFloat()
        } else {
            viewWidth.toFloat() / drawableWidth.toFloat()
        }
    matrix.setScale(scale, scale)
    imageMatrix = matrix
}

fun BottomSheetBehavior<ConstraintLayout>.open() {
    state = BottomSheetBehavior.STATE_EXPANDED
}

fun BottomSheetBehavior<ConstraintLayout>.close() {
    state = BottomSheetBehavior.STATE_HIDDEN
}

fun BottomSheetBehavior<ConstraintLayout>.toggle() {
    Log.e("STATE", state.toString())
    state =
        if (state ==
            BottomSheetBehavior.STATE_EXPANDED
        ) {
            BottomSheetBehavior.STATE_HIDDEN
        } else {
            BottomSheetBehavior.STATE_EXPANDED
        }
}

fun SideSheetBehavior<ConstraintLayout>.open() {
    state = SideSheetBehavior.STATE_EXPANDED
}

fun SideSheetBehavior<ConstraintLayout>.close() {
    state = SideSheetBehavior.STATE_HIDDEN
}

fun SideSheetBehavior<ConstraintLayout>.toggle() {
    state =
        if (state ==
            SideSheetBehavior.STATE_EXPANDED
        ) {
            SideSheetBehavior.STATE_HIDDEN
        } else {
            SideSheetBehavior.STATE_EXPANDED
        }
}

fun RecyclerView.initRecyclerView(
    hasFixedSize: Boolean = true,
    layoutManger: RecyclerView.LayoutManager? = this.layoutManager,
    adapter: (RecyclerView) -> RecyclerView.Adapter<*>,
) {
    this.setHasFixedSize(hasFixedSize)
    this.layoutManager = layoutManger
    this.adapter = adapter.invoke(this)
}

fun Fragment.findNavController(
    @IdRes navId: Int,
) = requireActivity().findNavController(navId)

fun NavController.navigateWithAnimation(
    @IdRes id: Int,
    args: Bundle? = null,
    navOptions: NavOptions? = null,
) {
    val newNavOptions =
        navOptions {
            navOptions?.let {
                launchSingleTop = it.shouldLaunchSingleTop()
                restoreState = it.shouldRestoreState()
                if (it.popUpToRoute != null) {
                    popUpTo(it.popUpToRoute!!) {
                        inclusive = it.isPopUpToInclusive()
                        saveState = it.shouldPopUpToSaveState()
                    }
                } else if (it.popUpToRouteClass != null) {
                    popUpTo(it.popUpToRouteClass!!)
                } else if (it.popUpToRouteObject != null && it.popUpToRouteObject !is Int) {
                    popUpTo(it.popUpToRouteObject!!) {
                        inclusive = it.isPopUpToInclusive()
                        saveState = it.shouldPopUpToSaveState()
                    }
                } else {
                    popUpTo(popUpToId) {
                        inclusive = it.isPopUpToInclusive()
                        saveState = it.shouldPopUpToSaveState()
                    }
                }
            }
            anim {
                enter = yu.desk.mococomic.R.anim.slide_in_right
                exit = yu.desk.mococomic.R.anim.slide_out_left
                popEnter = yu.desk.mococomic.R.anim.slide_in_left
                popExit = yu.desk.mococomic.R.anim.slide_out_right
            }
        }

    this.navigate(id, args, newNavOptions)
}

fun NavController.navigateWithAnimation(
    directions: NavDirections,
    navOptions: NavOptions? = null,
) {
    val newNavOptions =
        navOptions {
            navOptions?.let {
                launchSingleTop = it.shouldLaunchSingleTop()
                restoreState = it.shouldRestoreState()
                if (it.popUpToRoute != null) {
                    popUpTo(it.popUpToRoute!!) {
                        inclusive = it.isPopUpToInclusive()
                        saveState = it.shouldPopUpToSaveState()
                    }
                } else if (it.popUpToRouteClass != null) {
                    popUpTo(it.popUpToRouteClass!!)
                } else if (it.popUpToRouteObject != null) {
                    popUpTo(it.popUpToRouteObject!!) {
                        inclusive = it.isPopUpToInclusive()
                        saveState = it.shouldPopUpToSaveState()
                    }
                } else {
                    popUpTo(popUpToId) {
                        inclusive = it.isPopUpToInclusive()
                        saveState = it.shouldPopUpToSaveState()
                    }
                }
            }
            anim {
                enter = yu.desk.mococomic.R.anim.slide_in_right
                exit = yu.desk.mococomic.R.anim.slide_out_left
                popEnter = yu.desk.mococomic.R.anim.slide_in_left
                popExit = yu.desk.mococomic.R.anim.slide_out_right
            }
        }
    navigate(directions, newNavOptions)
}

fun View.loadingShimmer(show: Boolean = true) {
    if (this.parent is ShimmerContainer) {
        if (show) {
            (this.parent as ShimmerContainer).startShimmer()
        } else {
            (this.parent as ShimmerContainer).stopShimmer()
        }
    }
}