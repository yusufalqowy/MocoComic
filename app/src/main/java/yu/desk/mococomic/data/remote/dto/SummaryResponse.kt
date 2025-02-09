package yu.desk.mococomic.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SummaryResponse(
	@SerializedName("new_chapter")
	val newChapter: List<ComicDto<String>>? = listOf(),
	@SerializedName("new_manga")
	val newManga: List<ComicDto<String>>? = listOf(),
	@SerializedName("popular")
	val popular: List<ComicDto<String>>? = listOf(),
	@SerializedName("trending")
	val trending: List<ComicDto<String>>? = listOf(),
)