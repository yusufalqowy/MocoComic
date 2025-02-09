package yu.desk.mococomic.presentation.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.usecase.ComicUseCase
import yu.desk.mococomic.utils.PagingType
import yu.desk.mococomic.utils.UIState
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
	@Inject
	constructor(private val comicUseCase: ComicUseCase) : ViewModel() {
		private val _searchResponse = MutableStateFlow<UIState<List<Comic>>>(UIState.Empty())
		val searchResponse = _searchResponse.asStateFlow()
		private val _blockedComicResponse = MutableSharedFlow<UIState<Comic>>()
		val blockedComicResponse = _blockedComicResponse.asSharedFlow()
		private var listComic = mutableListOf<Comic>()
		private var page = 1
		var canLoadMore = false
		var query = ""

		fun loadMoreSearchComic() {
			viewModelScope.launch {
				page += 1
				val blockedComic = comicUseCase.getBlockedComic()
				comicUseCase.getSearchComic(query, page).collectLatest {
					_searchResponse.value =
						when (it) {
							is UIState.Success -> {
								val comicList = it.data.toMutableList()
								comicList.removeIf { comic -> blockedComic.contains(comic.slug) }
								listComic.removeIf { list -> list.paging != null }
								listComic.addAll(comicList)
								listComic = listComic.distinct().toMutableList()
								canLoadMore = it.data.isNotEmpty() && it.data.size >= 10
								if (canLoadMore) {
									listComic.add(Comic(paging = PagingType.LoadMore))
								}
								UIState.Success(listComic)
							}

							is UIState.Error -> {
								page -= 1
								listComic.removeIf { list -> list.paging != null }
								listComic.add(Comic(paging = PagingType.Error))
								UIState.Success(listComic)
							}

							else -> it
						}
				}
			}
		}

		fun getSearchComic(searchQuery: String) {
			viewModelScope.launch {
				listComic.clear()
				page = 1
				canLoadMore = false
				query = searchQuery
				_searchResponse.emit(UIState.Loading())

				val blockedComic = comicUseCase.getBlockedComic()
				comicUseCase.getSearchComic(searchQuery, page).collectLatest {
					_searchResponse.value =
						when (it) {
							is UIState.Success -> {
								Log.e("scC", it.data.size.toString())
								val comicList = it.data.toMutableList()
								comicList.removeIf { item -> blockedComic.contains(item.slug) }
								listComic.addAll(comicList)
								canLoadMore = it.data.isNotEmpty() && it.data.size >= 10
								if (canLoadMore) {
									listComic.removeIf { list -> list.paging != null }
									listComic.add(Comic(paging = PagingType.LoadMore))
									UIState.Success(listComic)
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
	}