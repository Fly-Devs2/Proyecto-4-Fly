package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishGameCardViewModel

val presentationModule = module {
    viewModel { PublishGameCardViewModel(get()) }
}
