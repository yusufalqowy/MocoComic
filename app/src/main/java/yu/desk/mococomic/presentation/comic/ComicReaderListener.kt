package yu.desk.mococomic.presentation.comic

import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.Comic

interface ComicReaderListener {
	fun onDrawerClickListener()

	fun onBeforeClickListener(chapter: Chapter)

	fun onNextClickListener(chapter: Chapter)

	fun onFavoriteClickListener(comic: Comic)

	fun onCommentClickListener(chapter: Chapter)
}