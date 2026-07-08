package ucenfotec.ac.cr.flydevs.presentation.Envelopes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.repository.ICardEnvelopeRepository

class CardEnvelopesViewModel(
    private val cardEnvelopeRepository: ICardEnvelopeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CardEnvelopesUIState())
    val uiState: StateFlow<CardEnvelopesUIState> = _uiState.asStateFlow()

    fun loadPendingEnvelopes(userId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                if (userId.isBlank()) {
                    throw Exception("No se encontró el usuario autenticado.")
                }

                val pendingEnvelopes = cardEnvelopeRepository
                    .getCardEnvelopebyUser(userId)
                    .filter { envelope ->
                        envelope.status == "PENDING"
                    }
                    .sortedByDescending { envelope ->
                        envelope.createdAt
                    }

                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        envelopes = pendingEnvelopes
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "No se pudieron cargar los sobres."
                    )
                }
            }
        }
    }
    fun deleteEnvelope(
        envelopeId: String,
        userId: String
    ) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    deletingEnvelopeId = envelopeId,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                cardEnvelopeRepository.deleteEnvelope(envelopeId)

                val pendingEnvelopes = cardEnvelopeRepository
                    .getCardEnvelopebyUser(userId)
                    .filter { envelope ->
                        envelope.status == "PENDING"
                    }
                    .sortedByDescending { envelope ->
                        envelope.createdAt
                    }

                _uiState.update { currentState ->
                    currentState.copy(
                        deletingEnvelopeId = null,
                        envelopes = pendingEnvelopes,
                        successMessage = "Sobre eliminado correctamente."
                    )
                }
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        deletingEnvelopeId = null,
                        errorMessage = exception.message ?: "No se pudo eliminar el sobre."
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
}