package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.core.module.Module
import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.auth.AndroidGoogleAuthManager
import ucenfotec.ac.cr.flydevs.auth.GoogleAuthManager

actual fun platformModule(): Module = module {
    single<GoogleAuthManager> {
        AndroidGoogleAuthManager(
            context = get(),
            serverClientId = "502216327523-pu8ebco1p7gm4sktb4m46ovj5sufdpfb.apps.googleusercontent.com"

        )
    }
}