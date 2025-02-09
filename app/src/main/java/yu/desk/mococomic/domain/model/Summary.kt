package yu.desk.mococomic.domain.model

import yu.desk.mococomic.data.remote.dto.SummaryResponse

data class Summary(
	val newChapter: List<Comic> = listOf(),
	val newManga: List<Comic> = listOf(),
	val popular: List<Comic> = listOf(),
	val trending: List<Comic> = listOf(),
)

fun SummaryResponse.toSummary() =
	Summary(
		newChapter = newChapter?.map { it.toComic() } ?: listOf(),
		newManga = newManga?.map { it.toComic() } ?: listOf(),
		popular = popular?.map { it.toComic() } ?: listOf(),
		trending = trending?.map { it.toComic() } ?: listOf(),
	)