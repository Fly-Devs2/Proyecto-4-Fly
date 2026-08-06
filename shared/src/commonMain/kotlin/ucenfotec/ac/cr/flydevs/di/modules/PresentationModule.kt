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
import ucenfotec.ac.cr.flydevs.presentation.notifications.NotificationsViewModel
import ucenfotec.ac.cr.flydevs.presentation.notifications.NotificationPreferencesViewModel
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.presentation.myBatches.MyBatchesViewModel
import ucenfotec.ac.cr.flydevs.presentation.orderDetail.OrderDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.batch.BatchEvidenceViewModel
import ucenfotec.ac.cr.flydevs.presentation.batch.ScanQrViewModel
import ucenfotec.ac.cr.flydevs.presentation.incident.ReportIncidentViewModel
import ucenfotec.ac.cr.flydevs.presentation.profile.ProfileViewModel
import ucenfotec.ac.cr.flydevs.presentation.shipmentDetail.ShipmentDetailViewModel

import ucenfotec.ac.cr.flydevs.presentation.storeBatches.StoreBatchesViewModel
import ucenfotec.ac.cr.flydevs.presentation.storeHome.StoreHomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.storeHome.StorePickupScanViewModel
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.CardCatalogViewModel
import ucenfotec.ac.cr.flydevs.presentation.publishGameCard.PublishGameCardViewModel
import ucenfotec.ac.cr.flydevs.presentation.register.RegisterViewModel

import ucenfotec.ac.cr.flydevs.presentation.Envelopes.CardEnvelopesViewModel
import ucenfotec.ac.cr.flydevs.presentation.envelope.CardEnvelopeViewModel
import ucenfotec.ac.cr.flydevs.presentation.purchaseHistory.PurchaseHistoryViewModel
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.presentation.AdminIncident.AdminIncidentsViewModel
import ucenfotec.ac.cr.flydevs.presentation.AdminIncidentDetail.AdminIncidentDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.adminUserDetail.AdminUserDetailViewModel
import ucenfotec.ac.cr.flydevs.presentation.adminUsers.AdminUsersViewModel
import ucenfotec.ac.cr.flydevs.presentation.messenger.MessengerHomeViewModel
import ucenfotec.ac.cr.flydevs.presentation.reputation.ReputationViewModel
import ucenfotec.ac.cr.flydevs.presentation.review.OrderReviewViewModel
import ucenfotec.ac.cr.flydevs.presentation.session.SessionViewModel

val presentationModule = module {
    viewModel { RegisterViewModel(get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { NotificationsViewModel(get(), get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { PublishGameCardViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { CardCatalogViewModel(get(), get(), get(), get()) }
    viewModel { MyCollectionViewModel(get<ICardCatalogRepository>(), get<IAuthRepository>()) }
    viewModel { ExchangeSellerViewModel(get(), get()) }
    viewModel { ExchangeBuyerViewModel(get(), get()) }
    viewModel { CardDetailViewModel(get(), get(), get(), get(), get()) }
    viewModel { CardEnvelopeViewModel(get(), get()) }
    viewModel { CardEnvelopesViewModel(get(), get(), get()) }
    viewModel { (orderId: String) -> OrderDetailViewModel(get(), get(), get(), get(), orderId) }
    viewModel { ProfileViewModel(get<IAuthRepository>()) }
    viewModel { PurchaseHistoryViewModel(get<IAuthRepository>(), get<IOrderRepository>()) }
    viewModel { MyBatchesViewModel(get(), get()) }
    viewModel { (batchDocumentId: String) -> ShipmentDetailViewModel(get(), get(), get(), batchDocumentId) }
    viewModel { NotificationPreferencesViewModel(get(), get()) }
    viewModel { SessionViewModel(get()) }
    viewModel { MessengerHomeViewModel(get(), get()) }
    viewModel { ScanQrViewModel(get(), get()) }
    viewModel { StoreBatchesViewModel(get(), get(), get(), get()) }
    viewModel { StoreHomeViewModel(get(), get(), get(), get()) }
    viewModel { StorePickupScanViewModel(get(), get()) }
    viewModel { BatchEvidenceViewModel(get(), get(), get()) }
    viewModel { (orderId: String) -> ReportIncidentViewModel(get(), get(), get(), orderId) }
    viewModel { OrderReviewViewModel(get()) }
    viewModel { ReputationViewModel(get()) }
    viewModel { AdminIncidentsViewModel(get(), get()) }
    viewModel {(incidentId: String)->  AdminIncidentDetailViewModel(get(), get(), get(), incidentId = incidentId) }
    viewModel { AdminUsersViewModel(get()) }
    viewModel { (userId: String) -> AdminUserDetailViewModel(get(), get(), userId) }
}
