package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.presentation.cardDetail.CardDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.exchange.ExchangeBuyerViewModel
import ucenfotec.ac.cr.flydevs.presentation.exchange.ExchangeSellerViewModel
import ucenfotec.ac.cr.flydevs.presentation.home.HomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.login.LoginViewModel
import ucenfotec.ac.cr.flydevs.presentation.myCollection.MyCollectionViewModel
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.presentation.orderDetail.OrderDetailViewModel

import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.CardCatalogViewModel
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishGameCardViewModel
import ucenfotec.ac.cr.flydevs.presentation.register.RegisterViewModel

import ucenfotec.ac.cr.flydevs.presentation.Envelopes.CardEnvelopesViewModel
import ucenfotec.ac.cr.flydevs.presentation.envelope.CardEnvelopeViewModel

val presentationModule = module {
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { PublishGameCardViewModel(get(), get(), get(), get(), get()) }
    viewModel { CardCatalogViewModel(get(), get(), get()) }
    viewModel { MyCollectionViewModel(get<ICardCatalogRepository>(), get<IAuthRepository>()) }
    viewModel { ExchangeSellerViewModel(get(), get()) }
    viewModel { ExchangeBuyerViewModel(get(), get()) }
    viewModel { CardDetailViewModel(get(), get()) }
    viewModel { CardEnvelopeViewModel(get()) }
    viewModel { CardEnvelopesViewModel(get()) }
    viewModel { (orderId: String) -> OrderDetailViewModel(get(), get(), get(), orderId) }
}
