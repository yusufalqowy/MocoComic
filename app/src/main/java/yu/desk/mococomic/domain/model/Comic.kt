package yu.desk.mococomic.domain.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import yu.desk.mococomic.data.remote.dto.ChapterDto
import yu.desk.mococomic.data.remote.dto.ComicDto
import yu.desk.mococomic.utils.ComicType
import yu.desk.mococomic.utils.PagingType
import java.util.Date

@Parcelize
data class Comic(
	val title: String = "",
	val slug: String = "",
	val cover: String = "",
	val genres: List<String> = emptyList(),
	val lastChapter: Chapter? = null,
	val rating: Double = 0.0,
	val status: String = "",
	val type: ComicType = ComicType.Unknown,
	val nsfw: Boolean = false,
	val isFavorite: Boolean = false,
	val paging: PagingType? = null,
) : Parcelable {
	fun toFirebaseComic() =
		FirebaseComic(
			title = title,
			slug = slug,
			cover = cover,
		)
}

data class FirebaseComic(
	val title: String = "",
	val slug: String = "",
	val cover: String = "",
	val type: String = "",
	val createdAt: Date = Date(),
) {
	fun toMap() =
		mapOf(
			"title" to title,
			"slug" to slug,
			"cover" to cover,
			"type" to type,
			"created_at" to Timestamp(createdAt),
		)

	fun toComic() =
		Comic(
			title = title,
			slug = slug,
			cover = cover,
			type = ComicType.fromTitle(type),
		)

	companion object {
		fun fromMap(map: Map<String, Any>) =
			FirebaseComic(
				title = map["title"] as String? ?: "",
				slug = map["slug"] as String? ?: "",
				cover = map["cover"] as String? ?: "",
				type = map["type"] as String? ?: "",
				createdAt = (map["created_at"] as Timestamp?)?.toDate() ?: Date(),
			)
	}
}

fun <T> ComicDto<T>.toComic(): Comic =
	Comic(
		title = title ?: "",
		slug = slug ?: "",
		cover = cover ?: "https://placehold.jp/300x400.png?text=Not%20Found",
		genres = genres ?: emptyList(),
		lastChapter =
			when {
				lastChapter is ChapterDto -> lastChapter.toChapter()
				lastChapter != null -> Chapter(title = lastChapter.toString())
				else -> null
			},
		rating = rating?.toDoubleOrNull() ?: 0.0,
		status = status ?: "",
		type = type?.let { ComicType.fromTitle(it) } ?: ComicType.Unknown,
		nsfw = nsfw ?: false,
	)