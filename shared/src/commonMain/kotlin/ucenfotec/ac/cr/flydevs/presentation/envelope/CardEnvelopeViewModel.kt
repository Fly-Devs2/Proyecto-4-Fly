package ucenfotec.ac.cr.flydevs.presentation.envelope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.CardEnvelope
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository

class CardEnvelopeViewModel(
    private val cardEnvelopeRepository: ICardEnvelopeRepository
): ViewModel(){
    private val _uiState = MutableStateFlow(CardEnvelopeUiState())
    val uiState: StateFlow<CardEnvelopeUiState> = _uiState.asStateFlow()

    fun loadEnvelope(userId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                val envelope = getPendingEnvelope(userId)

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        envelope = envelope,
                        cards = envelope?.cards ?: emptyList()
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

    fun removeCardFromEnvelope(userId: String, cardId: String) {
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
                    userId = userId,
                    cardId = cardId
                )

                val updatedEnvelope = getPendingEnvelope(userId)

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

    fun generateOrderFromEnvelope(userId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isGeneratingOrder = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                cardEnvelopeRepository.generateOrderFromEnvelope(userId)

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