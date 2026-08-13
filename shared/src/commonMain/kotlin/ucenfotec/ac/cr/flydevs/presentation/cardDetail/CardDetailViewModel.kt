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
import ucenfotec.ac.cr.flydevs.domain.model.CardGame
import ucenfotec.ac.cr.flydevs.domain.model.CardLanguage
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardCatalogRepository
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IScryfallRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository

class CardDetailViewModel(
    private val repository: ICardCatalogRepository,
    private val cardEnvelopeRepository: ICardEnvelopeRepository,
    private val authRepository: IAuthRepository,
    private val storeRepository: IStoreRepository,
    private val reputationRepository: IReputationRepository,
    private val scryfallRepository: IScryfallRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardDetailUiState())
    val uiState: StateFlow<CardDetailUiState> = _uiState.asStateFlow()


    private var loadedSellerId: String? = null
    private var sellerRatingJob: Job? = null

    fun loadCard(cardId: String, userId: String, fromCollection: Boolean = false) {
        if (_uiState.value.card?.id == cardId) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                if (fromCollection) {
                    repository.getCardsBySeller(userId).first { it.id == cardId }
                } else {
                    repository.getCardCatalog().first { it.id == cardId }
                }
            }
                .onSuccess { card ->
                    _uiState.update { it.copy(isLoading = false, card = card, currentImageIndex = 0) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar la carta",
                    ) }
                }

            val card = _uiState.value.card ?: run {
                println("SCRYFALL_DEBUG: Card is null after loadCard success")
                return@launch
            }

            observeSellerRating(card.sellerId)
            loadSeller(card.sellerId)
            loadStoreName(card.sourceStore)
            if (card.game == CardGame.MAGIC) {
                fetchScryfallData(card.name, card.language)
            } else {
                _uiState.update { it.copy(scryfallVersions = emptyList(), isLoadingScryfall = false) }
            }
        }
    }

    private fun fetchScryfallData(cardName: String, cardLanguage: CardLanguage? = null) {
        val query = cardName.trim()
        if (query.isBlank()) {
            println("SCRYFALL_DEBUG: Skipping empty name")
            _uiState.update { it.copy(scryfallVersions = emptyList(), isLoadingScryfall = false) }
            return
        }
        
        val langCode = when(cardLanguage) {
            CardLanguage.ES -> "es"
            CardLanguage.EN -> "en"
            CardLanguage.JP -> "ja"
            CardLanguage.DE -> "de"
            CardLanguage.FR -> "fr"
            CardLanguage.IT -> "it"
            else -> null
        }

        viewModelScope.launch {
            println("SCRYFALL_DEBUG: Starting fetch for '$query' (lang: $langCode)")
            _uiState.update { it.copy(isLoadingScryfall = true) }
            try {
                val versions = scryfallRepository.getCardPrints(query, langCode)
                println("SCRYFALL_DEBUG: Found ${versions.size} versions for '$query'")
                _uiState.update { it.copy(scryfallVersions = versions, isLoadingScryfall = false) }
            } catch (e: Exception) {
                println("SCRYFALL_DEBUG: Error fetching '$query': ${e.message}")
                _uiState.update { it.copy(isLoadingScryfall = false) }
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
        _uiState.update { it.copy(isFavorite = !it.isFavorite) }
    }

    fun onIdCopied() {
        _uiState.update { it.copy(idCopied = true) }
    }

    fun clearIdCopied() {
        _uiState.update { it.copy(idCopied = false) }
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
        _uiState.update { it.copy(reserved = true) }
    }
}

