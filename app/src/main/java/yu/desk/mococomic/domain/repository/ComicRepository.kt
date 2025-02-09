package yu.desk.mococomic.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.ComicDetail
import yu.desk.mococomic.domain.model.FirebaseChapter
import yu.desk.mococomic.domain.model.Summary
import yu.desk.mococomic.utils.ApiState
import yu.desk.mococomic.utils.FilterGenre
import yu.desk.mococomic.utils.FilterOrder
import yu.desk.mococomic.utils.FilterStatus
import yu.desk.mococomic.utils.FilterType

interface ComicRepository {
	fun getSummary(): Flow<ApiState<Summary>>

	fun getComicDetail(comic: Comic): Flow<ApiState<ComicDetail>>

	fun getChapterDetail(slug: String): Flow<ApiState<List<String>>>

	fun getChapterHistoryPagingData(): Flow<PagingData<FirebaseChapter>>

	fun getFavoriteComicPagingData(): Flow<PagingData<Comic>>

	fun getFilterComic(
		status: FilterStatus,
		type: FilterType,
		order: FilterOrder,
		genres: List<FilterGenre>,
		page: Int,
	): Flow<ApiState<List<Comic>>>

	fun getSearchComic(
		query: String,
		page: Int,
	): Flow<ApiState<List<Comic>>>

	fun saveUserFavoriteComic(comic: Comic): Flow<ApiState<Comic>>

	fun saveUserBlockedComic(comic: Comic): Flow<ApiState<Comic>>

	fun saveChapterHistory(firebaseChapter: FirebaseChapter): Flow<ApiState<Boolean>>

	fun deleteUserFavoriteComic(comic: Comic): Flow<ApiState<Comic>>

	suspend fun deleteFavoriteDb()

	suspend fun deleteChapterHistoryDb()

	suspend fun deleteBlockedDb()

	suspend fun syncBlockedComicFirebase()

	suspend fun syncFavoriteComicFirebase()

	suspend fun syncChapterHistoryFirebase()

	suspend fun getBlockedComic(): List<String>
}