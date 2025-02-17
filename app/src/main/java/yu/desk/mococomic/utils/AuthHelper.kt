package yu.desk.mococomic.utils

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import coil3.imageLoader
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import yu.desk.mococomic.R
import yu.desk.mococomic.presentation.component.AppThemeBottomSheet
import yu.desk.mococomic.presentation.component.ColorSchemeBottomSheet
import java.security.MessageDigest
import java.util.*

object AuthHelper {
	fun isLogin(context: Context): Boolean = context.getIsLogin()

	suspend fun signInWithGoogle(
		context: Context,
		onSuccess: () -> Unit,
		onError: (Exception) -> Unit,
	) {
		try {
			val credentialManager = CredentialManager.create(context)
			val nonce =
				UUID.randomUUID().toString().toByteArray().let { byte ->
					val md = MessageDigest.getInstance("SHA-256")
					md.digest(byte).fold("") { str, it -> str + "%02x".format(it) }
				}

			val googleIdOption: GetGoogleIdOption =
				GetGoogleIdOption
					.Builder()
					.setFilterByAuthorizedAccounts(false)
					.setServerClientId(context.getString(R.string.default_web_client_id))
					.setAutoSelectEnabled(false)
					.setNonce(nonce)
					.build()

			val request =
				GetCredentialRequest
					.Builder()
					.addCredentialOption(googleIdOption)
					.build()

			val result = credentialManager.getCredential(context, request)
			val idToken = GoogleIdTokenCredential.createFrom(result.credential.data).idToken
			val googleCredentials = GoogleAuthProvider.getCredential(idToken, null)
			Firebase.auth.signInWithCredential(googleCredentials).addOnCompleteListener {
				if (it.isSuccessful) {
					if (it.result.user != null) {
						Firebase.auth.updateCurrentUser(it.result.user!!)
						context.setIsLogin(true)
						onSuccess()
					}
				} else {
					onError(it.exception!!)
				}
			}
		} catch (e: Exception) {
			onError(e)
		}
	}

	suspend fun signOut(
		context: Context,
		onError: ((Exception) -> Unit)? = null,
		onSuccess: (() -> Unit)? = null,
	) {
		try {
			val credentialManager = CredentialManager.create(context)
			val request = ClearCredentialStateRequest()
			Firebase.auth.signOut()
			credentialManager.clearCredentialState(request)
			context.setIsLogin(false)
			context.setIsGuest(false)
			context.setEnableBiometric(false)
			context.setEnableBlockedComic(true)
			context.setColorScheme(ColorSchemeBottomSheet.ColorScheme.DYNAMIC)
			context.setAppTheme(AppThemeBottomSheet.AppTheme.SYSTEM)
			context.imageLoader.apply {
				memoryCache?.clear()
				diskCache?.clear()
				context.cacheDir.deleteRecursively()
			}
			onSuccess?.invoke()
		} catch (e: Exception) {
			e.printStackTrace()
			onError?.invoke(e)
		}
	}

	suspend fun signInAsGuest(
		context: Context,
		onError: ((Exception) -> Unit)? = null,
		onSuccess: (() -> Unit)? = null,
	) {
		// SignOut login user if any and set login as guest
		signOut(context, onError) {
			context.setIsLogin(true)
			context.setIsGuest(true)
			onSuccess?.invoke()
		}
	}

	fun getUser() = Firebase.auth.currentUser

	fun isUserLogin() = Firebase.auth.currentUser != null
}