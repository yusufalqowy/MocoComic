package yu.desk.mococomic.domain.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Chapter(
    val chapter: String = "",
    val slug: String = "",
    @SerializedName("tanggal")
    val releaseDate: String = "",
    val isAlreadyRead: Boolean = false,
) : Parcelable