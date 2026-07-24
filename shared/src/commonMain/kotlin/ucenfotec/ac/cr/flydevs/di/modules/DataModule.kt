package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.data.remote.createHttpClient
import ucenfotec.ac.cr.flydevs.data.repository.StorageImageRepository
import ucenfotec.ac.cr.flydevs.data.repository.FirestoreExpansionRepository
import ucenfotec.ac.cr.flydevs.data.repository.FirestoreRarityRepository
import ucenfotec.ac.cr.flydevs.data.repository.AuthRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.BatchRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.CardCatalogRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.CardEnvelopeRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.GameCardRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.NotificationRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.OrderRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.StoreRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IExpansionRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IGameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IRarityRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository

val dataModule = module {
    single<IAuthRepository> { AuthRepositoryImpl(get()) }
    single { createHttpClient() }
    single<IGameCardRepository> { GameCardRepositoryImpl() }
    single<IImageStorageRepository> { StorageImageRepository(get()) }
    single<IRarityRepository> { FirestoreRarityRepository() }
    single<IExpansionRepository> { FirestoreExpansionRepository() }
    single<ICardCatalogRepository> { CardCatalogRepositoryImpl(get()) }
    single<ICardEnvelopeRepository> { CardEnvelopeRepositoryImpl() }
    single<IStoreRepository> { StoreRepositoryImpl() }

    single<IBatchRepository> { BatchRepositoryImpl() }

    single<IOrderRepository> { OrderRepositoryImpl(get()) }
    single<INotificationRepository> { NotificationRepositoryImpl() }
    single<IBatchRepository> { BatchRepositoryImpl() }
}
