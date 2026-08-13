package ucenfotec.ac.cr.flydevs.presentation.envelope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.CardEnvelope
import ucenfotec.ac.cr.flydevs.domain.model.ShippingMethod
import ucenfotec.ac.cr.flydevs.domain.model.Store
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IStoreRepository

class CardEnvelopeViewModel(
    private val cardEnvelopeRepository: ICardEnvelopeRepository,
    private val storeRepository: IStoreRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(CardEnvelopeUiState())
    val uiState: StateFlow<CardEnvelopeUiState> = _uiState.asStateFlow()

    fun loadEnvelopeById(envelopeId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                val stores = storeRepository.getStores()
                val envelope = cardEnvelopeRepository.getCardEnvelopeById(envelopeId)

                if (envelope == null) {
                    throw Exception("No se encontró el sobre seleccionado.")
                }

                val selectedStore = stores.find { it.id == envelope.destinationStore }
                val sourceStore = stores.find { it.id == envelope.sourceStore }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        envelope = envelope,
                        cards = envelope.cards,
                        stores = stores,
                        selectedStore = selectedStore,
                        sourceStoreName = sourceStore?.name
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se pudo cargar el sobre."
                    )
                }
            }
        }
    }

    fun addCardToEnvelope(userId: String, cardId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                cardEnvelopeRepository.addCardToEnvelope(
                    userId = userId,
                    cardId = cardId
                )

                val updatedEnvelope = getPendingEnvelope(userId)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        envelope = updatedEnvelope,
                        cards = updatedEnvelope?.cards ?: emptyList(),
                        successMessage = "Carta agregada al sobre correctamente."
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se pudo agregar la carta al sobre."
                    )
                }
            }
        }
    }

    fun removeCardFromEnvelope(envelopeId: String, cardId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                cardEnvelopeRepository.removeCardFromEnvelope(
                    envelopeId = envelopeId,
                    cardId = cardId
                )

                val updatedEnvelope = cardEnvelopeRepository.getCardEnvelopeById(envelopeId)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        envelope = updatedEnvelope,
                        cards = updatedEnvelope?.cards ?: emptyList(),
                        successMessage = "Carta eliminada del sobre correctamente."
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se pudo eliminar la carta del sobre."
                    )
                }
            }
        }
    }

    fun generateOrderFromEnvelope(envelopeId: String) {
        if (_uiState.value.selectedStore == null) {
            _uiState.update { it.copy(errorMessage = "Debes seleccionar una tienda de destino.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isGeneratingOrder = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                cardEnvelopeRepository.generateOrderFromEnvelope(envelopeId)

                _uiState.update { currentState ->
                    currentState.copy(
                        isGeneratingOrder = false,
                        envelope = null,
                        cards = emptyList(),
                        successMessage = "Orden generada correctamente."
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isGeneratingOrder = false,
                        errorMessage = exception.message ?: "No se pudo generar la orden."
                    )
                }
            }
        }
    }

    fun onStoreChange(envelopeId: String, store: Store) {
        _uiState.update { it.copy(selectedStore = store) }
        viewModelScope.launch {
            try {
                cardEnvelopeRepository.updateEnvelopeDestinationStore(envelopeId, store.id)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "No se pudo actualizar la tienda de destino.") }
            }
        }
    }

    fun onShippingMethodChange(envelopeId: String, method: ShippingMethod) {
        val currentEnvelope = _uiState.value.envelope ?: return
        
        if (method == ShippingMethod.PICKUP) {
            val sourceStoreId = currentEnvelope.sourceStore
            val stores = _uiState.value.stores
            val sourceStore = stores.find { it.id == sourceStoreId }
            
            if (sourceStore != null) {
                onStoreChange(envelopeId, sourceStore)
            }
        }
    }

    fun clearMessages() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    private suspend fun getPendingEnvelope(userId: String): CardEnvelope? {
        val userEnvelopes = cardEnvelopeRepository.getCardEnvelopebyUser(userId)

        return userEnvelopes.firstOrNull { envelope ->
            envelope.status == "PENDING"
        }
    }



}