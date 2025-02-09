package yu.desk.mococomic.di

import android.content.Context
import androidx.room.Room
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import yu.desk.mococomic.data.local.ComicDao
import yu.desk.mococomic.data.local.ComicDatabase
import yu.desk.mococomic.data.remote.ComicApi
import yu.desk.mococomic.data.repository.ComicRepositoryImpl
import yu.desk.mococomic.domain.repository.ComicRepository
import yu.desk.mococomic.domain.usecase.ComicUseCase
import yu.desk.mococomic.domain.usecase.ComicUseCaseImpl
import yu.desk.mococomic.utils.MyConstants
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
	@Provides
	@Singleton
	fun provideOkHttpClient(
		@ApplicationContext context: Context,
	): OkHttpClient = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).callTimeout(60, TimeUnit.SECONDS).pingInterval(10, TimeUnit.SECONDS).addNetworkInterceptor(ChuckerInterceptor.Builder(context).build()).build()

	@Provides
	@Singleton
	fun provideComicApi(okHttpClient: OkHttpClient): ComicApi = Retrofit.Builder().baseUrl(MyConstants.BASE_URL).addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build().create(ComicApi::class.java)

	@Provides
	@Singleton
	fun provideComicDatabase(
		@ApplicationContext context: Context,
	): ComicDatabase =
		Room.databaseBuilder(
			context,
			ComicDatabase::class.java,
			"moco_comic.db",
		).fallbackToDestructiveMigration().build()

	@Provides
	@Singleton
	fun provideComicDao(comicDatabase: ComicDatabase): ComicDao = comicDatabase.comicDao()

	@Provides
	@Singleton
	fun provideComicRepository(
		comicApi: ComicApi,
		comicDao: ComicDao,
	): ComicRepository = ComicRepositoryImpl(comicApi, comicDao, FirebaseFirestore.getInstance())

	@Provides
	@Singleton
	fun provideComicUseCase(comicRepository: ComicRepository): ComicUseCase = ComicUseCaseImpl(comicRepository)
}