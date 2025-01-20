package yu.desk.mococomic

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApp : Application() {
    override fun onCreate() {
        DynamicColors.applyToActivitiesIfAvailable(this)
        super.onCreate()
    }
}