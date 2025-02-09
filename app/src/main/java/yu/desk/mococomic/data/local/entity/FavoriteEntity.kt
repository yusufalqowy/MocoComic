package yu.desk.mococomic.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteEntity(
	@PrimaryKey
	val slug: String,
)