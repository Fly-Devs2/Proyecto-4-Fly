package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Incident
import ucenfotec.ac.cr.flydevs.domain.model.IncidentPriority
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.IncidentUpdate

interface IIncidentRepository {

    /**
     * El usuario reporta una incidencia sobre una orden. Retorna el ID generado.
     */
    suspend fun reportIncident(incident: Incident): String

    /**
     * Incidencias que [reporterId] ya abrió sobre una orden; avisa para no reportar dos veces.
     *
     * Filtra por reportante porque las reglas de Firestore solo dejan leer las propias.
     */
    fun observeMyIncidentsForOrder(orderId: String, reporterId: String): Flow<List<Incident>>

    fun observeAllIncidents(): Flow<List<Incident>>

    /**
     * Detalle de una incidencia específica.
     */
    fun observeIncident(
        incidentId: String
    ): Flow<Incident?>

    /**
     * Cambia el estado administrativo.
     */
    suspend fun updateIncidentStatus(
        incidentId: String,
        status: IncidentStatus
    )

    /**
     * Cambia la prioridad administrativa.
     */
    suspend fun updateIncidentPriority(
        incidentId: String,
        priority: IncidentPriority
    )

    /**
     * Marca la incidencia como resuelta.
     */
    suspend fun resolveIncident(
        incidentId: String
    )

    /**
     * Agrega una nota de seguimiento.
     */
    suspend fun addIncidentUpdate(
        update: IncidentUpdate
    ): String

    /**
     * Historial de notas, más recientes primero.
     */
    fun observeIncidentUpdates(
        incidentId: String
    ): Flow<List<IncidentUpdate>>
}
