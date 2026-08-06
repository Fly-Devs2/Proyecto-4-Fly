package ucenfotec.ac.cr.flydevs.presentation.AdminIncidentDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.INCIDENT_UPDATE_MAX_LENGTH
import ucenfotec.ac.cr.flydevs.domain.model.IncidentPriority
import ucenfotec.ac.cr.flydevs.domain.model.IncidentStatus
import ucenfotec.ac.cr.flydevs.domain.model.IncidentUpdate
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IIncidentRepository
import ucenfotec.ac.cr.flydevs.domain.repository.IOrderRepository

class AdminIncidentDetailViewModel(
    private val incidentRepository: IIncidentRepository,
    private val orderRepository: IOrderRepository,
    private val authRepository: IAuthRepository,
    private val incidentId: String
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            AdminIncidentDetailUiState(
                incidentId = incidentId
            )
        )

    val uiState: StateFlow<AdminIncidentDetailUiState> =
        _uiState.asStateFlow()

    private var incidentJob: Job? = null
    private var orderJob: Job? = null
    private var updatesJob: Job? = null
    private var adminJob: Job? = null

    init {
        loadAdminIdentity()
        observeDetail()
    }

    fun onNoteTextChange(
        value: String
    ) {
        _uiState.update {
            it.copy(
                noteText =
                    value.take(
                        INCIDENT_UPDATE_MAX_LENGTH
                    ),
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onInternalNoteChange(
        isInternal: Boolean
    ) {
        _uiState.update {
            it.copy(
                isInternalNote = isInternal
            )
        }
    }

    fun updateStatus(
        status: IncidentStatus
    ) {
        if (
            status == IncidentStatus.URGENT ||
            _uiState.value.isUpdatingStatus
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingStatus = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                incidentRepository
                    .updateIncidentStatus(
                        incidentId = incidentId,
                        status = status
                    )

                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        successMessage =
                            "Estado actualizado correctamente."
                    )
                }
            } catch (exception: Exception) {
                println(
                    "ADMIN_INCIDENT_DETAIL_ERROR | " +
                            "No se pudo actualizar el estado: " +
                            exception.message
                )

                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        errorMessage =
                            exception.message
                                ?: "No se pudo actualizar el estado."
                    )
                }
            }
        }
    }

    fun updatePriority(
        priority: IncidentPriority
    ) {
        if (_uiState.value.isUpdatingPriority) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUpdatingPriority = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                incidentRepository
                    .updateIncidentPriority(
                        incidentId = incidentId,
                        priority = priority
                    )

                _uiState.update {
                    it.copy(
                        isUpdatingPriority = false,
                        successMessage =
                            "Prioridad actualizada correctamente."
                    )
                }
            } catch (exception: Exception) {
                println(
                    "ADMIN_INCIDENT_DETAIL_ERROR | " +
                            "No se pudo actualizar la prioridad: " +
                            exception.message
                )

                _uiState.update {
                    it.copy(
                        isUpdatingPriority = false,
                        errorMessage =
                            exception.message
                                ?: "No se pudo actualizar la prioridad."
                    )
                }
            }
        }
    }

    fun addNote() {
        val currentState = _uiState.value
        val message = currentState.noteText.trim()

        if (currentState.isAddingNote) {
            return
        }

        if (message.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Escribe una nota de seguimiento."
                )
            }

            return
        }

        if (currentState.adminId.isBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "No se pudo identificar al administrador."
                )
            }

            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAddingNote = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                incidentRepository.addIncidentUpdate(
                    IncidentUpdate(
                        incidentId = incidentId,
                        authorId = currentState.adminId,
                        authorName =
                            currentState.adminName.ifBlank {
                                "Administrador"
                            },
                        authorRole = UserRole.ADMIN.name,
                        message = message,
                        isInternal =
                            currentState.isInternalNote
                    )
                )

                _uiState.update {
                    it.copy(
                        noteText = "",
                        isAddingNote = false,
                        successMessage =
                            "Nota agregada correctamente."
                    )
                }
            } catch (exception: Exception) {
                println(
                    "ADMIN_INCIDENT_DETAIL_ERROR | " +
                            "No se pudo agregar la nota: " +
                            exception.message
                )

                _uiState.update {
                    it.copy(
                        isAddingNote = false,
                        errorMessage =
                            exception.message
                                ?: "No se pudo agregar la nota."
                    )
                }
            }
        }
    }

    fun resolveIncident() {
        val currentState = _uiState.value

        if (
            currentState.isResolving ||
            currentState.isResolved
        ) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isResolving = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            try {
                incidentRepository.resolveIncident(
                    incidentId = incidentId
                )

                _uiState.update {
                    it.copy(
                        isResolving = false,
                        successMessage =
                            "La incidencia fue resuelta."
                    )
                }
            } catch (exception: Exception) {
                println(
                    "ADMIN_INCIDENT_DETAIL_ERROR | " +
                            "No se pudo resolver la incidencia: " +
                            exception.message
                )

                _uiState.update {
                    it.copy(
                        isResolving = false,
                        errorMessage =
                            exception.message
                                ?: "No se pudo resolver la incidencia."
                    )
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun retry() {
        observeDetail()
    }

    private fun loadAdminIdentity() {
        adminJob?.cancel()

        adminJob = viewModelScope.launch {
            val adminId =
                authRepository
                    .getCurrentUserUid()
                    .orEmpty()

            if (adminId.isBlank()) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            "No se encontró una sesión activa."
                    )
                }

                return@launch
            }

            val adminProfile =
                runCatching {
                    authRepository.getUserProfile(
                        adminId
                    )
                }
                    .onFailure { exception ->
                        println(
                            "ADMIN_INCIDENT_DETAIL_ERROR | " +
                                    "No se pudo obtener el perfil: " +
                                    exception.message
                        )
                    }
                    .getOrNull()

            _uiState.update {
                it.copy(
                    adminId = adminId,
                    adminName =
                        adminProfile
                            ?.name
                            ?.trim()
                            .orEmpty()
                            .ifBlank {
                                "Administrador"
                            }
                )
            }
        }
    }

    private fun observeDetail() {
        incidentJob?.cancel()
        orderJob?.cancel()
        updatesJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        incidentJob = viewModelScope.launch {
            incidentRepository
                .observeIncident(incidentId)
                .catch { exception ->
                    println(
                        "ADMIN_INCIDENT_DETAIL_ERROR | " +
                                "No se pudo cargar la incidencia: " +
                                exception.message
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage =
                                exception.message
                                    ?: "No se pudo cargar la incidencia."
                        )
                    }
                }
                .collectLatest { incident ->
                    _uiState.update {
                        it.copy(
                            incident = incident,
                            isLoading = false,
                            errorMessage =
                                if (incident == null) {
                                    "La incidencia no existe."
                                } else {
                                    null
                                }
                        )
                    }

                    observeOrder(
                        orderId =
                            incident
                                ?.orderId
                                .orEmpty()
                    )
                }
        }

        updatesJob = viewModelScope.launch {
            incidentRepository
                .observeIncidentUpdates(incidentId)
                .catch { exception ->
                    println(
                        "ADMIN_INCIDENT_DETAIL_ERROR | " +
                                "No se pudieron cargar las notas: " +
                                exception.message
                    )

                    _uiState.update {
                        it.copy(
                            updates = emptyList(),
                            errorMessage =
                                exception.message
                                    ?: "No se pudieron cargar las notas."
                        )
                    }
                }
                .collectLatest { updates ->
                    _uiState.update {
                        it.copy(
                            updates = updates
                        )
                    }
                }
        }
    }

    private fun observeOrder(
        orderId: String
    ) {
        orderJob?.cancel()

        if (orderId.isBlank()) {
            _uiState.update {
                it.copy(
                    order = null
                )
            }

            return
        }

        orderJob = viewModelScope.launch {
            orderRepository
                .getOrder(orderId)
                .catch { exception ->
                    println(
                        "ADMIN_INCIDENT_DETAIL_ERROR | " +
                                "No se pudo cargar la orden: " +
                                exception.message
                    )

                    _uiState.update {
                        it.copy(
                            order = null,
                            errorMessage =
                                "No se pudo cargar la orden relacionada."
                        )
                    }
                }
                .collectLatest { order ->
                    _uiState.update {
                        it.copy(
                            order = order
                        )
                    }
                }
        }
    }

    override fun onCleared() {
        incidentJob?.cancel()
        orderJob?.cancel()
        updatesJob?.cancel()
        adminJob?.cancel()

        super.onCleared()
    }
}