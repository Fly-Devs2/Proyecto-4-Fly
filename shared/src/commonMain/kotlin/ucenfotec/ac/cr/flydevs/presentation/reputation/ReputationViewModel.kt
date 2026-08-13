package ucenfotec.ac.cr.flydevs.presentation.reputation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.ReputationTimeframe
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.repository.IReputationRepository

class ReputationViewModel(
    private val reputationRepository:
    IReputationRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(ReputationUiState())

    val uiState: StateFlow<ReputationUiState> =
        _uiState

    private var observedUserId: String? = null

    private var headerJob: Job? = null
    private var reviewsJob: Job? = null

    fun loadReputation(
        userId: String
    ) {
        if (userId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage =
                        "No se pudo identificar al usuario."
                )
            }

            return
        }

        /*
         * Evita volver a iniciar los listeners
         * durante cada recomposición.
         */
        if (observedUserId == userId) {
            return
        }

        observedUserId = userId

        headerJob?.cancel()
        reviewsJob?.cancel()

        _uiState.value =
            ReputationUiState(
                userId = userId
            )

        observeHeader(
            userId = userId
        )

        loadReviews(
            clearCurrentReviews = true
        )
    }

    fun selectRole(
        role: ReviewRole
    ) {
        if (_uiState.value.selectedRole == role) {
            return
        }

        _uiState.update {
            it.copy(
                selectedRole = role,
                visibleReviewLimit = 3,
                reviews = emptyList(),
                hasMoreReviews = false,
                errorMessage = null
            )
        }

        loadReviews(
            clearCurrentReviews = true
        )
    }

    fun selectTimeframe(
        timeframe: ReputationTimeframe
    ) {
        if (_uiState.value.selectedTimeframe == timeframe) {
            return
        }

        _uiState.update {
            it.copy(
                selectedTimeframe = timeframe,
                visibleReviewLimit = 3,
                reviews = emptyList(),
                hasMoreReviews = false,
                errorMessage = null
            )
        }

        loadReviews(
            clearCurrentReviews = true
        )
    }

    fun loadMoreReviews() {
        val currentState =
            _uiState.value

        if (
            currentState.isLoadingReviews ||
            !currentState.hasMoreReviews
        ) {
            return
        }

        _uiState.update {
            it.copy(
                visibleReviewLimit =
                    it.visibleReviewLimit + 3
            )
        }

        loadReviews(
            clearCurrentReviews = false
        )
    }

    fun retry() {
        val userId =
            observedUserId ?: return

        /*
         * Reinicia completamente la carga.
         */
        observedUserId = null

        loadReputation(
            userId = userId
        )
    }

    private fun observeHeader(
        userId: String
    ) {
        headerJob =
            viewModelScope.launch {
                combine(
                    reputationRepository
                        .observeUser(userId),

                    reputationRepository
                        .observeRatingSummary(userId)
                ) { user, summary ->
                    user to summary
                }
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    exception.message
                                        ?: "No se pudo cargar la reputación."
                            )
                        }
                    }
                    .collect { (user, summary) ->
                        val currentRole = _uiState.value.selectedRole
                        
                        // Intelligent role selection on first load:
                        // If current role has no data but other role does, switch to it.
                        val newRole = if (observedUserId != null && _uiState.value.isLoading) {
                            val sellerHasData = summary.allTime.sellerReviewCount > 0 || summary.allTime.sellerCompletedTransactionCount > 0
                            val buyerHasData = summary.allTime.buyerReviewCount > 0 || summary.allTime.buyerCompletedTransactionCount > 0
                            
                            if (!sellerHasData && buyerHasData) ReviewRole.BUYER
                            else ReviewRole.SELLER
                        } else {
                            currentRole
                        }

                        _uiState.update {
                            it.copy(
                                userName =
                                    user?.name
                                        ?.trim()
                                        .orEmpty(),

                                ratingSummary =
                                    summary,
                                
                                selectedRole = newRole,

                                isLoading = false,
                                errorMessage = null
                            )
                        }

                        if (newRole != currentRole) {
                            loadReviews(clearCurrentReviews = true)
                        }
                    }
            }
    }

    private fun loadReviews(
        clearCurrentReviews: Boolean
    ) {
        val currentState =
            _uiState.value

        val userId =
            currentState.userId

        if (userId.isBlank()) {
            return
        }

        reviewsJob?.cancel()

        reviewsJob =
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        isLoadingReviews = true,
                        reviews =
                            if (clearCurrentReviews) {
                                emptyList()
                            } else {
                                it.reviews
                            },
                        errorMessage = null
                    )
                }

                try {
                    val latestState =
                        _uiState.value

                    val page =
                        reputationRepository.getReviews(
                            userId = userId,
                            role =
                                latestState.selectedRole,
                            limit =
                                latestState.visibleReviewLimit,
                            timeframe =
                                latestState.selectedTimeframe
                        )

                    _uiState.update {
                        it.copy(
                            reviews = page.reviews,
                            hasMoreReviews =
                                page.hasMore,
                            isLoadingReviews = false,
                            errorMessage = null
                        )
                    }
                } catch (exception: Exception) {
                    println(
                        "REPUTATION_DEBUG | " +
                                "Loading reviews failed: " +
                                exception.message
                    )

                    exception.printStackTrace()

                    _uiState.update {
                        it.copy(
                            isLoadingReviews = false,
                            errorMessage =
                                exception.message
                                    ?: "No se pudieron cargar las reseñas."
                        )
                    }
                }
            }
    }

    override fun onCleared() {
        headerJob?.cancel()
        reviewsJob?.cancel()
        super.onCleared()
    }
}