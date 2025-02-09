package yu.desk.mococomic.presentation.component

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class SwipeRightDetector(context: Context, private val listener: OnSwipeRightListener) {
	interface OnSwipeRightListener {
		fun onSwipeRight()
	}

	private val gestureDetector =
		GestureDetector(
			context,
			object : GestureDetector.SimpleOnGestureListener() {
				private val SWIPE_THRESHOLD = 400
				private val SWIPE_VELOCITY_THRESHOLD = 400

				override fun onFling(
					e1: MotionEvent?,
					e2: MotionEvent,
					velocityX: Float,
					velocityY: Float,
				): Boolean {
					val result = false
					try {
						val diffY = e2.y - (e1?.y ?: 0f)
						val diffX = e2.x - (e1?.x ?: 0f)
						if (abs(diffX) > abs(diffY)) {
							if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
								if (diffX > 0) {
									listener.onSwipeRight()
								}
							}
						}
					} catch (exception: Exception) {
						exception.printStackTrace()
					}
					return result
				}
			},
		)

	fun onTouchEvent(event: MotionEvent): Boolean {
		return gestureDetector.onTouchEvent(event)
	}
}