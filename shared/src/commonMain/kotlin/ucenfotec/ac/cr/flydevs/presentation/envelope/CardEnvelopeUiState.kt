package ucenfotec.ac.cr.flydevs.presentation.envelope

import kotlinx.serialization.Serializable
import ucenfotec.ac.cr.flydevs.domain.model.CardEnvelope
import ucenfotec.ac.cr.flydevs.domain.model.GameCard


@Serializable
data class CardEnvelopeUiState(
    val isLoading: Boolean = false,
    val isGeneratingOrder: Boolean = false,
    val envelope: CardEnvelope? = null,
    val cards: List<GameCard> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null
) {

    val cardCount: Int
        get() = cards.size

    val subTotal: Long
        get() = envelope?.subTotal ?: 0L

    val total: Long
        get() = envelope?.total ?: 0L

    val shippingCost: Long
        get() = (total - subTotal).coerceAtLeast(0L)

    val isEnvelopeEmpty: Boolean
        get() = cards.isEmpty()

    val canGenerateOrder: Boolean
        get() = cards.isNotEmpty() && !isLoading && !isGeneratingOrder
}