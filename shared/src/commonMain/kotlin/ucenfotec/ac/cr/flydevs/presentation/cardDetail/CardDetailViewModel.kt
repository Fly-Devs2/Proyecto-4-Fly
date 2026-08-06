package ucenfotec.ac.cr.flydevs.presentation.cardDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository

class CardDetailViewModel(
    private val repository: ICardCatalogRepository,
    private val cardEnvelopeRepository: ICardEnvelopeRepository,
    private val authRepository: IAuthRepository,
    private val storeRepository: IStoreRepository,
    private val reputationRepository: IReputationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()


    private var loadedSellerId: String? = null
    private var sellerRatingJob: Job? = null

    fun loadCard(cardId: String, userId: String, fromCollection: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching {
                if (fromCollection) {
                    repository.getCardsBySeller(userId).first { it.id == cardId }
                } else {
                    repository.getCardCatalog().first { it.id == cardId }
                }
            }
                .onSuccess { card ->
                    _uiState.value = _uiState.value.copy(isLoading = false, card = card, currentImageIndex = 0)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar la carta",
                    )
                }

            val card = _uiState.value.card

            if (card != null) {
                observeSellerRating(card.sellerId)
                loadSeller(card.sellerId)
                loadStoreName(card.sourceStore)
            }
        }
    }

    private suspend fun loadStoreName(storeId: String) {
        if (storeId.isBlank()) return
        try {
            val stores = storeRepository.getStores()
            val store = stores.find { it.id == storeId }
            _uiState.update { it.copy(sourceStoreName = store?.name) }
        } catch (e: Exception) {
            println("ERROR_CARD_DETAIL: Error loading store name: ${e.message}")
        }
    }

    fun toggleFavorite() {
        _uiState.value = _uiState.value.copy(isFavorite = !_uiState.value.isFavorite)
    }

    fun onIdCopied() {
        _uiState.value = _uiState.value.copy(idCopied = true)
    }

    fun clearIdCopied() {
        _uiState.value = _uiState.value.copy(idCopied = false)
    }
    private fun observeSellerRating(
        sellerId: String
    ) {
        if (sellerId.isBlank()) {
            sellerRatingJob?.cancel()
            loadedSellerId = null

            _uiState.update { currentState ->
                currentState.copy(
                    sellerAverageRating = 0.0,
                    sellerReviewCount = 0,
                    isLoadingSellerRating = false
                )
            }

            return
        }

        /*
         * Evita crear otro listener para el mismo vendedor
         * cuando la pantalla se recompone o vuelve a cargar
         * la misma carta.
         */
        if (
            loadedSellerId == sellerId &&
            sellerRatingJob?.isActive == true
        ) {
            return
        }

        sellerRatingJob?.cancel()
        loadedSellerId = sellerId

        _uiState.update { currentState ->
            currentState.copy(
                sellerAverageRating = 0.0,
                sellerReviewCount = 0,
                isLoadingSellerRating = true
            )
        }

        sellerRatingJob = viewModelScope.launch {
            reputationRepository
                .observeRatingSummary(sellerId)
                .catch { exception ->
                    println(
                        "CARD_DETAIL_RATING_ERROR | " +
                                "sellerId=$sellerId | " +
                                "message=${exception.message}"
                    )

                    _uiState.update { currentState ->
                        currentState.copy(
                            sellerAverageRating = 0.0,
                            sellerReviewCount = 0,
                            isLoadingSellerRating = false
                        )
                    }
                }
                .collect { summary ->
                    /*
                     * El listener anterior podría emitir mientras
                     * estamos cambiando hacia otra carta.
                     */
                    if (loadedSellerId != sellerId) {
                        return@collect
                    }

                    _uiState.update { currentState ->
                        currentState.copy(
                            sellerAverageRating =
                                summary.allTime.sellerAverageRating,

                            sellerReviewCount =
                                summary.allTime.sellerReviewCount,

                            isLoadingSellerRating = false
                        )
                    }
                }
        }
    }

    fun addToEnvelope(
        userId: String,
        cardId: String
    ) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isAddingToEnvelope = true,
                    actionErrorMessage = null,
                    targetEnvelopeId = null
                )
            }

            try {
                val envelopeId = cardEnvelopeRepository.addCardToEnvelope(
                    userId = userId,
                    cardId = cardId
                )

                _uiState.update { currentState ->
                    currentState.copy(
                        isAddingToEnvelope = false,
                        addedToEnvelope = true,
                        targetEnvelopeId = envelopeId
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isAddingToEnvelope = false,
                        actionErrorMessage = exception.message ?: "No se pudo agregar la carta al sobre."
                    )
                }
            }
        }
    }
    fun clearEnvelopeNavigation() {
        _uiState.update { currentState ->
            currentState.copy(
                targetEnvelopeId = null,
                actionErrorMessage = null
            )
        }
    }

    fun onImageSwipe(newIndex: Int) {
        _uiState.update { it.copy(currentImageIndex = newIndex) }
    }

    private suspend fun loadSeller(sellerId: String) {
        if (sellerId.isBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    seller = null,

                )
            }
            return
        }

        _uiState.update { currentState ->
            currentState.copy(
                seller = null,

            )
        }

        try {
            val seller = authRepository.getUserProfile(sellerId)

            _uiState.update { currentState ->
                currentState.copy(
                    seller = seller,

                )
            }
        } catch (exception: Exception) {
            _uiState.update { currentState ->
                currentState.copy(
                    seller = null,

                )
            }
        }
    }
    override fun onCleared() {
        sellerRatingJob?.cancel()
        sellerRatingJob = null
        loadedSellerId = null

        super.onCleared()
    }




    fun reserveCard() {
        _uiState.value = _uiState.value.copy(reserved = true)
    }
}

