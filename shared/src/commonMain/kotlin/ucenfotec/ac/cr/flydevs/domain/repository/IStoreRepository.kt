package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.Store

interface IStoreRepository {
    suspend fun getStores(): List<Store>
}
