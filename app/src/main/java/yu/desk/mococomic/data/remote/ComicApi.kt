package yu.desk.mococomic.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import yu.desk.mococomic.data.remote.dto.ChapterDetailResponse
import yu.desk.mococomic.data.remote.dto.ComicDetailResponse
import yu.desk.mococomic.data.remote.dto.SearchResponse
import yu.desk.mococomic.data.remote.dto.SummaryResponse

interface ComicApi {
	@GET("v6/summary")
	suspend fun getSummary(): Response<SummaryResponse>

	@GET("v6/manga")
	suspend fun getComicDetail(
		@Query("id") slug: String,
	): Response<ComicDetailResponse>

	@GET("v6/chapter")
	suspend fun getChapterDetail(
		@Query("id") slug: String,
	): Response<ChapterDetailResponse>

	@GET("v6/search")
	suspend fun getFilterComic(
		@Query("status") status: String,
		@Query("type") type: String,
		@Query("order") order: String,
		@Query("genres") genres: String,
		@Query("page") page: Int = 1,
	): Response<SearchResponse>

	@GET("v6/search/simple")
	suspend fun getSearchComic(
		@Query("s") query: String,
		@Query("page") page: Int = 1,
	): Response<SearchResponse>
}