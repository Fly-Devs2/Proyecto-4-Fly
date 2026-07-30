package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.Incident

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
}
