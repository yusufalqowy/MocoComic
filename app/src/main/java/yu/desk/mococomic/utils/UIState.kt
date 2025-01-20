package yu.desk.mococomic.utils

sealed class UIState<T> {
    class Success<T>(val data: T) : UIState<T>()

    class Error<T>(val code: Int? = null, val message: String) : UIState<T>()

    class Loading<T> : UIState<T>()

    class Empty<T> : UIState<T>()
}

fun <T> ApiState<T>.toUiState(): UIState<T> =
    when (this) {
        is ApiState.Error -> UIState.Error(message = message)
        is ApiState.Success -> UIState.Success(data = data)
        is ApiState.Failure -> UIState.Error(code = code, message = message)
    }

fun <T> apiResponseHandler(
    uiState: UIState<T>,
    onLoading: () -> Unit = {},
    onSuccess: (data: T) -> Unit,
    onError: (message: String?) -> Unit = {},
) {
    when (uiState) {
        is UIState.Empty -> Unit
        is UIState.Error -> onError.invoke(uiState.message)
        is UIState.Loading -> onLoading.invoke()
        is UIState.Success -> uiState.data?.let { onSuccess(it) }
    }
}
