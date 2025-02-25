package yu.desk.mococomic.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.Keep
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import androidx.window.layout.WindowMetricsCalculator
import coil3.*
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.request.*
import coil3.util.DebugLogger
import coil3.util.Logger
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.sidesheet.SideSheetBehavior
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yu.desk.mococomic.R
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.component.*
import java.net.URL
import java.net.URLEncoder
import java.util.*
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
	result: ((Drawable) -> Unit)? = null,
) {
	var firstScaleType = scaleType
	if (tag != null) {
		firstScaleType = tag as ImageView.ScaleType
	} else {
		tag = firstScaleType
	}
	val primaryColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorPrimary, ContextCompat.getColor(context, context.primarySystemColor()))
	val loadingDrawable =
		CircularProgressDrawable(context).also {
			it.strokeWidth = 10f
			it.centerRadius = 40f
			it.strokeCap = Paint.Cap.ROUND
			it.setColorSchemeColors(primaryColor)
			it.start()
		}
	val placeholder =
		if (enableLoading) {
			loadingDrawable
		} else {
			drawable
		}
	setBackgroundColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOutlineVariant, Color.DKGRAY))
	val request =
		ImageRequest.Builder(context)
			.data(url)
			.target(this)
			.diskCacheKey(url)
			.memoryCacheKey(url)
			.placeholder(placeholder)
			.placeholderMemoryCacheKey(url)
			.listener(
				onError = { _, _ ->
					scaleType = ImageView.ScaleType.FIT_CENTER
				},
				onSuccess = { _, res ->
					scaleType = firstScaleType
					val image = res.image.asDrawable(resources)
					setImageDrawable(image)
					result?.invoke(image)
				},
				onCancel = {
					scaleType = firstScaleType
				},
				onStart = {
					scaleType =
						if (enableLoading) {
							ImageView.ScaleType.FIT_CENTER
						} else {
							firstScaleType
						}
				},
			)
			.build()
	context.getImageLoader().enqueue(request)
}

fun Context.getImageLoader(): ImageLoader {
	val errorImage = ResourcesCompat.getDrawable(resources, R.drawable.bg_error, null)
	errorImage?.setTint(MaterialColors.getColor(this, com.google.android.material.R.attr.colorError, 0))
	return ImageLoader
		.Builder(this)
		.memoryCachePolicy(CachePolicy.ENABLED)
		.diskCachePolicy(CachePolicy.ENABLED)
		.crossfade(true)
		.memoryCache {
			MemoryCache
				.Builder()
				.maxSizePercent(this, 0.5)
				.weakReferencesEnabled(true)
				.build()
		}.diskCache {
			DiskCache
				.Builder()
				.maxSizePercent(0.5)
				.directory(cacheDir)
				.build()
		}
		.error(errorImage?.asImage())
		.logger(DebugLogger(Logger.Level.Warn))
		.components {
			if (SDK_INT >= 28) {
				add(AnimatedImageDecoder.Factory())
			} else {
				add(GifDecoder.Factory())
			}
		}.build()
}

fun ImageView.scaleCropTop() {
	val drawable = drawable ?: return
	scaleType = ImageView.ScaleType.MATRIX
	val matrix = Matrix()
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
	invalidate()
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
	this.adapter = null
	this.setHasFixedSize(hasFixedSize)
	this.layoutManager = layoutManger
	this.adapter = adapter.invoke(this)
}

fun Fragment.findNavController(
	@IdRes navId: Int,
): NavController {
	return try {
		requireActivity().findNavController(navId)
	} catch (e: Exception) {
		findNavController()
	}
}

fun NavController.navigateWithAnimation(
	@IdRes id: Int,
	args: Bundle? = null,
	builder: NavOptionsBuilder.() -> Unit = {},
) {
	try {
		val newBuilder: NavOptionsBuilder.() -> Unit = {
			apply(builder)
			anim {
				enter = R.anim.slide_in_right
				exit = R.anim.slide_out_left
				popEnter = R.anim.slide_in_left
				popExit = R.anim.slide_out_right
			}
		}
		val navOptions = navOptions(newBuilder)
		navigate(resId = id, args = args, navOptions = navOptions)
	} catch (e: Exception) {
		e.printStackTrace()
	}
}

