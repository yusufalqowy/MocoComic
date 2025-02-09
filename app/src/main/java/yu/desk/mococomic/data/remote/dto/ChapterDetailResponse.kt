package yu.desk.mococomic.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ChapterDetailResponse(
	@SerializedName("content")
	val content: List<String>? = listOf(),
	@SerializedName("title")
	val title: String? = "",
)