package yu.desk.mococomic.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ComicDetailResponse(
	@SerializedName("author")
	val author: String? = "",
	@SerializedName("chapters")
	val chapters: List<ChapterDto>? = listOf(),
	@SerializedName("cover")
	val cover: String? = "",
	@SerializedName("genres")
	val genres: List<String>? = listOf(),
	@SerializedName("published")
	val published: String? = "",
	@SerializedName("score")
	val score: String? = "",
	@SerializedName("serialization")
	val serialization: String? = "",
	@SerializedName("status")
	val status: String? = "",
	@SerializedName("subtitle")
	val subtitle: String? = "",
	@SerializedName("synopsis")
	val synopsis: String? = "",
	@SerializedName("title")
	val title: String? = "",
)