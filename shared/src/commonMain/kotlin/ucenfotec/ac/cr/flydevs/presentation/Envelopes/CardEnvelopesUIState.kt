package ucenfotec.ac.cr.flydevs.presentation.Envelopes

import ucenfotec.ac.cr.flydevs.domain.model.CardEnvelope

data class CardEnvelopesUIState(
    val isLoading: Boolean = false,
    val envelopes: List<CardEnvelope> = emptyList(),
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val deletingEnvelopeId: String? = null,
) {
    val pendingEnvelopeCount: Int
        get() = envelopes.size

    val totalCards: Int
        get() = envelopes.sumOf { envelope ->
            if (envelope.cards.isNotEmpty()) {
                envelope.cards.size
            } else {
                envelope.cardIds.size
            }
        }

    val totalAmount: Long
        get() = envelopes.sumOf { envelope ->
            envelope.total
        }

    val isEmpty: Boolean
        get() = envelopes.isEmpty() && !isLoading
}