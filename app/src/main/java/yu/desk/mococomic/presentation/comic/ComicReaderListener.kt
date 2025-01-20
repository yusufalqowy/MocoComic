package yu.desk.mococomic.presentation.comic

import yu.desk.mococomic.domain.model.Chapter

interface ComicReaderListener {
    fun onDrawerClickListener()
    fun onBeforeClickListener(chapter: Chapter)
    fun onNextClickListener(chapter: Chapter)
    fun onFavoriteClickListener(chapter: Chapter)
}
