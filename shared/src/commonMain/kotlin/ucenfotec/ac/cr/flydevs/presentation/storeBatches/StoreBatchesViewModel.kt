package ucenfotec.ac.cr.flydevs.presentation.storeBatches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.CardStatus
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IBatchRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository
import ucenfotec.ac.cr.flydevs.presentation.storePickups.StorePickupCardItem
import ucenfotec.ac.cr.flydevs.presentation.storePickups.StorePickupsUiState

class StoreBatchesViewModel(
    private val authRepository: IAuthRepository,
    private val batchRepository: IBatchRepository,
    private val orderRepository: IOrderRepository,
    private val cardCatalogRepository: ICardCatalogRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StoreBatchesUiState())
    val uiState: StateFlow<StoreBatchesUiState> = _uiState.asStateFlow()

    private val _pickupsUiState = MutableStateFlow(StorePickupsUiState())
    val pickupsUiState: StateFlow<StorePickupsUiState> = _pickupsUiState.asStateFlow()

    init {
        loadStoreAndBatches()
    }

    private fun loadStoreAndBatches() {
        val uid = authRepository.getCurrentUserUid()
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Inicia sesión para ver los lotes de tu tienda.") }
            _pickupsUiState.update { it.copy(isLoading = false, errorMessage = "Inicia sesión para ver los retiros de tu tienda.") }
            return
        }

        viewModelScope.launch {
            runCatching {
                val user = authRepository.getUserProfile(uid)
                val storeId = user?.storeId
                val storeName = user?.storeName

                if (storeId == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "No tienes una tienda asignada.") }
                    _pickupsUiState.update { it.copy(isLoading = false, errorMessage = "No tienes una tienda asignada.") }
                    return@launch
                }

                _uiState.update { it.copy(storeId = storeId, storeName = storeName ?: "Mi Tienda") }
                _pickupsUiState.update { it.copy(storeId = storeId, storeName = storeName ?: "Mi Tienda") }
                observeBatches(storeId)
                observeStorePickups(storeId)
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar el perfil.") }
                _pickupsUiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar los retiros.") }
            }
        }
    }

    private fun observeBatches(storeId: String) {
        batchRepository.observeOutgoingStoreBatches(storeId)
            .onEach { batches ->
                _uiState.update { it.copy(isLoading = false, batches = batches, errorMessage = null) }
            }
            .catch { e ->
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar lotes.") }
            }
            .launchIn(viewModelScope)
    }

    private fun observeStorePickups(storeId: String) {
        batchRepository.observeStorePickups(storeId)
            .onEach { deliveredBatches ->
                viewModelScope.launch {
                    runCatching {
                        val cardCatalog = cardCatalogRepository.getCardCatalog()
                        val cardsById = cardCatalog.associateBy { it.id }

                        val pickupCards = deliveredBatches.flatMap { batch ->
                            val orders = orderRepository.getOrdersByIds(batch.orderIds)
                            orders.flatMap { order ->
                                order.cards.map { orderCard ->
                                    val cardDetails = cardsById[orderCard.cardId]
                                    StorePickupCardItem(
                                        cardId = orderCard.cardId,
                                        name = orderCard.name.takeIf { it.isNotBlank() } ?: cardDetails?.name.orEmpty(),
                                        rarity = cardDetails?.rarity ?: "N/A",
                                        cardStatus = (cardDetails?.status ?: CardStatus.AVAILABLE).name,
                                        userRole = if (order.sellerName.isNotBlank() && order.buyerName.isNotBlank()) {
                                            "BUYER"
                                        } else if (order.sellerName.isNotBlank()) {
                                            "SELLER"
                                        } else {
                                            "BUYER"
                                        },
                                        buyerName = order.buyerName,
                                        sellerName = order.sellerName,
                                        arrivalAt = batch.deliveredAt ?: batch.updatedAt,
                                        arrivalDateLabel = formatArrivalDate(batch.deliveredAt ?: batch.updatedAt),
                                        batchId = batch.id,
                                        orderId = order.id,
                                        imageUrl = orderCard.imageUrl.takeIf { it.isNotBlank() } ?: cardDetails?.imageUrl.orEmpty(),
                                        condition = orderCard.condition,
                                        game = orderCard.game,
                                        destinationStoreName = batch.destinationStoreName,
                                    )
                                }
                            }
                        }.sortedByDescending { it.arrivalAt }

                        _pickupsUiState.update { current ->
                            val next = current.copy(
                                isLoading = false,
                                allCards = pickupCards,
                                availableRarities = pickupCards.map { it.rarity }.distinct().sorted(),
                                storeId = storeId,
                                storeName = current.storeName.ifBlank { "Mi Tienda" },
                                page = 0,
                                errorMessage = null,
                            )
                            next.copy(filteredCards = applyPickupFilters(pickupCards, next))
                        }
                    }.onFailure { e ->
                        _pickupsUiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar los retiros.") }
                    }
                }
            }
            .catch { e ->
                _pickupsUiState.update { it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar los retiros.") }
            }
            .launchIn(viewModelScope)
    }

    fun onPickupStatusFilterChange(value: String?) {
        _pickupsUiState.update { current ->
            val next = current.copy(selectedCardStatus = value?.takeUnless { it.equals("Todos", ignoreCase = true) }, page = 0)
            next.copy(filteredCards = applyPickupFilters(current.allCards, next))
        }
    }

    fun onPickupUserFilterChange(value: String?) {
        _pickupsUiState.update { current ->
            val next = current.copy(selectedUserRole = value?.takeUnless { it.equals("Todos", ignoreCase = true) }, page = 0)
            next.copy(filteredCards = applyPickupFilters(current.allCards, next))
        }
    }

    fun onPickupRarityFilterChange(value: String?) {
        _pickupsUiState.update { current ->
            val next = current.copy(selectedRarity = value?.takeUnless { it.equals("Todos", ignoreCase = true) }, page = 0)
            next.copy(filteredCards = applyPickupFilters(current.allCards, next))
        }
    }

    fun onPickupDateFilterChange(value: String) {
        _pickupsUiState.update { current ->
            val next = current.copy(arrivalDateFilter = value, page = 0)
            next.copy(filteredCards = applyPickupFilters(current.allCards, next))
        }
    }

    fun clearPickupFilters() {
        _pickupsUiState.update { current ->
            val next = current.copy(selectedCardStatus = null, selectedUserRole = null, selectedRarity = null, arrivalDateFilter = "", page = 0)
            next.copy(filteredCards = applyPickupFilters(current.allCards, next))
        }
    }

    fun onPickupPageChange(page: Int) {
        _pickupsUiState.update { current ->
            val totalPages = current.totalPages
            val safePage = page.coerceIn(0, totalPages - 1)
            current.copy(page = safePage)
        }
    }

    private fun applyPickupFilters(cards: List<StorePickupCardItem>, state: StorePickupsUiState): List<StorePickupCardItem> {
        return cards.filter { card ->
            val matchesStatus = state.selectedCardStatus?.let { selected ->
                card.cardStatus.equals(selected, ignoreCase = true)
            } ?: true

            val matchesUser = state.selectedUserRole?.let { selected ->
                card.userRole.equals(selected, ignoreCase = true)
            } ?: true

            val matchesRarity = state.selectedRarity?.let { selected ->
                card.rarity.equals(selected, ignoreCase = true)
            } ?: true

            val matchesDate = if (state.arrivalDateFilter.isBlank()) {
                true
            } else {
                card.arrivalDateLabel == state.arrivalDateFilter
            }

            matchesStatus && matchesUser && matchesRarity && matchesDate
        }
    }
}

private fun formatArrivalDate(millis: Long): String {
    val day = millis / 86_400_000L
    val (year, month, dayOfMonth) = fromEpochDay(day)
    return "${year}-${month.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
}

private fun fromEpochDay(epochDay: Long): Triple<Int, Int, Int> {
    val z = epochDay + 719468L
    val era = if (z >= 0L) z / 146097L else (z - 146096L) / 146097L
    val doe = z - era * 146097L
    val yoe = (doe - doe / 1460L + doe / 36524L - doe / 146096L) / 365L
    val y = yoe + era * 400L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val d = doy - (153L * mp + 2L) / 5L + 1L
    val m = mp + 3L - 12L * (mp / 10L)
    return Triple(y.toInt(), m.toInt(), d.toInt())
}
