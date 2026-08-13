package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.Store
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository

class StoreRepositoryImpl : IStoreRepository {
    private val storesCollection = Firebase.firestore.collection("stores")

    override suspend fun getStores(): List<Store> {
        return try {
            val snapshot = storesCollection.get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<Store>().copy(id = doc.id)
                } catch (e: Exception) {
                    println("ERROR_STORE_REPO: Error mapping store ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("ERROR_STORE_REPO: Error fetching stores: ${e.message}")
            emptyList()
        }
    }
}
