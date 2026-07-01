package ucenfotec.ac.cr.flydevs

import android.app.Application
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Firebase.initialize(this)

        /* Koin se inicializa en MainActivity para asegurar el contexto de Activity */
    }
}
