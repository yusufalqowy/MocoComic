package yu.desk.mococomic.data.repository

import androidx.paging.*
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import yu.desk.mococomic.data.local.ComicDao
import yu.desk.mococomic.data.local.entity.BlockedEntity
import yu.desk.mococomic.data.local.entity.ChapterHistoryEntity
import yu.desk.mococomic.data.local.entity.FavoriteEntity
import yu.desk.mococomic.data.remote.ComicApi
import yu.desk.mococomic.domain.model.*
import yu.desk.mococomic.domain.paging.FirestorePagingSource
import yu.desk.mococomic.domain.repository.ComicRepository
import yu.desk.mococomic.utils.*
import yu.desk.mococomic.utils.AuthHelper
import javax.security.auth.login.LoginException

class ComicRepositoryImpl(
	private val comicApi: ComicApi,
	private val comicDao: ComicDao,
	private val firestore: FirebaseFirestore,
) : ComicRepository {
	override fun getSummary(): Flow<ApiState<Summary>> =
		flow {
			handleResponse(
				apiCall = { comicApi.getSummary() },
				onError = { code, message ->
					if (code != null) {
						emit(ApiState.Failure(code = code, message = message))
					} else {
						emit(ApiState.Error(message = message))
					}
				},
				onSuccess = { data ->
					val blockedComic = getBlockedComic()
					val newChapterComic =
						data.newChapter?.toMutableList()?.also {
							it.removeIf { c -> blockedComic.contains(c.slug) }
						}
					val newManga =
						data.newManga?.toMutableList()?.also {
							it.removeIf { c -> blockedComic.contains(c.slug) }
						}
					val popular =
						data.popular?.toMutableList()?.also {
							it.removeIf { c -> blockedComic.contains(c.slug) }
						}
					val trending =
						data.trending?.toMutableList()?.also {
							it.removeIf { c -> blockedComic.contains(c.slug) }
						}
					val newData =
						data.copy(
							newChapter = newChapterComic,
							newManga = newManga,
							popular = popular,
							trending = trending,
						)

					// Because api response newChapter always empty. I get new chapter from here just for perfection :)
					if (data.newChapter.isNullOrEmpty()) {
						handleResponse(
							apiCall = {
								comicApi.getFilterComic(
									FilterStatus.All.path,
									FilterType.All.path,
									FilterOrder.LastUpdate.path,
									"",
									page = 1,
								)
							},
							onError = { _, _ ->
								val newChapter =
									newManga?.shuffled()?.plus(popular?.shuffled() ?: emptyList())
										?: emptyList()
								emit(
									ApiState.Success(
										data = newData.toSummary().copy(newChapter = newChapter.map { it.toComic() }),
									),
								)
							},
							onSuccess = {
								val newChapter =
									it.mangas?.toMutableList()?.also { l ->
										l.removeIf { c -> blockedComic.contains(c.slug) }
									}
								emit(
									ApiState.Success(
										data =
											newData.toSummary().copy(
												newChapter = newChapter?.take(12)?.map { map -> map.toComic() } ?: emptyList(),
											),
									),
								)
							},
						)
					} else {
						emit(ApiState.Success(data = newData.toSummary()))
					}
				},
			)
		}

	override fun getComicDetail(comic: Comic): Flow<ApiState<ComicDetail>> =
		flow {
			handleResponse(
				apiCall = { comicApi.getComicDetail(comic.slug.toString()) },
				onError = { code, message ->
					if (code != null) {
						emit(ApiState.Failure(code = code, message = message))
					} else {
						emit(ApiState.Error(message = message))
					}
				},
				onSuccess = {
					val chapterHistory = getChapterHistoryByComic(comic.slug)
					val isFavorite = if (AuthHelper.isUserLogin()) comicDao.isComicFavorite(comic.slug) else false
					val newChapter =
						it.chapters?.map { ch -> ch.copy(isAlreadyRead = chapterHistory.contains(ch.slug)).toChapter() }
							?: emptyList()
					emit(
						ApiState.Success(
							data =
								it.toComicDetail().copy(
									comic = comic.copy(isFavorite = isFavorite, cover = it.cover ?: comic.cover),
									chapters = newChapter,
								),
						),
					)
				},
			)
		}

	override fun getChapterDetail(slug: String): Flow<ApiState<List<String>>> =
		flow {
			handleResponse(
				apiCall = { comicApi.getChapterDetail(slug) },
				onError = { code, message ->
					if (code != null) {
						emit(ApiState.Failure(code = code, message = message))
					} else {
						emit(ApiState.Error(message = message))
					}
				},
				onSuccess = {
					emit(ApiState.Success(data = it.content ?: emptyList()))
				},
			)
		}

	override fun getFilterComic(
		status: FilterStatus,
		type: FilterType,
		order: FilterOrder,
		genres: List<FilterGenre>,
		page: Int,
	): Flow<ApiState<List<Comic>>> =
		flow {
			handleResponse(
				apiCall = {
					comicApi.getFilterComic(
						status.path,
						type.path,
						order.path,
						genres.joinToString(",") { it.path },
						page = page,
					)
				},
				onError = { code, message ->
					if (code != null) {
						emit(ApiState.Failure(code = code, message = message))
					} else {
						emit(ApiState.Error(message = message))
					}
				},
				onSuccess = { res ->
					val favoriteComic = getFavoriteComic()
					val data =
						res.mangas?.toMutableList()?.run {
							map { it.toComic().copy(isFavorite = favoriteComic.contains(it.slug)) }
						} ?: emptyList()
					emit(ApiState.Success(data = data))
				},
			)
		}

	override fun getSearchComic(
		query: String,
		page: Int,
	): Flow<ApiState<List<Comic>>> =
		flow {
			handleResponse(
				apiCall = { comicApi.getSearchComic(query = query, page = page) },
				onError = { code, message ->
					if (code != null) {
						emit(ApiState.Failure(code = code, message = message))
					} else {
						emit(ApiState.Error(message = message))
					}
				},
				onSuccess = { res ->
					val favoriteComic = getFavoriteComic()
					val data =
						res.mangas?.toMutableList()?.run {
							map { it.toComic().copy(isFavorite = favoriteComic.contains(it.slug)) }
						} ?: emptyList()
					emit(ApiState.Success(data = data))
				},
			)
		}

	override fun saveUserFavoriteComic(comic: Comic): Flow<ApiState<Comic>> =
		flow {
			val currentUser = AuthHelper.getUser() ?: return@flow emit(ApiState.Failure(401, "Please login to continue"))
			try {
				firestore
					.collection("user_data")
					.document(currentUser.uid)
					.collection("favorites")
					.document(comic.slug)
					.set(comic.toFirebaseComic().toMap())
					.await()
				comicDao.insertFavoriteComic(FavoriteEntity(comic.slug))
				emit(ApiState.Success(comic))
			} catch (e: Exception) {
				e.printStackTrace()
				emit(ApiState.Error(e.message ?: "Something went wrong"))
			}
		}

	override fun saveUserBlockedComic(comic: Comic): Flow<ApiState<Comic>> =
		flow {
			val currentUser = AuthHelper.getUser() ?: return@flow emit(ApiState.Failure(401, "Please login to continue"))
			try {
				firestore
					.collection("user_data")
					.document(currentUser.uid)
					.collection("blocked_comics")
					.document(comic.slug)
					.set(comic.toFirebaseComic().toMap())
					.await()
				comicDao.insertBlockedComic(BlockedEntity(comic.slug))
				emit(ApiState.Success(comic))
			} catch (e: Exception) {
				e.printStackTrace()
				emit(ApiState.Error(e.message ?: "Something went wrong"))
			}
		}

	override fun saveChapterHistory(firebaseChapter: FirebaseChapter): Flow<ApiState<Boolean>> =
		flow {
			val currentUser =
				AuthHelper.getUser() ?: return@flow emit(ApiState.Failure(401, "Please login to continue"))
			val isAlreadyRead = comicDao.isChapterAlreadyRead(firebaseChapter.slug)
			if (!isAlreadyRead) {
				try {
					firestore
						.collection("user_data")
						.document(currentUser.uid)
						.collection("histories")
						.document(firebaseChapter.slug)
						.set(firebaseChapter.toMap())
						.await()
					comicDao.insertChapterHistory(
						ChapterHistoryEntity(
							firebaseChapter.slug,
							firebaseChapter.comic?.slug ?: "",
						),
					)
					emit(ApiState.Success(true))
				} catch (e: Exception) {
					e.printStackTrace()
					emit(ApiState.Error(e.message ?: "Something went wrong"))
				}
			}
		}

	override fun deleteUserFavoriteComic(comic: Comic): Flow<ApiState<Comic>> =
		flow {
			val currentUser =
				AuthHelper.getUser() ?: return@flow emit(ApiState.Failure(401, "Please login to continue"))
			try {
				firestore
					.collection("user_data")
					.document(currentUser.uid)
					.collection("favorites")
					.document(comic.slug)
					.delete()
					.await()
				comicDao.deleteFavoriteComic(FavoriteEntity(comic.slug))
				emit(ApiState.Success(comic))
			} catch (e: Exception) {
				e.printStackTrace()
				emit(ApiState.Error(e.message ?: "Something went wrong"))
			}
		}

	override fun deleteChapterHistory(firebaseChapter: FirebaseChapter): Flow<ApiState<FirebaseChapter>> =
		flow {
			val currentUser =
				AuthHelper.getUser() ?: return@flow emit(ApiState.Failure(401, "Please login to continue"))
			try {
				firestore
					.collection("user_data")
					.document(currentUser.uid)
					.collection("histories")
					.document(firebaseChapter.slug)
					.delete()
					.await()
				comicDao.deleteChapterHistory(ChapterHistoryEntity(slug = firebaseChapter.slug, comicSlug = firebaseChapter.comic?.slug ?: ""))
				emit(ApiState.Success(firebaseChapter))
			} catch (e: Exception) {
				e.printStackTrace()
				emit(ApiState.Error(e.message ?: "Something went wrong"))
			}
		}

	override suspend fun deleteFavoriteDb() {
		comicDao.deleteFavoriteComic()
	}

	override suspend fun deleteChapterHistoryDb() {
		comicDao.deleteChapterHistory()
	}

	override suspend fun deleteBlockedDb() {
		comicDao.deleteBlockedComic()
	}

	override suspend fun syncBlockedComicFirebase() {
		val currentUser = AuthHelper.getUser() ?: return
		val query =
			firestore
				.collection("user_data")
				.document(currentUser.uid)
				.collection("blocked_comics")
				.orderBy("created_at")
		try {
			val blockedComic = getBlockedComic()
			val count =
				query
					.count()
					.get(AggregateSource.SERVER)
					.await()
					.count
					.toInt()

			if (blockedComic.size != count) {
				val data =
					query
						.get()
						.await()
						.toHashSet()
						.map {
							val data = FirebaseComic.fromMap(it.data)
							BlockedEntity(data.slug)
						}
				comicDao.initBlockedComic(data)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override suspend fun syncFavoriteComicFirebase() {
		val currentUser = AuthHelper.getUser() ?: return
		try {
			val favoriteComic = getFavoriteComic()
			val query =
				firestore
					.collection("user_data")
					.document(currentUser.uid)
					.collection("favorites")
					.orderBy("created_at")

			val count =
				query
					.count()
					.get(AggregateSource.SERVER)
					.await()
					.count
					.toInt()

			if (favoriteComic.size != count) {
				val data =
					query
						.get()
						.await()
						.toHashSet()
						.map {
							val data = FirebaseComic.fromMap(it.data)
							FavoriteEntity(data.slug)
						}
				comicDao.initFavoriteComic(data)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override suspend fun syncChapterHistoryFirebase() {
		val currentUser = AuthHelper.getUser() ?: return
		try {
			val chapterHistory = getChapterHistory()
			val query =
				firestore
					.collection("user_data")
					.document(currentUser.uid)
					.collection("histories")
					.orderBy("created_at")
			val count =
				query
					.count()
					.get(AggregateSource.SERVER)
					.await()
					.count
					.toInt()

			if (chapterHistory.size != count) {
				val data =
					query
						.get()
						.await()
						.toHashSet()
						.map {
							val data = FirebaseChapter.fromMap(it.data)
							ChapterHistoryEntity(data.slug, data.comic?.slug ?: "")
						}
				comicDao.initChapterHistory(data)
			}
		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun getChapterHistoryPagingData(searchQuery: String?): Flow<PagingData<FirebaseChapter>> {
		val currentUser =
			AuthHelper.getUser() ?: return flow {
				emit(
					PagingData.empty(
						sourceLoadStates =
							LoadStates(
								refresh = LoadState.Error(LoginException("Please login to continue")),
								prepend = LoadState.NotLoading(true),
								append = LoadState.NotLoading(true),
							),
					),
				)
			}
		var query =
			FirebaseFirestore
				.getInstance()
				.collection("user_data")
				.document(currentUser.uid)
				.collection("histories")
				.limit(15)

		if (!searchQuery.isNullOrBlank()) {
			query =
				query.whereGreaterThanOrEqualTo("comic.title", searchQuery)
					.whereLessThan("comic.title", searchQuery + "\uf8ff")
		}

		query = query.orderBy("created_at", Query.Direction.DESCENDING)

		return Pager(
			config = PagingConfig(pageSize = 15, enablePlaceholders = true, initialLoadSize = 15),
			pagingSourceFactory = {
				FirestorePagingSource(query = query, mapper = { snapshot -> FirebaseChapter.fromMap(snapshot.data!!) })
			},
		).flow
	}

	override fun getFavoriteComicPagingData(): Flow<PagingData<Comic>> {
		val currentUser =
			AuthHelper.getUser() ?: return flow {
				emit(
					PagingData.empty(
						sourceLoadStates =
							LoadStates(
								refresh = LoadState.Error(LoginException("Please login to continue")),
								prepend = LoadState.NotLoading(true),
								append = LoadState.NotLoading(true),
							),
					),
				)
			}
		val query =
			FirebaseFirestore
				.getInstance()
				.collection("user_data")
				.document(currentUser.uid)
				.collection("favorites")
				.orderBy("created_at")
				.limit(15)

		return Pager(
			config = PagingConfig(pageSize = 15, enablePlaceholders = true, initialLoadSize = 15),
			pagingSourceFactory = {
				FirestorePagingSource(
					query = query,
					mapper = { snapshot ->
						FirebaseComic.fromMap(snapshot.data!!).toComic().copy(isFavorite = true)
					},
				)
			},
		).flow
	}

	override suspend fun getBlockedComic() = if (AuthHelper.isUserLogin()) comicDao.getBlockedComics().map { it.slug } else emptyList()

	private suspend fun getFavoriteComic() = if (AuthHelper.isUserLogin()) comicDao.getFavoriteComics().map { it.slug } else emptyList()

	private suspend fun getChapterHistory() = if (AuthHelper.isUserLogin()) comicDao.getChapterHistory().map { it.slug } else emptyList()

	private suspend fun getChapterHistoryByComic(comicSlug: String) =
		if (AuthHelper.isUserLogin()) {
			comicDao.getChapterHistoryByComic(comicSlug).map {
				it.slug
			}
		} else {
			emptyList()
		}
}