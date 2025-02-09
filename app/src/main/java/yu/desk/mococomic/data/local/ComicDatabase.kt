package yu.desk.mococomic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import yu.desk.mococomic.data.local.entity.BlockedEntity
import yu.desk.mococomic.data.local.entity.ChapterHistoryEntity
import yu.desk.mococomic.data.local.entity.FavoriteEntity

@Database(
	entities = [FavoriteEntity::class, BlockedEntity::class, ChapterHistoryEntity::class],
	version = 1,
	exportSchema = false,
)
abstract class ComicDatabase : RoomDatabase() {
	abstract fun comicDao(): ComicDao
}