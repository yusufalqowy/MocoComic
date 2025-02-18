package yu.desk.mococomic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings.Secure
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

class DemoManager(private val context: Context) {
	companion object {
		enum class StatusDemo {
			NOT_YET,
			RUNNING,
			OVER,
		}
	}

	suspend fun startDemo(onComplete: ((status: StatusDemo) -> Unit)? = null) {
		val firestore = FirebaseFirestore.getInstance()
		val query =
			firestore
				.collection("demo-data")
				.document(getDeviceID())
		val endDemoDate = context.getEndDemo()
		try {
			if (endDemoDate != null) {
				onComplete?.invoke(checkStatus(endDemoDate))
				return
			}

			val doc = query.get().await()
			if (!doc.exists()) {
				val data = DemoData()
				query.set(data.toMap()).await()
				context.setEndDemo(data.endDate)
				onComplete?.invoke(checkStatus(data.endDate))
			} else {
				val data =
					query.get().await().let {
						it.data?.let { it1 -> DemoData.fromMap(it1) }
					}
				if (data != null) {
					context.setEndDemo(data.endDate)
					onComplete?.invoke(checkStatus(data.endDate))
				} else {
					onComplete?.invoke(StatusDemo.NOT_YET)
				}
			}
		} catch (e: Exception) {
			e.printStackTrace()
			onComplete?.invoke(StatusDemo.NOT_YET)
		}
	}

	fun checkDemoStatus(onComplete: ((status: StatusDemo) -> Unit)? = null) {
		val firestore = FirebaseFirestore.getInstance()
		val endDemoDate = context.getEndDemo()
		if (endDemoDate != null) {
			onComplete?.invoke(checkStatus(endDemoDate))
		} else {
			firestore
				.collection("demo-data")
				.document(getDeviceID())
				.get()
				.addOnCompleteListener {
					if (it.isSuccessful) {
						val data = it.result.data?.let { l -> DemoData.fromMap(l) }
						if (data != null) {
							context.setEndDemo(data.endDate)
							onComplete?.invoke(checkStatus(data.endDate))
						} else {
							onComplete?.invoke(StatusDemo.NOT_YET)
						}
					} else {
						onComplete?.invoke(StatusDemo.NOT_YET)
					}
				}
		}
	}

	private fun checkStatus(endDate: Date) =
		when {
			Date().after(endDate) -> StatusDemo.OVER
			else -> StatusDemo.RUNNING
		}

	@SuppressLint("HardwareIds")
	private fun getDeviceID(): String {
		return try {
			val androidId = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
			androidId.ifEmpty {
				UUID.randomUUID().toString()
			}
		} catch (e: Exception) {
			e.printStackTrace()
			UUID.randomUUID().toString()
		}
	}

	private data class DemoData(
		val startDate: Date = Calendar.getInstance().time,
		val endDate: Date =
			Calendar.getInstance().run {
				add(Calendar.DATE, 7)
				time
			},
	) {
		fun toMap() =
			mapOf(
				"startDate" to Timestamp(startDate),
				"endDate" to Timestamp(endDate),
			)

		companion object {
			fun fromMap(map: Map<String, Any>) =
				DemoData(
					startDate = (map["startDate"] as Timestamp?)?.toDate() ?: Date(),
					endDate = (map["endDate"] as Timestamp?)?.toDate() ?: Date(),
				)
		}
	}
}