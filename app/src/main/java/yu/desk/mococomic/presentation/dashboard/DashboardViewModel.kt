package yu.desk.mococomic.presentation.dashboard

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import yu.desk.mococomic.domain.model.Comic
import yu.desk.mococomic.presentation.dashboard.explore.ExploreFragment
import yu.desk.mococomic.presentation.dashboard.favorite.FavoriteFragment
import yu.desk.mococomic.presentation.dashboard.home.HomeFragment
import yu.desk.mococomic.presentation.dashboard.profile.ProfileFragment
import yu.desk.mococomic.utils.FilterGenre
import yu.desk.mococomic.utils.FilterOrder
import yu.desk.mococomic.utils.FilterStatus
import yu.desk.mococomic.utils.FilterType
import yu.desk.mococomic.utils.MyConstants
import yu.desk.mococomic.utils.UIState
import kotlin.random.Random

class DashboardViewModel : ViewModel() {
    private val sampleListComic = Gson().fromJson(MyConstants.LIST_COMIC, Array<Comic>::class.java).toList()
    var filterStatus = FilterStatus.All
    var filterType = FilterType.All
    var filterOrder = FilterOrder.LastUpdate
    var filterGenres = emptyList<FilterGenre>()
    var activeFragment: Fragment? = null
    private val _exploreResponse = MutableStateFlow<UIState<List<Comic>>>(UIState.Empty())
    val exploreResponse = _exploreResponse.asStateFlow()
    private val _homeResponse = MutableStateFlow<UIState<List<Comic>>>(UIState.Empty())
    val homeResponse = _homeResponse.asStateFlow()

    fun getHome(){
        viewModelScope.launch {
            _homeResponse.value = UIState.Loading()
            delay(3000)
            if(Random.nextBoolean()){
                _homeResponse.value = UIState.Success(sampleListComic)
            } else {
                _homeResponse.value = UIState.Error(0,"Error")
            }
        }
    }

    fun getExplore() {
        viewModelScope.launch {
            _exploreResponse.value = UIState.Loading()
            delay(3000)
            if(Random.nextBoolean()){
                _exploreResponse.value = UIState.Success(sampleListComic)
            } else {
                _exploreResponse.value = UIState.Error(0,"Error")
            }
        }

    }
}
