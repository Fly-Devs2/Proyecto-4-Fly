package ucenfotec.ac.cr.flydevs.di.modules

import org.koin.dsl.module
import ucenfotec.ac.cr.flydevs.data.repository.AuthRepositoryImpl
import ucenfotec.ac.cr.flydevs.data.repository.GameCardRepositoryImpl
import ucenfotec.ac.cr.flydevs.domain.repository.AuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.GameCardRepository

val dataModule = module {
    single<GameCardRepository> { GameCardRepositoryImpl() }
    single<AuthRepository> { AuthRepositoryImpl() }
}
