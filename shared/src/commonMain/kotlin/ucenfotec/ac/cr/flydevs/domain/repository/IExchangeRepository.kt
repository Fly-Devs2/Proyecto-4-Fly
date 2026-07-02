package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.Exchange

interface IExchangeRepository {

    /** Crea el sobre con un id único y su estado inicial: ESPERANDO_ENTREGA_TIENDA. */
    suspend fun createExchange(exchange: Exchange): Exchange

    /**
     * Recupera un intercambio específico basado en su identificador único.
     * @param exchangeId El identificador del intercambio a buscar.
     * @return El objeto [Exchange] si se encuentra, o `null` si no existe.
     */
    suspend fun getExchange(exchangeId: String): Exchange?

    /**
     * Obtiene todos los intercambios (reservas) asociados a un comprador específico.
     *
     * @param buyerId El identificador único del comprador.
     * @return Una lista de objetos [Exchange] pertenecientes al comprador.
     */
    suspend fun getExchangesForBuyer(buyerId: String): List<Exchange>

    /**
     * El vendedor sube la foto del envío, Pasa a ESPERANDO_COMPROBANTE_SINPE.
     * Sin esta evidencia no se le pide el comprobante al comprador.
     */
    suspend fun submitSellerEvidence(exchangeId: String, evidenceUrl: String): Exchange

    /**
     * El comprador sube el comprobante SINPE. Pasa a COMPROBANTE_RECIBIDO.
     * Solo permitido si ya existe la evidencia del vendedor.
     */
    suspend fun submitSinpeProof(exchangeId: String, proofUrl: String): Exchange
}
