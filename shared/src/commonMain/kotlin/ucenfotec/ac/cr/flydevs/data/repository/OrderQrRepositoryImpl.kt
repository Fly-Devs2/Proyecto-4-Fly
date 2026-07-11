package ucenfotec.ac.cr.flydevs.data.repository


import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions
import dev.gitlive.firebase.functions.httpsCallable
import ucenfotec.ac.cr.flydevs.domain.model.GenerateOrderQrRequest
import ucenfotec.ac.cr.flydevs.domain.model.OrderQrResult
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderQrRepository

class OrderQrRepositoryImpl: IOrderQrRepository {

    private val functions = Firebase.functions("us-central1")

    override suspend fun generateOrderQr(
        orderId: String,
        forceRegenerate: Boolean
    ): OrderQrResult {
        val callable = functions.httpsCallable("generateOrderQr")

        val result = callable(
            GenerateOrderQrRequest(
                orderId = orderId,
                forceRegenerate = forceRegenerate
            )
        )

        return result.data()
    }
}