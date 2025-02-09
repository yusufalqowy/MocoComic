package yu.desk.mococomic.presentation.comic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.ComicDetail
import yu.desk.mococomic.domain.model.FirebaseChapter
import yu.desk.mococomic.domain.usecase.ComicUseCase
import yu.desk.mococomic.utils.UIState
import javax.inject.Inject

@HiltViewModel
class ComicViewModel
	@Inject
	constructor(
		private val comicUseCase: ComicUseCase,
	) : ViewModel() {
		var comic: Comic = Comic()
		var chapter: Chapter = Chapter()
		var chapterList: MutableStateFlow<List<Chapter>> = MutableStateFlow(emptyList())
		private val _comicDetailResponse: MutableStateFlow<UIState<ComicDetail>> = MutableStateFlow(UIState.Empty())
		val comicDetailResponse = _comicDetailResponse.asStateFlow()
		private val _chapterDetailResponse: MutableStateFlow<UIState<List<String>>> = MutableStateFlow(UIState.Empty())
		val chapterDetailResponse = _chapterDetailResponse.asStateFlow()
		private val _saveFavoriteResponse: MutableSharedFlow<UIState<Pair<String, Comic>>> = MutableSharedFlow()
		val saveFavoriteResponse = _saveFavoriteResponse.asSharedFlow()
		private val _blockedComicResponse = MutableSharedFlow<UIState<Comic>>()
		val blockedComicResponse = _blockedComicResponse.asSharedFlow()

		fun setCurrentComic(comic: Comic) {
			this.comic = comic
		}

		fun setCurrentChapter(ch: Chapter) {
			chapter = ch
		}

		fun getComicDetail() {
			viewModelScope.launch {
				if (comic.slug.isNotEmpty()) {
					_comicDetailResponse.emit(UIState.Loading())
					comicUseCase.getComicDetail(comic).collectLatest {
						_comicDetailResponse.value =
							when (it) {
								is UIState.Success -> {
									chapterList.value = it.data.chapters
									setCurrentComic(it.data.comic)
									it
								}

								else -> it
							}
					}
				} else {
					_comicDetailResponse.emit(UIState.Error(message = "Comic not found"))
				}
			}
		}

		fun getChapterDetail() {
			viewModelScope.launch {
				chapter.apply {
					if (slug.isNotEmpty()) {
						_chapterDetailResponse.emit(UIState.Loading())
						comicUseCase.getChapterDetail(slug).collectLatest {
							_chapterDetailResponse.value =
								when (it) {
									is UIState.Success -> {
										saveChapterHistory(
											FirebaseChapter(
												chapter = title,
												slug = slug,
												comic = comic.toFirebaseComic(),
											),
										)
										it
									}

									else -> it
								}
						}
					} else {
						_chapterDetailResponse.emit(UIState.Error(message = "Chapter not found"))
					}
				}
			}
		}

		fun setFavorite(item: Comic) {
			viewModelScope.launch {
				_saveFavoriteResponse.emit(UIState.Loading())
				comicUseCase.setUserFavoriteComic(item, item.isFavorite).collectLatest {
					_saveFavoriteResponse.emit(it)

					if (it is UIState.Success) {
						setCurrentComic(comic.copy(isFavorite = !item.isFavorite))
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

		private fun saveChapterHistory(ch: FirebaseChapter) {
			viewModelScope.launch {
				comicUseCase.saveChapterHistory(ch).collectLatest { state ->
					if (state is UIState.Success) {
						chapterList.value =
							chapterList.value.map {
								if (it.slug == ch.slug) {
									it.copy(isAlreadyRead = true)
								} else {
									it
								}
							}
					}
				}
			}
		}
	}