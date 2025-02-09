package yu.desk.mococomic.domain.usecase

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.ComicDetail
import yu.desk.mococomic.domain.model.FirebaseChapter
import yu.desk.mococomic.domain.model.Summary
import yu.desk.mococomic.domain.repository.ComicRepository
import yu.desk.mococomic.utils.*

interface ComicUseCase {
	suspend fun syncBlockedComicFirebase()

	suspend fun syncFavoriteComicFirebase()

	suspend fun syncChapterHistoryFirebase()

	suspend fun getBlockedComic(): List<String>

	fun clearDb(): Flow<UIState<Boolean>>

	fun getSummary(): Flow<UIState<Summary>>

	fun getComicDetail(comic: Comic): Flow<UIState<ComicDetail>>

	fun getChapterDetail(slug: String): Flow<UIState<List<String>>>

	fun getFavoriteComicPagingData(): Flow<PagingData<Comic>>

	fun getChapterHistoryPagingData(): Flow<PagingData<FirebaseChapter>>

	fun getFilterComic(
		status: FilterStatus,
		type: FilterType,
		order: FilterOrder,
		genres: List<FilterGenre>,
		page: Int,
	): Flow<UIState<List<Comic>>>

	fun getSearchComic(
		query: String,
		page: Int,
	): Flow<UIState<List<Comic>>>

	fun saveUserFavoriteComic(comic: Comic): Flow<UIState<Comic>>

	fun saveUserBlockedComic(comic: Comic): Flow<UIState<Comic>>

	fun saveChapterHistory(firebaseChapter: FirebaseChapter): Flow<UIState<Boolean>>

	fun deleteUserFavoriteComic(comic: Comic): Flow<UIState<Comic>>

	fun setUserFavoriteComic(
		comic: Comic,
		isDelete: Boolean = false,
	): Flow<UIState<Pair<String, Comic>>>
}

class ComicUseCaseImpl(
	private val repository: ComicRepository,
) : ComicUseCase {
	override suspend fun syncBlockedComicFirebase() = repository.syncBlockedComicFirebase()

	override suspend fun syncFavoriteComicFirebase() = repository.syncFavoriteComicFirebase()

	override suspend fun syncChapterHistoryFirebase() = repository.syncChapterHistoryFirebase()

	override suspend fun getBlockedComic(): List<String> = repository.getBlockedComic()

	override fun clearDb(): Flow<UIState<Boolean>> =
		flow {
			try {
				repository.deleteFavoriteDb()
				repository.deleteBlockedDb()
				repository.deleteChapterHistoryDb()
				emit(UIState.Success(true))
			} catch (e: Exception) {
				e.printStackTrace()
				emit(UIState.Error(message = e.message ?: "Something went wrong"))
			}
		}

	override fun getSummary(): Flow<UIState<Summary>> = repository.getSummary().map { it.toUiState() }

	override fun getComicDetail(comic: Comic): Flow<UIState<ComicDetail>> =
		repository.getComicDetail(comic = comic).map {
			it.toUiState()
		}

	override fun getChapterDetail(slug: String): Flow<UIState<List<String>>> =
		repository.getChapterDetail(slug = slug).map {
			it.toUiState()
		}

	override fun getFavoriteComicPagingData(): Flow<PagingData<Comic>> = repository.getFavoriteComicPagingData()

	override fun getChapterHistoryPagingData(): Flow<PagingData<FirebaseChapter>> = repository.getChapterHistoryPagingData()

	override fun getFilterComic(
		status: FilterStatus,
		type: FilterType,
		order: FilterOrder,
		genres: List<FilterGenre>,
		page: Int,
	): Flow<UIState<List<Comic>>> = repository.getFilterComic(status, type, order, genres, page).map { it.toUiState() }

	override fun getSearchComic(
		query: String,
		page: Int,
	): Flow<UIState<List<Comic>>> = repository.getSearchComic(query, page).map { it.toUiState() }

	override fun saveUserFavoriteComic(comic: Comic): Flow<UIState<Comic>> =
		repository.saveUserFavoriteComic(comic).map {
			it.toUiState()
		}

	override fun saveUserBlockedComic(comic: Comic): Flow<UIState<Comic>> =
		repository.saveUserBlockedComic(comic).map {
			it.toUiState()
		}

	override fun saveChapterHistory(firebaseChapter: FirebaseChapter): Flow<UIState<Boolean>> =
		repository.saveChapterHistory(firebaseChapter).map {
			it.toUiState()
		}

	override fun deleteUserFavoriteComic(comic: Comic): Flow<UIState<Comic>> =
		repository.deleteUserFavoriteComic(comic).map {
			it.toUiState()
		}

	override fun setUserFavoriteComic(
		comic: Comic,
		isDelete: Boolean,
	): Flow<UIState<Pair<String, Comic>>> =
		if (isDelete) {
			repository.deleteUserFavoriteComic(comic).map { it.toUiState().mapData { Pair("delete", comic) } }
		} else {
			repository.saveUserFavoriteComic(comic).map { it.toUiState().mapData { Pair("save", comic) } }
		}
}