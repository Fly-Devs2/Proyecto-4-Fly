package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.dsl.module

val sharedModule = module {
    includes(dataModule, presentationModule, platformModule())
}
