package ucenfotec.ac.cr.flydevs.presentation.storePickups

data class StorePickupsUiState(
    val isLoading: Boolean = true,
    val allCards: List<StorePickupCardItem> = emptyList(),
    val filteredCards: List<StorePickupCardItem> = emptyList(),
    val storeId: String = "",
    val storeName: String = "",
    val errorMessage: String? = null,
    val selectedCardStatus: String? = null,
    val selectedUserRole: String? = null,
    val selectedRarity: String? = null,
    val arrivalDateFilter: String = "",
    val page: Int = 0,
    val pageSize: Int = 8,
    val availableRarities: List<String> = emptyList(),
) {
    val totalCards: Int get() = filteredCards.size

    val totalPages: Int get() = if (filteredCards.isEmpty()) 1 else ((filteredCards.size + pageSize - 1) / pageSize).coerceAtLeast(1)

    val visibleCards: List<StorePickupCardItem> get() = filteredCards.drop(page * pageSize).take(pageSize)

    val hasFilters: Boolean get() = selectedCardStatus != null || selectedUserRole != null || selectedRarity != null || arrivalDateFilter.isNotBlank()
}
