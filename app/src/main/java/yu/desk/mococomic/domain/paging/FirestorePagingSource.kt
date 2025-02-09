package yu.desk.mococomic.domain.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import javax.security.auth.login.LoginException

class FirestorePagingSource<T : Any>(
	private val query: Query,
	private val mapper: (DocumentSnapshot) -> T,
) : PagingSource<QuerySnapshot, T>() {
	override fun getRefreshKey(state: PagingState<QuerySnapshot, T>): QuerySnapshot? = null

	override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, T> =
		try {
			if (Firebase.auth.currentUser != null) {
				val currentPage = params.key ?: query.get().await()
				if (!currentPage.isEmpty) {
					val lastVisible = currentPage.documents[currentPage.size() - 1]
					val nextPage = query.startAfter(lastVisible).get().await()
					LoadResult.Page(
						data = currentPage.map(mapper),
						prevKey = null,
						nextKey = nextPage,
					)
				} else {
					LoadResult.Page(
						data = emptyList(),
						prevKey = null,
						nextKey = null,
					)
				}
			} else {
				LoadResult.Error(LoginException("Please login to continue"))
			}
		} catch (e: Exception) {
			e.printStackTrace()
			LoadResult.Error(e)
		}
}