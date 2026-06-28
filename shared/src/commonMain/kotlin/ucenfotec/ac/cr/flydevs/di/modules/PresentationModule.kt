package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.presentation.home.HomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.login.LoginViewModel
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.CardCatalogViewModel
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishGameCardViewModel
import ucenfotec.ac.cr.flydevs.presentation.register.RegisterViewModel

val presentationModule = module {
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { PublishGameCardViewModel(get(), get(), get(), get(), get()) }
    viewModel { CardCatalogViewModel(get(), get(), get()) }
}
