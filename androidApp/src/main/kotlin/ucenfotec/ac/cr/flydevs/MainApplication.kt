package ucenfotec.ac.cr.flydevs

import android.app.Application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import org.koin.android.ext.koin.androidContext
import ucenfotec.ac.cr.flydevs.di.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)
        FlyMessagingService.ensureChannel(this)

        initKoin {
            androidContext(this@MainApplication)
        }
    }
}
