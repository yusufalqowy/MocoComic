package yu.desk.mococomic.utils

object MyConstants {
	init {
		System.loadLibrary("native-lib")
	}

	private external fun baseUrl(): String

	private external fun webUrl(): String

	val BASE_URL = baseUrl()
	val WEB_URL = webUrl()

	// SavedState Handle Key
	const val BLOCKED_COMIC = "blocked_comic"
	const val FAVORITE_COMIC = "favorite_comic"
}