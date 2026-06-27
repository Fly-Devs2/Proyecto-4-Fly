package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.data.remote.createHttpClient
import ucenfotec.ac.cr.flydevs.data.repository.StorageImageRepository
import ucenfotec.ac.cr.flydevs.data.repository.FirestoreExpansionRepository
import ucenfotec.ac.cr.flydevs.data.repository.FirestoreRarityRepository
import ucenfotec.ac.cr.flydevs.data.repository.AuthRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.GameCardRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.AuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IExpansionRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IGameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IImageStorageRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IRarityRepository

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl() }
    single { createHttpClient() }
    single<IGameCardRepository> { GameCardRepositoryImpl() }
    single<IImageStorageRepository> { StorageImageRepository(get()) }
    single<IRarityRepository> { FirestoreRarityRepository() }
    single<IExpansionRepository> { FirestoreExpansionRepository() }
}
