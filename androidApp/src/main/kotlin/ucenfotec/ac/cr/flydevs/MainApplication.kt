package ucenfotec.ac.cr.flydevs

import android.app.Application
import ucenfotec.ac.cr.flydevs.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin()
    }
}
