package yu.desk.mococomic.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ComicDto<T>(
	@SerializedName("cover")
	val cover: String? = "",
	@SerializedName("genres")
	val genres: List<String>? = listOf(),
	@SerializedName("last_chapter")
	val lastChapter: T? = null,
	@SerializedName("nsfw")
	val nsfw: Boolean? = false,
	@SerializedName("rating")
	val rating: String? = "",
	@SerializedName("slug")
	val slug: String? = "",
	@SerializedName("status")
	val status: String? = "",
	@SerializedName("title")
	val title: String? = "",
	@SerializedName("type")
	val type: String? = "",
)