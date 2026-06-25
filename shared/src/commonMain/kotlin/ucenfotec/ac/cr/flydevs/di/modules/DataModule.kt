package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.data.remote.createHttpClient
import ucenfotec.ac.cr.flydevs.data.repository.FirebaseStorageImageRepository
import ucenfotec.ac.cr.flydevs.data.repository.GameCardRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.GameCardRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ImageStorageRepository

val dataModule = module {
    single { createHttpClient() }
    single<GameCardRepository> { GameCardRepositoryImpl() }
    single<ImageStorageRepository> { FirebaseStorageImageRepository(get()) }
}