fun NavController.navigateWithAnimation(
	directions: NavDirections,
	builder: NavOptionsBuilder.() -> Unit = {},
) {
	try {
		val newBuilder: NavOptionsBuilder.() -> Unit = {
			apply(builder)
			anim {
				enter = R.anim.slide_in_right
				exit = R.anim.slide_out_left
				popEnter = R.anim.slide_in_left
				popExit = R.anim.slide_out_right
			}
		}
		val navOptions = navOptions(newBuilder)
		navigate(directions = directions, navOptions = navOptions)
	} catch (e: Exception) {
		e.printStackTrace()
	}
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

fun View.showSnackBar(
	message: String,
	duration: Int = Snackbar.LENGTH_LONG,
) = Snackbar.make(this, message, duration).show()

fun Date.getTimeAgo() =
	try {
		val now = System.currentTimeMillis()
		// Mendapatkan teks "time ago"
		DateUtils
			.getRelativeTimeSpanString(
				this.time,
				now,
				DateUtils.MINUTE_IN_MILLIS,
			).toString()
	} catch (e: Exception) {
		e.printStackTrace()
		"Invalid Date"
	}

fun Context.openTab(url: String) {
	val builder =
		CustomTabsIntent
			.Builder()
			.setShowTitle(false)
			.setInstantAppsEnabled(true)
			.setUrlBarHidingEnabled(true)
			.build()

	builder.launchUrl(this, Uri.parse(url))
}

suspend fun Comic.getCommentUrl(): String {
	val url = "${MyConstants.WEB_URL}manga/$slug"
	return withContext(Dispatchers.IO) {
		try {
			val id =
				URL(url)
					.readText()
					.trim()
					.substringAfter("\"disqusIdentifier\":\"")
					.substringBefore("\",\"disqusShortname\"")
					.replace("\\", "")
			val encodeId = URLEncoder.encode(id, "UTF-8")
			return@withContext "${MyConstants.WEB_URL}comments?id=$encodeId"
		} catch (e: Exception) {
			return@withContext "$url/#disqus_thread"
		}
	}
}

suspend fun Chapter.getCommentUrl(): String {
	val url = "${MyConstants.WEB_URL}$slug"
	return withContext(Dispatchers.IO) {
		try {
			val id =
				URL(url)
					.readText()
					.trim()
					.substringAfter("\"disqusIdentifier\":\"")
					.substringBefore("\",\"disqusShortname\"")
					.replace("\\", "")
			val encodeId = URLEncoder.encode(id, "UTF-8")
			return@withContext "${MyConstants.WEB_URL}comments?id=$encodeId"
		} catch (e: Exception) {
			return@withContext "$url/#disqus_thread"
		}
	}
}

fun Fragment.showLoading() {
	Log.e(this.tag, this.javaClass.simpleName)
	LoadingDialog.show(childFragmentManager)
}

fun Fragment.hideLoading() {
	Log.e(this.tag, this.javaClass.simpleName)
	LoadingDialog.hide(childFragmentManager)
}

fun Fragment.showInfoBottomSheet(infoData: InfoDialogData) {
	InfoDialogBottomSheet.show(infoData, childFragmentManager)
}

fun Fragment.showInfoDialog(infoData: InfoDialogData) {
	InfoDialog.show(infoData, childFragmentManager)
}

@ColorRes
fun Context.primarySystemColor(): Int {
	return if (isDarkMode()) {
		if (DynamicColors.isDynamicColorAvailable()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				android.R.color.system_primary_dark
			} else {
				android.R.color.system_accent1_200
			}
		} else {
			com.google.android.material.R.color.material_dynamic_primary80
		}
	} else {
		if (DynamicColors.isDynamicColorAvailable()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
				android.R.color.system_primary_light
			} else {
				android.R.color.system_accent1_600
			}
		} else {
			com.google.android.material.R.color.material_dynamic_primary40
		}
	}
}

