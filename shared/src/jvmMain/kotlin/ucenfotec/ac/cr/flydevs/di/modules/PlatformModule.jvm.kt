package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.core.module.Module
import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.auth.GoogleAuthManager
import ucenfotec.ac.cr.flydevs.auth.JvmGoogleAuthManager

actual fun platformModule(): Module = module {
    single<GoogleAuthManager> { JvmGoogleAuthManager() }
}