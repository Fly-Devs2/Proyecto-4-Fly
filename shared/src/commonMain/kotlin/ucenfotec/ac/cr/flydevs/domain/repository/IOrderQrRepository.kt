package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.OrderQrResult

interface IOrderQrRepository {

    suspend fun generateOrderQr(
        orderId: String,
        forceRegenerate: Boolean = false
    ): OrderQrResult
}