fun Context.isDarkMode(): Boolean {
	val darkModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
	return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}

fun Context.isNotificationGranted(): Boolean {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		ActivityCompat.checkSelfPermission(
			this,
			Manifest.permission.POST_NOTIFICATIONS,
		) == PackageManager.PERMISSION_GRANTED
	} else {
		// On older versions, the permission is implicitly granted if the app is installed.
		true
	}
}

fun requestNotificationPermission(
	activity: ComponentActivity,
	onGranted: () -> Unit,
	onDenied: () -> Unit,
) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
		val requestPermissionLauncher =
			activity.registerForActivityResult(
				ActivityResultContracts.RequestPermission(),
			) { isGranted: Boolean ->
				if (isGranted) {
					onGranted()
				} else {
					if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.POST_NOTIFICATIONS)) {
						AlertDialog.Builder(activity)
							.setTitle("Notification Permission")
							.setMessage("This app needs notification permission to alert you about important events.")
							.setPositiveButton("OK") { dialog, _ ->
								requestNotificationPermission(activity, onGranted, onDenied)
								dialog.dismiss()
							}
							.setNegativeButton("Cancel") { dialog, _ ->
								onDenied()
								dialog.dismiss()
							}
							.show()
					} else {
						activity.openNotificationSettings()
					}
				}
			}
		requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
	} else {
		onGranted()
	}
}

fun Activity.openNotificationSettings() {
	val intent = Intent()
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
		intent.putExtra(Settings.EXTRA_APP_PACKAGE, this.packageName)
	} else {
		intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
		intent.data = Uri.fromParts("package", this.packageName, null)
	}
	startActivity(intent)
}

fun Context.copyTextToClipboard(text: String, label: String = "Copied Text") {
	val clipboardManager = ContextCompat.getSystemService(this, ClipboardManager::class.java)
	if (clipboardManager != null) {
		val clipData = ClipData.newPlainText(label, text)
		clipboardManager.setPrimaryClip(clipData)
		// Provide feedback for Android 12L and lower
		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
			Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
		}
	}
}

fun View.getStateView(): StateView? {
	return if (parent is StateView) {
		parent as StateView
	} else {
		null
	}
}

fun Window.hideSystemBars() {
	val windowInsetsController =
		WindowCompat.getInsetsController(this, decorView)
	windowInsetsController.systemBarsBehavior =
		WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
	windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
}

@Keep
fun Window.hideNavigationBars() {
	val windowInsetsController =
		WindowCompat.getInsetsController(this, decorView)
	windowInsetsController.systemBarsBehavior =
		WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
	windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
}

@Keep
fun Window.showSystemBar() {
	val windowInsetsController =
		WindowCompat.getInsetsController(this, decorView)
	windowInsetsController.systemBarsBehavior =
		WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
	windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
}

fun Activity.getStatusBarHeight(): Int {
	val rectangle = Rect()
	window.decorView.getWindowVisibleDisplayFrame(rectangle)
	return rectangle.top
}

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun Fragment.getResourceStatusBarHeight(): Int {
	return resources.getDimensionPixelSize(resources.getIdentifier("status_bar_height", "dimen", "android"))
}

@DrawableRes
fun getLanguageIcon(locale: Locale): Int {
	return when (locale.language) {
		"in" -> R.drawable.ic_indonesia
		else -> R.drawable.ic_usa
	}
}

fun <T> Flow<T>.launchAndCollectLatest(lifecycleOwner: LifecycleOwner, lifecycleState: Lifecycle.State = Lifecycle.State.STARTED, collector: suspend (value: T) -> Unit) {
	lifecycleOwner.apply {
		lifecycleScope.launch {
			repeatOnLifecycle(lifecycleState) {
				this@launchAndCollectLatest.collectLatest {
					collector(it)
				}
			}
		}
	}
}