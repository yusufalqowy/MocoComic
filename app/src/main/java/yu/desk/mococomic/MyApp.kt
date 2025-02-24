package yu.desk.mococomic

import android.app.Application
import android.content.res.Configuration
import android.os.Build.VERSION.SDK_INT
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.asImage
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import coil3.util.Logger
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import yu.desk.mococomic.utils.getModeNight

@HiltAndroidApp
class MyApp :
	Application(),
	SingletonImageLoader.Factory {
	override fun onCreate() {
		DynamicColors.applyToActivitiesIfAvailable(this)
		AppCompatDelegate.setDefaultNightMode(getModeNight())
		super.onCreate()
		FirebaseApp.initializeApp(this)
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		AppCompatDelegate.setDefaultNightMode(getModeNight())
		super.onConfigurationChanged(newConfig)
	}

	override fun newImageLoader(context: PlatformContext): ImageLoader {
		val errorImage = ResourcesCompat.getDrawable(resources, R.drawable.bg_error, null)
		errorImage?.setTint(MaterialColors.getColor(context, com.google.android.material.R.attr.colorError, 0))
		return ImageLoader
			.Builder(context)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCache {
				MemoryCache
					.Builder()
					.maxSizePercent(context, 0.5)
					.weakReferencesEnabled(true)
					.build()
			}.diskCache {
				DiskCache
					.Builder()
					.maxSizePercent(0.5)
					.directory(cacheDir)
					.build()
			}.crossfade(true)
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
}