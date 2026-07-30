package ucenfotec.ac.cr.flydevs.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ucenfotec.ac.cr.flydevs.domain.model.ReviewEligibility
import ucenfotec.ac.cr.flydevs.domain.repository.IReviewRepository
import ucenfotec.ac.cr.flydevs.domain.validation.ReviewValidator.MAX_REVIEW_COMMENT_LENGTH
import ucenfotec.ac.cr.flydevs.domain.validation.ReviewValidator.isValidReviewRating

class OrderReviewViewModel(
    private val reviewRepository: IReviewRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(OrderReviewUiState())

    val uiState: StateFlow<OrderReviewUiState> =
        _uiState.asStateFlow()

    private var reviewObserverJob: Job? = null

    private var currentOrderId: String? = null


    fun loadReview(
        orderId: String
    ) {
        if (orderId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    eligibility = ReviewEligibility.InvalidOrder,
                    errorMessage =
                        "No se pudo identificar el pedido."
                )
            }
            return
        }

        currentOrderId = orderId
        reviewObserverJob?.cancel()

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val eligibility =
                    reviewRepository.getReviewEligibility(
                        orderId = orderId
                    )

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        eligibility = eligibility,
                        errorMessage = null
                    )
                }

                if (
                    eligibility ==
                    ReviewEligibility.Available
                ) {
                    observeExistingReview(
                        orderId = orderId
                    )
                } else {
                    reviewObserverJob?.cancel()

                    _uiState.update {
                        it.copy(
                            existingReview = null,
                            selectedRating = 0.0,
                            comment = "",
                            errorMessage = null
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        eligibility =
                            ReviewEligibility.InvalidOrder,
                        errorMessage =
                            exception.message
                                ?: "No se pudo cargar la calificación."
                    )
                }
            }
        }
    }



    private fun loadEligibility(
        orderId: String,

    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val eligibility =
                reviewRepository.getReviewEligibility(
                    orderId = orderId,

                )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    eligibility = eligibility
                )
            }
        }
    }

    private fun observeExistingReview(
        orderId: String
    ) {
        reviewObserverJob?.cancel()

        reviewObserverJob =
            viewModelScope.launch {
                reviewRepository
                    .observeReview(orderId)
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage =
                                    exception.message
                                        ?: "No se pudo consultar la reseña."
                            )
                        }
                    }
                    .collect { review ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                existingReview = review,
                                selectedRating =
                                    review?.rating ?: 0.0,
                                comment =
                                    review?.comment.orEmpty(),
                                errorMessage = null
                            )
                        }
                    }
            }
    }

    fun onRatingChanged(rating: Double) {
        if (_uiState.value.eligibility !=
            ReviewEligibility.Available
        ) {
            return
        }

        if (!rating.isValidReviewRating()) {
            return
        }

        _uiState.update {
            it.copy(
                selectedRating = rating,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onCommentChanged(comment: String) {
        if (_uiState.value.eligibility !=
            ReviewEligibility.Available
        ) {
            return
        }

        if (comment.length > MAX_REVIEW_COMMENT_LENGTH) {
            return
        }

        _uiState.update {
            it.copy(
                comment = comment,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun saveReview() {
        val orderId = currentOrderId
        val currentState = _uiState.value

        if (orderId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "No se pudo identificar el pedido."
                )
            }
            return
        }

        if (
            currentState.eligibility !=
            ReviewEligibility.Available
        ) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Debes completar el pago por SINPE " +
                                "para calificar."
                )
            }
            return
        }

        if (
            !currentState.selectedRating
                .isValidReviewRating()
        ) {
            _uiState.update {
                it.copy(
                    errorMessage =
                        "Selecciona una calificación válida."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    successMessage = null,
                    errorMessage = null
                )
            }

            val result =
                reviewRepository.saveReview(
                    orderId = orderId,
                    rating = currentState.selectedRating,
                    comment = currentState.comment,
                    existingCreatedAt = currentState.existingReview?.createdAt
                )

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage =
                                if (
                                    currentState.existingReview == null
                                ) {
                                    "Calificación enviada correctamente."
                                } else {
                                    "Calificación actualizada correctamente."
                                },
                            errorMessage = null
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            successMessage = null,
                            errorMessage =
                                exception.message
                                    ?: "No se pudo guardar la calificación."
                        )
                    }
                }
            )
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }
}