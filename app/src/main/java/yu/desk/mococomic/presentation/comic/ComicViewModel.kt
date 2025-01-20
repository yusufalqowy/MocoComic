package yu.desk.mococomic.presentation.comic

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import yu.desk.mococomic.domain.model.Chapter
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.domain.model.ComicDetail
import yu.desk.mococomic.utils.MyConstants
import yu.desk.mococomic.utils.UIState
import kotlin.random.Random

class ComicViewModel : ViewModel() {
    var comic: Comic? = null
    var chapter: Chapter? = null
    private var comicDetail = Gson().fromJson(MyConstants.COMIC_DETAIL, ComicDetail::class.java)
    private var _comicDetailResponse = MutableStateFlow<UIState<ComicDetail>>(UIState.Empty())
    val comicDetailResponse = _comicDetailResponse.asStateFlow()
    private var _chapterDetailResponse = MutableStateFlow<UIState<Any>>(UIState.Empty())
    val chapterDetailResponse = _chapterDetailResponse.asStateFlow()

    fun setCurrentComic(comic: Comic) {
        this.comic = comic
    }

    fun setCurrentChapter(chapter: Chapter) {
        this.chapter = chapter
    }

    fun getChapterList() = comicDetail.chapters
    fun getComicDetail() {
        Log.e("TAG", "getComicDetail: $comic")
        viewModelScope.launch {
            _comicDetailResponse.value = UIState.Loading()
            delay(3000)
            if (Random.nextBoolean()) {
                _comicDetailResponse.value = UIState.Success(comicDetail)
            } else {
                _comicDetailResponse.value = UIState.Error(message = "Something went wrong!")
            }
        }
    }

    fun getChapterDetail() {
        viewModelScope.launch {
            _chapterDetailResponse.value = UIState.Success("")

            /*_chapterDetailResponse.value = UIState.Loading()
            delay(5000)
            if(Random.nextBoolean()){
                _chapterDetailResponse.value = UIState.Success("")
            }else{
                _chapterDetailResponse.value = UIState.Error(message = "Something went wrong!")
            }*/
        }
    }
}
