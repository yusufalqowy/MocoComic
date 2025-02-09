package yu.desk.mococomic.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import yu.desk.mococomic.presentation.component.AppThemeBottomSheet
import yu.desk.mococomic.presentation.component.ColorSchemeBottomSheet
import yu.desk.mococomic.utils.SharedPrefKeys.APP_PREFERENCES_NAME

object SharedPrefKeys {
	const val APP_PREFERENCES_NAME = "moco_comic_preferences"
	const val IS_LOGIN_KEY = "is_login"
	const val IS_GUEST_KEY = "is_guest"
	const val IS_ENABLE_BLOCKED_COMIC = "is_enable_blocked_comic"
	const val IS_ENABLE_BIOMETRIC = "is_enable_biometric"
	const val APP_THEME = "app_theme"
	const val COLOR_SCHEME = "color_scheme"
}

fun Context.getEnableBlockedComic() = appSharedPreferences().getBoolean(SharedPrefKeys.IS_ENABLE_BLOCKED_COMIC, true)

fun Context.setEnableBlockedComic(value: Boolean) = appSharedPreferences().putBoolean(SharedPrefKeys.IS_ENABLE_BLOCKED_COMIC, value)

fun Context.setEnableBiometric(value: Boolean) = appSharedPreferences().putBoolean(SharedPrefKeys.IS_ENABLE_BIOMETRIC, value)

fun Context.getEnableBiometric() = appSharedPreferences().getBoolean(SharedPrefKeys.IS_ENABLE_BIOMETRIC, false)

fun Context.getIsLogin() = appSharedPreferences().getBoolean(SharedPrefKeys.IS_LOGIN_KEY, false)

fun Context.setIsLogin(value: Boolean) = appSharedPreferences().putBoolean(SharedPrefKeys.IS_LOGIN_KEY, value)

fun Context.getIsGuest() = appSharedPreferences().getBoolean(SharedPrefKeys.IS_GUEST_KEY, false)

fun Context.setIsGuest(value: Boolean) = appSharedPreferences().putBoolean(SharedPrefKeys.IS_GUEST_KEY, value)

fun Context.getAppTheme() = AppThemeBottomSheet.AppTheme.getByName(this.appSharedPreferences().getString(SharedPrefKeys.APP_THEME, AppThemeBottomSheet.AppTheme.SYSTEM.name) ?: "")

fun Context.setAppTheme(value: AppThemeBottomSheet.AppTheme) = this.appSharedPreferences().putString(SharedPrefKeys.APP_THEME, value.name)

fun Context.getColorScheme() = ColorSchemeBottomSheet.ColorScheme.getByName(this.appSharedPreferences().getString(SharedPrefKeys.COLOR_SCHEME, ColorSchemeBottomSheet.ColorScheme.DYNAMIC.name) ?: "")

fun Context.setColorScheme(value: ColorSchemeBottomSheet.ColorScheme) = this.appSharedPreferences().putString(SharedPrefKeys.COLOR_SCHEME, value.name)

fun Context.getModeNight() =
	when (this.appSharedPreferences().getString(SharedPrefKeys.APP_THEME, AppThemeBottomSheet.AppTheme.SYSTEM.name)) {
		AppThemeBottomSheet.AppTheme.SYSTEM.name -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
		AppThemeBottomSheet.AppTheme.LIGHT.name -> AppCompatDelegate.MODE_NIGHT_NO
		AppThemeBottomSheet.AppTheme.DARK.name -> AppCompatDelegate.MODE_NIGHT_YES
		else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
	}

fun SharedPreferences.putString(
	key: String,
	value: String,
) {
	edit { putString(key, value) }
}

fun SharedPreferences.putInt(
	key: String,
	value: Int,
) {
	edit { putInt(key, value) }
}

fun SharedPreferences.putBoolean(
	key: String,
	value: Boolean,
) {
	edit { putBoolean(key, value) }
}

fun SharedPreferences.putLong(
	key: String,
	value: Long,
) {
	edit { putLong(key, value) }
}

fun SharedPreferences.putFloat(
	key: String,
	value: Float,
) {
	edit { putFloat(key, value) }
}

fun Context.appSharedPreferences(): SharedPreferences {
	val masterKey: MasterKey =
		MasterKey.Builder(this)
			.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
			.build()

	return EncryptedSharedPreferences.create(
		this,
		APP_PREFERENCES_NAME,
		masterKey,
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
	)
}