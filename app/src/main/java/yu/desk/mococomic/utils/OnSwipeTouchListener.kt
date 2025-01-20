package yu.desk.mococomic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs


class OnSwipeTouchListener(val context: Context) : SimpleOnGestureListener(), OnTouchListener {
    companion object {
        private const val SwipeThreshold = 100
        private const val SwipeVelocityThreshold = 100
    }

    private var gestureDetector: GestureDetector = GestureDetector(context, this)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean = gestureDetector.onTouchEvent(event)

    override fun onDown(e: MotionEvent) = true

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float,
    ): Boolean {
        try {
            val diffY = e2.y - (e1?.y ?: 0f)
            val diffX = e2.x - (e1?.x ?: 0f)

            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SwipeThreshold && abs(velocityX) > SwipeVelocityThreshold) {
                    return when {
                        diffX > 0 -> onSwipeRight()
                        else -> onSwipeLeft()
                    }
                }
            } else if (abs(diffY) > SwipeThreshold && abs(velocityY) > SwipeVelocityThreshold) {
                return when {
                    diffY > 0 -> onSwipeDown()
                    else -> onSwipeUp()
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }

    open fun onSwipeRight(): Boolean {
        return false
    }

    open fun onSwipeLeft(): Boolean {
        return false
    }

    open fun onSwipeUp(): Boolean {
        return false
    }

    open fun onSwipeDown(): Boolean {
        return false
    }

}
