package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.INCIDENT_DESCRIPTION_MAX_LENGTH
import ucenfotec.ac.cr.flydevs.domain.model.Incident
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.repository.IIncidentRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class IncidentRepositoryImpl : IIncidentRepository {

    private val incidentsCollection = Firebase.firestore.collection("incidents")

    override suspend fun reportIncident(incident: Incident): String {
        require(incident.orderId.isNotBlank()) {
            "La incidencia debe estar asociada a una orden."
        }
        require(incident.reporterId.isNotBlank()) {
            "Debes iniciar sesión para reportar una incidencia."
        }

        val description = incident.description.trim()
        require(description.isNotBlank()) {
            "Describí qué ocurrió con tu pedido."
        }
        require(description.length <= INCIDENT_DESCRIPTION_MAX_LENGTH) {
            "La descripción no puede superar los $INCIDENT_DESCRIPTION_MAX_LENGTH caracteres."
        }

        val now = getEpochMillis()

        val documentReference = incidentsCollection.add(
            incident.copy(
                description = description,
                status = IncidentStatus.OPEN,
                createdAt = now,
                updatedAt = now,
            )
        )

        return documentReference.id
    }

    override fun observeMyIncidentsForOrder(orderId: String, reporterId: String): Flow<List<Incident>> {
        require(orderId.isNotBlank()) {
            "El identificador de la orden es obligatorio."
        }
        require(reporterId.isNotBlank()) {
            "El identificador del reportante es obligatorio."
        }

        return incidentsCollection
            .where { "orderId" equalTo orderId }
            .where { "reporterId" equalTo reporterId }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .mapNotNull { it.toIncident() }
                    .sortedByDescending { it.createdAt }
            }
    }

    private fun DocumentSnapshot.toIncident(): Incident? =
        runCatching { data<Incident>().copy(id = id) }
            .onFailure { println("DEBUG_INCIDENTS: No se pudo mapear la incidencia $id: ${it.message}") }
            .getOrNull()
}
