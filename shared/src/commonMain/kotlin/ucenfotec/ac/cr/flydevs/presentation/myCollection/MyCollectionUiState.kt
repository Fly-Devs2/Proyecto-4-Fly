package ucenfotec.ac.cr.flydevs.presentation.myCollection

import ucenfotec.ac.cr.flydevs.domain.model.CardStatus
import ucenfotec.ac.cr.flydevs.domain.model.GameCard

enum class CollectionFilter { TODAS, ACTIVAS, VENDIDAS, PAUSADAS }

data class MyCollectionUiState(
    val isLoading: Boolean = false,
    val cards: List<GameCard> = emptyList(),
    val activeFilter: CollectionFilter = CollectionFilter.TODAS,
    val errorMessage: String? = null,
    val pendingDeleteCardId: String? = null,
    val deleteError: String? = null,
) {
    val totalActivas: Int get() = cards.count { it.status == CardStatus.AVAILABLE }
    val totalVendidas: Int get() = cards.count { it.status == CardStatus.SOLD }
    val totalVistas: Int get() = cards.size * 3

    val filteredCards: List<GameCard> get() = when (activeFilter) {
        CollectionFilter.TODAS    -> cards
        CollectionFilter.ACTIVAS  -> cards.filter { it.status == CardStatus.AVAILABLE }
        CollectionFilter.VENDIDAS -> cards.filter { it.status == CardStatus.SOLD }
        CollectionFilter.PAUSADAS -> cards.filter { it.status == CardStatus.RESERVED }
    }
}
