package ucenfotec.ac.cr.flydevs.di

import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import ucenfotec.ac.cr.flydevs.di.modules.sharedModule

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        includes(config)
        modules(sharedModule)
    }
}