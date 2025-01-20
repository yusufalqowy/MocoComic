package yu.desk.mococomic.utils

import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

sealed class ApiState<T> {
    class Success<T>(
        val data: T,
        val fromDb: Boolean = false,
    ) : ApiState<T>()

    class Failure<T>(
        val code: Int,
        val message: String,
    ) : ApiState<T>()

    class Error<T>(
        val message: String,
    ) : ApiState<T>()
}

suspend fun <T> handleResponse(
    apiCall: suspend () -> Response<T>,
    onError: suspend (code: Int?, message: String) -> Unit,
    onSuccess: suspend (T) -> Unit,
) {
    try {
        val response = apiCall.invoke()
        if (response.isSuccessful && response.body() != null) {
            onSuccess.invoke(response.body()!!)
        } else {
            onError.invoke(null, "Data is empty")
        }
    } catch (e: HttpException) {
        onError.invoke(e.code(), e.message())
    } catch (e: IOException) {
        onError.invoke(null, "No internet connection")
    } catch (e: Exception) {
        e.printStackTrace()
        if (e is CancellationException) throw e
        onError.invoke(null, "Something went wrong")
    }
}