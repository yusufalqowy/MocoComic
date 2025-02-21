package yu.desk.mococomic.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import yu.desk.mococomic.data.local.entity.BlockedEntity
import yu.desk.mococomic.data.local.entity.ChapterHistoryEntity
import yu.desk.mococomic.data.local.entity.FavoriteEntity

@Dao
interface ComicDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertFavoriteComic(comic: FavoriteEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertBlockedComic(comic: BlockedEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertFavoriteComic(list: List<FavoriteEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertBlockedComic(list: List<BlockedEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertChapterHistory(chapter: ChapterHistoryEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertChapterHistory(list: List<ChapterHistoryEntity>)

	@Delete
	suspend fun deleteFavoriteComic(comic: FavoriteEntity)

	@Delete
	suspend fun deleteChapterHistory(chapter: ChapterHistoryEntity)

	@Delete
	suspend fun deleteBlockedComic(comic: BlockedEntity)

	@Query("SELECT * FROM favoriteentity")
	suspend fun getFavoriteComics(): List<FavoriteEntity>

	@Query("SELECT * FROM blockedentity")
	suspend fun getBlockedComics(): List<BlockedEntity>

	@Query("SELECT * FROM chapterhistoryentity")
	suspend fun getChapterHistory(): List<ChapterHistoryEntity>

	@Query("SELECT * FROM chapterhistoryentity WHERE comicSlug = :comicSlug")
	suspend fun getChapterHistoryByComic(comicSlug: String): List<ChapterHistoryEntity>

	@Query("SELECT 1 FROM favoriteentity WHERE slug = :slug")
	suspend fun isComicFavorite(slug: String): Boolean

	@Query("SELECT 1 FROM blockedentity WHERE slug = :slug")
	suspend fun isComicBlocked(slug: String): Boolean

	@Query("SELECT 1 FROM chapterhistoryentity WHERE slug = :slug")
	suspend fun isChapterAlreadyRead(slug: String): Boolean

	@Query("DELETE FROM favoriteentity")
	suspend fun deleteFavoriteComic()

	@Query("DELETE FROM blockedentity")
	suspend fun deleteBlockedComic()

	@Query("DELETE FROM chapterhistoryentity")
	suspend fun deleteChapterHistory()

	@Transaction
	suspend fun initFavoriteComic(list: List<FavoriteEntity>) {
		deleteFavoriteComic()
		insertFavoriteComic(list)
	}

	@Transaction
	suspend fun initBlockedComic(list: List<BlockedEntity>) {
		deleteBlockedComic()
		insertBlockedComic(list)
	}

	@Transaction
	suspend fun initChapterHistory(list: List<ChapterHistoryEntity>) {
		deleteChapterHistory()
		insertChapterHistory(list)
	}
}