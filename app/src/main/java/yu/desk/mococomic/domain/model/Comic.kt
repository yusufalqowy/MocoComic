package yu.desk.mococomic.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Comic(
    val title: String = "",
    val slug: String = "",
    val cover: String = "",
    val genres: List<String> = emptyList(),
    @SerializedName("last_chapter")
    val lastChapter: Chapter,
    val rating: Double = 0.0,
    val status: String = "",
    val type: String = "",
    val nsfw: Boolean = false,
    val isFavorite: Boolean = false,
) : Parcelable
