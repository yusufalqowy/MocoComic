package yu.desk.mococomic.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class CryptoManager {
	private val charset by lazy {
		charset("UTF-8")
	}

	private val keyStore =
		KeyStore.getInstance("AndroidKeyStore").apply {
			load(null)
		}

	private val encryptCipher
		get() =
			Cipher.getInstance(TRANSFORMATION).apply {
				init(Cipher.ENCRYPT_MODE, getKey("secret"))
			}

	private fun getDecryptCipherForIv(iv: ByteArray): Cipher {
		return Cipher.getInstance(TRANSFORMATION).apply {
			init(Cipher.DECRYPT_MODE, getKey("secret"), IvParameterSpec(iv))
		}
	}

	private fun getKey(keyAlias: String): SecretKey {
		val existingKey = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
		return existingKey?.secretKey ?: createKey(keyAlias)
	}

	private fun createKey(keyAlias: String): SecretKey {
		return KeyGenerator.getInstance(ALGORITHM).apply {
			init(
				KeyGenParameterSpec.Builder(
					keyAlias,
					KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
				)
					.setBlockModes(BLOCK_MODE)
					.setEncryptionPaddings(PADDING)
					.setUserAuthenticationRequired(false)
					.setRandomizedEncryptionRequired(true)
					.build(),
			)
		}.generateKey()
	}

	fun encrypt(
		data: String,
		outputStream: OutputStream,
	): ByteArray {
		val encryptedBytes = encryptCipher.doFinal(encryptCipher.iv + data.toByteArray(charset))
		outputStream.use {
			it.write(encryptCipher.iv.size)
			it.write(encryptCipher.iv)
			it.write(encryptedBytes.size)
			it.write(encryptedBytes)
		}
		return encryptedBytes
	}

	fun decrypt(inputStream: InputStream): String {
		return inputStream.use {
			val ivSize = it.read()
			val iv = ByteArray(ivSize)
			it.read(iv)

			val encryptedBytesSize = it.read()
			val encryptedBytes = ByteArray(encryptedBytesSize)
			it.read(encryptedBytes)

			val decipheredByteArray =
				getDecryptCipherForIv(iv)
					.doFinal(encryptedBytes)

			// Strip the Initialization Vector (usually first 16 bytes) to get the actual unencrypted data
			decipheredByteArray
				.toList()
				.subList(ivSize, decipheredByteArray.size) // skip the IV
				.toByteArray()
				.toString(charset)
		}
	}

	companion object {
		private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
		private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
		private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
		private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
	}
}