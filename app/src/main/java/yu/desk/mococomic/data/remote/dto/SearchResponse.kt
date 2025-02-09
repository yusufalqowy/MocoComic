package yu.desk.mococomic.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SearchResponse(
	@SerializedName("mangas")
	val mangas: List<ComicDto<ChapterDto>>? = listOf(),
)