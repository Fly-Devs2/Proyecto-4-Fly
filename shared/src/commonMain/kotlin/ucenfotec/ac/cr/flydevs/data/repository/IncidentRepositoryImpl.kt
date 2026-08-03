package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.INCIDENT_DESCRIPTION_MAX_LENGTH
import ucenfotec.ac.cr.flydevs.domain.model.INCIDENT_UPDATE_MAX_LENGTH
import ucenfotec.ac.cr.flydevs.domain.model.Incident
import ucenfotec.ac.cr.flydevs.domain.model.IncidentPriority
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.IncidentUpdate
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
    override fun observeAllIncidents():
            Flow<List<Incident>> {
        return incidentsCollection
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .mapNotNull {
                        it.toIncident()
                    }
                    .sortedByDescending {
                        it.createdAt
                    }
            }
    }

    override fun observeIncident(
        incidentId: String
    ): Flow<Incident?> {
        require(incidentId.isNotBlank()) {
            "El identificador de la incidencia es obligatorio."
        }

        return incidentsCollection
            .document(incidentId)
            .snapshots
            .map { snapshot ->
                if (snapshot.exists) {
                    snapshot.toIncident()
                } else {
                    null
                }
            }
    }

    override suspend fun updateIncidentStatus(
        incidentId: String,
        status: IncidentStatus
    ) {
        require(incidentId.isNotBlank()) {
            "El identificador de la incidencia es obligatorio."
        }

        incidentsCollection
            .document(incidentId)
            .update(
                "status" to status.name,
                "updatedAt" to getEpochMillis()
            )
    }

    override suspend fun updateIncidentPriority(
        incidentId: String,
        priority: IncidentPriority
    ) {
        require(incidentId.isNotBlank()) {
            "El identificador de la incidencia es obligatorio."
        }

        incidentsCollection
            .document(incidentId)
            .update(
                "priority" to priority.name,
                "updatedAt" to getEpochMillis()
            )
    }

    override suspend fun addIncidentUpdate(
        update: IncidentUpdate
    ): String {
        require(update.incidentId.isNotBlank()) {
            "La nota debe estar asociada a una incidencia."
        }

        require(update.authorId.isNotBlank()) {
            "No se pudo identificar al administrador."
        }

        val message =
            update.message.trim()

        require(message.isNotBlank()) {
            "La nota de seguimiento no puede estar vacía."
        }

        require(
            message.length <=
                    INCIDENT_UPDATE_MAX_LENGTH
        ) {
            "La nota no puede superar los " +
                    "$INCIDENT_UPDATE_MAX_LENGTH caracteres."
        }

        val now =
            getEpochMillis()

        val incidentReference =
            incidentsCollection.document(
                update.incidentId
            )

        val updateReference =
            incidentReference
                .collection("updates")
                .add(
                    update.copy(
                        message = message,
                        createdAt = now
                    )
                )

        /*
         * También actualizamos la incidencia para reflejar
         * que tuvo actividad administrativa reciente.
         */
        incidentReference.update(
            "updatedAt" to now
        )

        return updateReference.id
    }

    override fun observeIncidentUpdates(
        incidentId: String
    ): Flow<List<IncidentUpdate>> {
        require(incidentId.isNotBlank()) {
            "El identificador de la incidencia es obligatorio."
        }

        return incidentsCollection
            .document(incidentId)
            .collection("updates")
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents
                    .mapNotNull {
                        it.toIncidentUpdate()
                    }
                    .sortedByDescending {
                        it.createdAt
                    }
            }
    }



    override suspend fun resolveIncident(
        incidentId: String
    ) {
        updateIncidentStatus(
            incidentId = incidentId,
            status = IncidentStatus.RESOLVED
        )
    }
    private fun DocumentSnapshot.toIncidentUpdate():
            IncidentUpdate? {
        return runCatching {
            data<IncidentUpdate>().copy(
                id = id
            )
        }
            .onFailure {
                println(
                    "DEBUG_INCIDENTS: " +
                            "No se pudo mapear la nota " +
                            "$id: ${it.message}"
                )
            }
            .getOrNull()
    }



    private fun DocumentSnapshot.toIncident(): Incident? =
        runCatching { data<Incident>().copy(id = id) }
            .onFailure { println("DEBUG_INCIDENTS: No se pudo mapear la incidencia $id: ${it.message}") }
            .getOrNull()


}
