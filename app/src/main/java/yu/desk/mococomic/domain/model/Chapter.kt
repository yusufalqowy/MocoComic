package yu.desk.mococomic.domain.model

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize
import yu.desk.mococomic.data.remote.dto.ChapterDto
import java.util.Date

@Parcelize
data class Chapter(
	val title: String = "",
	val slug: String = "",
	val isAlreadyRead: Boolean = false,
) : Parcelable

data class FirebaseChapter(
	val chapter: String = "",
	val slug: String = "",
	val createdAt: Date = Date(),
	val comic: FirebaseComic? = null,
) {
	fun toMap() =
		hashMapOf(
			"chapter" to chapter,
			"slug" to slug,
			"created_at" to Timestamp(createdAt),
			"comic" to comic?.toMap(),
		)

	companion object {
		@Suppress("UNCHECKED_CAST")
		fun fromMap(map: Map<String, Any>) =
			FirebaseChapter(
				chapter = map["chapter"] as String? ?: "",
				slug = map["slug"] as String? ?: "",
				comic =
					if (map["comic"] != null &&
						map["comic"] is Map<*, *>
					) {
						FirebaseComic.fromMap(map["comic"] as Map<String, Any>)
					} else {
						null
					},
				createdAt = (map["created_at"] as Timestamp?)?.toDate() ?: Date(),
			)
	}
}

fun ChapterDto.toChapter() =
	Chapter(
		title = chapter ?: "-",
		slug = slug ?: "",
		isAlreadyRead = isAlreadyRead,
	)