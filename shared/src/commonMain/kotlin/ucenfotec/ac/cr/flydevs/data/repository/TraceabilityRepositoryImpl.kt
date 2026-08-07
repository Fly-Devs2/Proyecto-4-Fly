package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.Incident
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.Order
import ucenfotec.ac.cr.flydevs.domain.model.OrderStatus
import ucenfotec.ac.cr.flydevs.domain.model.Review
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.TraceStatusTone
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityRecord
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilitySummary
import ucenfotec.ac.cr.flydevs.domain.model.TraceabilityTab
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRatingSummary
import ucenfotec.ac.cr.flydevs.domain.model.UserTraceability
import ucenfotec.ac.cr.flydevs.domain.model.orderCodeOf
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ITraceabilityRepository

/**
 * Arma la trazabilidad del usuario a partir de las colecciones que ya existen
 * (`orders`, `reviews`, `incidents`, `user_ratings`)
 */
class TraceabilityRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val reputationRepository: IReputationRepository,
    private val orderRepository: IOrderRepository,
) : ITraceabilityRepository {

    private val reviewsCollection = firestore.collection("reviews")
    private val incidentsCollection = firestore.collection("incidents")

    override fun observeUserTraceability(userId: String): Flow<UserTraceability> {
        val reviewsFlow = combine(
            observeReviews(field = "reviewedUserId", userId = userId),
            observeReviews(field = "reviewerId", userId = userId),
        ) { received, given -> received to given }

        val incidentsFlow = combine(
            observeIncidents(field = "reporterId", userId = userId),
            observeIncidents(field = "counterpartId", userId = userId),
        ) { reported, involved -> (reported + involved).distinctBy { it.id } }

        return combine(
            reputationRepository.observeUser(userId),
            reputationRepository.observeRatingSummary(userId),
            orderRepository.getOrdersForUser(userId),
            reviewsFlow,
            incidentsFlow,
        ) { user, ratingSummary, orders, reviews, incidents ->
            build(
                userId = userId,
                user = user,
                ratingSummary = ratingSummary,
                orders = orders,
                reviewsReceived = reviews.first,
                reviewsGiven = reviews.second,
                incidents = incidents,
            )
        }
    }

    private fun build(
        userId: String,
        user: User?,
        ratingSummary: UserRatingSummary,
        orders: List<Order>,
        reviewsReceived: List<Review>,
        reviewsGiven: List<Review>,
        incidents: List<Incident>,
    ): UserTraceability {
        val amountByOrderId = orders.associate { it.id to it.montoTotal }

        val purchases = orders
            .filter { it.buyerId.trim() == userId.trim() }
            .sortedByDescending { it.createdAt }

        val sales = orders
            .filter { it.sellerId.trim() == userId.trim() }
            .sortedByDescending { it.createdAt }

        val orderRecords = purchases.map { it.toRecord() }
        val saleRecords = sales.map { it.toRecord() }
        val ratingRecords = reviewsReceived
            .map { it.toRecord(received = true, amountByOrderId = amountByOrderId) }
            .sortedByDescending { it.timestamp }

        val activityRecords = (
            orderRecords +
                saleRecords +
                ratingRecords +
                reviewsGiven.map { it.toRecord(received = false, amountByOrderId = amountByOrderId) } +
                incidents.map { it.toRecord(amountByOrderId = amountByOrderId) }
            ).sortedByDescending { it.timestamp }

        val averageRating = ratingSummary.allTime.sellerAverageRating
            .takeIf { ratingSummary.allTime.sellerReviewCount > 0 }
            ?: ratingSummary.allTime.buyerAverageRating

        return UserTraceability(
            user = user,
            summary = TraceabilitySummary(
                orderCount = orderRecords.size,
                salesCount = saleRecords.size,
                averageRating = averageRating,
                eventCount = activityRecords.size,
            ),
            records = mapOf(
                TraceabilityTab.ORDERS to orderRecords,
                TraceabilityTab.SALES to saleRecords,
                TraceabilityTab.RATINGS to ratingRecords,
                TraceabilityTab.ACTIVITY to activityRecords,
            ),
        )
    }

    private fun Order.toRecord(): TraceabilityRecord = TraceabilityRecord(
        code = orderCodeOf(id),
        timestamp = createdAt,
        detail = if (cards.size == 1) "1 carta" else "${cards.size} cartas",
        amount = montoTotal,
        statusLabel = status.label,
        statusTone = status.tone(),
    )

    private fun Review.toRecord(
        received: Boolean,
        amountByOrderId: Map<String, Long>,
    ): TraceabilityRecord {
        val role = if (reviewedRole == ReviewRole.SELLER) "vendedor" else "comprador"

        return TraceabilityRecord(
            code = orderCodeOf(orderId),
            timestamp = if (updatedAt > 0L) updatedAt else createdAt,
            detail = if (received) "Rating recibido como $role" else "Rating enviado al $role",
            amount = amountByOrderId[orderId],
            statusLabel = "★ $rating",
            statusTone = when {
                rating >= 4.0 -> TraceStatusTone.POSITIVE
                rating >= 3.0 -> TraceStatusTone.NEUTRAL
                else -> TraceStatusTone.NEGATIVE
            },
        )
    }

    private fun Incident.toRecord(
        amountByOrderId: Map<String, Long>,
    ): TraceabilityRecord = TraceabilityRecord(
        code = orderCode.ifBlank { orderCodeOf(orderId) },
        timestamp = createdAt,
        detail = "Incidencia: ${type.label}",
        amount = amountByOrderId[orderId],
        statusLabel = status.label,
        statusTone = when (status) {
            IncidentStatus.RESOLVED -> TraceStatusTone.POSITIVE
            IncidentStatus.URGENT -> TraceStatusTone.NEGATIVE
            else -> TraceStatusTone.NEUTRAL
        },
    )

    private fun OrderStatus.tone(): TraceStatusTone = when (this) {
        OrderStatus.PICKED_UP -> TraceStatusTone.POSITIVE
        OrderStatus.DISPUTED, OrderStatus.CANCELLED -> TraceStatusTone.NEGATIVE
        else -> TraceStatusTone.NEUTRAL
    }

    private fun observeReviews(field: String, userId: String): Flow<List<Review>> =
        reviewsCollection
            .where { field equalTo userId }
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.toReview() } }

    private fun observeIncidents(field: String, userId: String): Flow<List<Incident>> =
        incidentsCollection
            .where { field equalTo userId }
            .snapshots
            .map { snapshot -> snapshot.documents.mapNotNull { it.toIncident() } }

    private fun DocumentSnapshot.toReview(): Review? =
        runCatching { data<Review>() }
            .onFailure { println("DEBUG_TRACEABILITY: reseña $id no mapeada: ${it.message}") }
            .getOrNull()

    private fun DocumentSnapshot.toIncident(): Incident? =
        runCatching { data<Incident>().copy(id = id) }
            .onFailure { println("DEBUG_TRACEABILITY: incidencia $id no mapeada: ${it.message}") }
            .getOrNull()
}
