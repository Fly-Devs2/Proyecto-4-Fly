package ucenfotec.ac.cr.flydevs.di.modules

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.data.remote.createHttpClient
import ucenfotec.ac.cr.flydevs.data.repository.AdminUserRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.TraceabilityRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.ITraceabilityRepository
import ucenfotec.ac.cr.flydevs.data.repository.StorageImageRepository
import ucenfotec.ac.cr.flydevs.data.repository.FirestoreExpansionRepository
import ucenfotec.ac.cr.flydevs.data.repository.FirestoreRarityRepository
import ucenfotec.ac.cr.flydevs.data.repository.AuthRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.BatchRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.CardCatalogRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.CardEnvelopeRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.GameCardRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.IncidentRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.NotificationRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.OrderRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.ReputationRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.ReviewRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.StoreRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.IAdminUserRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IExpansionRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IGameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IIncidentRepository
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IRarityRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReviewRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IRoleRepository
import ucenfotec.ac.cr.flydevs.data.repository.RoleRepositoryImpl

import ucenfotec.ac.cr.flydevs.data.remote.ScryfallApiService
import ucenfotec.ac.cr.flydevs.data.repository.ScryfallRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.ShipmentLocationRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.IScryfallRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IShipmentLocationRepository

val dataModule = module {
    single<IAuthRepository> { AuthRepositoryImpl(get()) }
    single { createHttpClient() }
    single { ScryfallApiService(get()) }
    single<IScryfallRepository> { ScryfallRepositoryImpl(get()) }
    single<IGameCardRepository> { GameCardRepositoryImpl() }
    single<IImageStorageRepository> { StorageImageRepository(get()) }
    single<IRarityRepository> { FirestoreRarityRepository() }
    single<IExpansionRepository> { FirestoreExpansionRepository() }
    single<ICardCatalogRepository> { CardCatalogRepositoryImpl(get()) }
    single<ICardEnvelopeRepository> { CardEnvelopeRepositoryImpl() }
    single<IStoreRepository> { StoreRepositoryImpl() }

    single<IOrderRepository> { OrderRepositoryImpl(get()) }

    single<IBatchRepository> { BatchRepositoryImpl(get()) }

    single<INotificationRepository> { NotificationRepositoryImpl() }
    single<FirebaseFirestore> {
        Firebase.firestore
    }
    single<IReviewRepository> { ReviewRepositoryImpl(get(), get()) }
    single<IReputationRepository> { ReputationRepositoryImpl(get()) }
    single<IAdminUserRepository> { AdminUserRepositoryImpl(get()) }
    single<ITraceabilityRepository> { TraceabilityRepositoryImpl(get(), get(), get()) }
    single<IRoleRepository> { RoleRepositoryImpl() }

    single<IIncidentRepository> { IncidentRepositoryImpl() }
    single<IShipmentLocationRepository> { ShipmentLocationRepositoryImpl() }
}
