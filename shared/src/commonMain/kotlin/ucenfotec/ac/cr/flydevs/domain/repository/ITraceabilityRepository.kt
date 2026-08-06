package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.UserTraceability

interface ITraceabilityRepository {

    /**
     * Trazabilidad completa de un usuario para el panel de administración.
     *
     * El repositorio solo expone datos no sensibles: identidad pública, montos,
     * fechas y estados de sus órdenes, ventas, ratings e incidencias.
     */
    fun observeUserTraceability(userId: String): Flow<UserTraceability>
}
