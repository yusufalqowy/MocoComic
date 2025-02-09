package yu.desk.mococomic.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChapterDto(
	@SerializedName("chapter")
	val chapter: String? = "",
	@SerializedName("slug")
	val slug: String? = "",
	val isAlreadyRead: Boolean = false,
)