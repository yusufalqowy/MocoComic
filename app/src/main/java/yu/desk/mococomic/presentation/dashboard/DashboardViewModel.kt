package yu.desk.mococomic.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.Summary
import yu.desk.mococomic.domain.usecase.ComicUseCase
import yu.desk.mococomic.utils.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel
	@Inject
	constructor(
		private val comicUseCase: ComicUseCase,
	) : ViewModel() {
		var filterStatus = FilterStatus.All
		var filterType = FilterType.All
		var filterOrder = FilterOrder.LastUpdate
		var filterGenres = emptyList<FilterGenre>()
		var canLoadMore = false
		val currentUser = Firebase.auth.currentUser
		private var page = 1
		private var listExploreComic = mutableListOf<Comic>()
		private val _homeResponse = MutableStateFlow<UIState<Summary>>(UIState.Empty())
		val homeResponse = _homeResponse.asStateFlow()
		private val _exploreResponse = MutableStateFlow<UIState<List<Comic>>>(UIState.Empty())
		val exploreResponse = _exploreResponse.asStateFlow()
		private val _deleteUserFavoriteResponse = MutableSharedFlow<UIState<Comic>>()
		val deleteUserFavorite = _deleteUserFavoriteResponse.asSharedFlow()
		private val _blockedComicResponse = MutableSharedFlow<UIState<Comic>>()
		val blockedComicResponse = _blockedComicResponse.asSharedFlow()
		private val _clearDbResponse = MutableSharedFlow<UIState<Boolean>>()
		val clearDbResponse = _clearDbResponse.asSharedFlow()

		val favoriteComicPagingData = comicUseCase.getFavoriteComicPagingData().cachedIn(viewModelScope)

		val chapterHistoryPagingData = comicUseCase.getChapterHistoryPagingData().cachedIn(viewModelScope)

		fun syncFavoriteComicFirebase() {
			viewModelScope.launch {
				comicUseCase.syncFavoriteComicFirebase()
			}
		}

		fun syncBlockedComicFirebase() {
			viewModelScope.launch {
				comicUseCase.syncBlockedComicFirebase()
			}
		}

		fun syncChapterHistoryFirebase() {
			viewModelScope.launch {
				comicUseCase.syncChapterHistoryFirebase()
			}
		}

		fun getHome() {
			viewModelScope.launch {
				_homeResponse.value = UIState.Loading()
				comicUseCase.getSummary().collectLatest {
					_homeResponse.value = it
				}
			}
		}

		fun loadMoreExplore() {
			viewModelScope.launch {
				page += 1
				val blockedComic = comicUseCase.getBlockedComic()
				comicUseCase
					.getFilterComic(
						status = filterStatus,
						type = filterType,
						order = filterOrder,
						genres = filterGenres,
						page = page,
					).collectLatest {
						_exploreResponse.value =
							when (it) {
								is UIState.Success -> {
									val comicList = it.data.toMutableList()
									if (filterStatus == FilterStatus.Favorite) {
										comicList.removeIf { item -> !item.isFavorite }
									}
									comicList.removeIf { comic -> blockedComic.contains(comic.slug) }
									listExploreComic.removeIf { list -> list.paging != null }
									listExploreComic.addAll(comicList)
									listExploreComic = listExploreComic.distinct().toMutableList()
									canLoadMore = it.data.isNotEmpty() && it.data.size >= 50
									if (canLoadMore) {
										listExploreComic.add(Comic(paging = PagingType.LoadMore))
									}
									UIState.Success(listExploreComic)
								}

								is UIState.Error -> {
									page -= 1
									listExploreComic.removeIf { list -> list.paging != null }
									listExploreComic.add(Comic(paging = PagingType.Error))
									UIState.Success(listExploreComic)
								}

								else -> it
							}
					}
			}
		}

		fun getExplore() {
			viewModelScope.launch {
				listExploreComic.clear()
				canLoadMore = false
				page = 1
				_exploreResponse.value = UIState.Loading()
				val blockedComic = comicUseCase.getBlockedComic()
				comicUseCase
					.getFilterComic(
						status = filterStatus,
						type = filterType,
						order = filterOrder,
						genres = filterGenres,
						page = page,
					).collectLatest {
						_exploreResponse.value =
							when (it) {
								is UIState.Success -> {
									val comicList = it.data.toMutableList()
									if (filterStatus == FilterStatus.Favorite) {
										comicList.removeIf { item -> !item.isFavorite }
									}
									comicList.removeIf { item -> blockedComic.contains(item.slug) }
									listExploreComic.addAll(comicList)
									canLoadMore = it.data.isNotEmpty() && it.data.size >= 50
									if (canLoadMore) {
										listExploreComic.removeIf { list -> list.paging != null }
										listExploreComic.add(Comic(paging = PagingType.LoadMore))
										UIState.Success(listExploreComic)
									} else {
										it
									}
								}

								else -> it
							}
					}
			}
		}

		fun blockedComic(comic: Comic) {
			viewModelScope.launch {
				_blockedComicResponse.emit(UIState.Loading())
				comicUseCase.saveUserBlockedComic(comic).collectLatest {
					_blockedComicResponse.emit(it)
				}
			}
		}

		fun deleteUserFavoriteComic(comic: Comic) {
			viewModelScope.launch {
				_deleteUserFavoriteResponse.emit(UIState.Loading())
				comicUseCase.deleteUserFavoriteComic(comic).collectLatest {
					_deleteUserFavoriteResponse.emit(it)
				}
			}
		}

		fun logout() {
			viewModelScope.launch {
				_clearDbResponse.emit(UIState.Loading())
				comicUseCase.clearDb().collectLatest {
					_clearDbResponse.emit(it)
				}
			}
		}
	}