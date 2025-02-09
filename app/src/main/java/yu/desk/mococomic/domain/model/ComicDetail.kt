package yu.desk.mococomic.domain.model

import yu.desk.mococomic.data.remote.dto.ComicDetailResponse

data class ComicDetail(
	val comic: Comic = Comic(),
	val author: String = "",
	val chapters: List<Chapter> = listOf(),
	val cover: String = "",
	val genres: List<String> = listOf(),
	val published: String = "",
	val score: String = "",
	val serialization: String = "",
	val status: String = "",
	val subtitle: String = "",
	val synopsis: String = "",
	val title: String = "",
)

fun ComicDetailResponse.toComicDetail() =
	ComicDetail(
		author = author ?: "",
		chapters = chapters?.map { it.toChapter() } ?: listOf(),
		cover = cover ?: "",
		genres = genres ?: listOf(),
		published = published ?: "",
		score = score ?: "",
		serialization = serialization ?: "",
		status = status ?: "",
		subtitle = subtitle ?: "",
		synopsis = synopsis ?: "",
		title = title ?: "",
	)