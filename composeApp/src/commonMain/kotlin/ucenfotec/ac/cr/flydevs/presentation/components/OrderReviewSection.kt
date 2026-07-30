package ucenfotec.ac.cr.flydevs.presentation.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ucenfotec.ac.cr.flydevs.domain.model.ReviewEligibility
import ucenfotec.ac.cr.flydevs.domain.validation.ReviewValidator.MAX_REVIEW_COMMENT_LENGTH
import ucenfotec.ac.cr.flydevs.presentation.theme.*

import ucenfotec.ac.cr.flydevs.presentation.review.OrderReviewUiState
import java.awt.Color

@Composable
fun OrderReviewSection(
    uiState: OrderReviewUiState,
    reviewedUserName: String,
    onRatingChanged: (Double) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BgCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (uiState.isEditingReview) {
                    "Editar calificación"
                } else {
                    "Calificar"
                },
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Comparte tu experiencia con $reviewedUserName.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium
            )

            when {
                uiState.isLoading -> {
                    ReviewLoadingContent()
                }

                uiState.eligibility ==
                        ReviewEligibility.UserNotParticipant -> {
                    Text(
                        text = "No puedes calificar porque no participaste en este pedido.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                uiState.eligibility ==
                        ReviewEligibility.InvalidOrder -> {
                    Text(
                        text = uiState.errorMessage
                            ?: "No se pudo cargar la información del pedido.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    ReviewFormContent(
                        uiState = uiState,
                        onRatingChanged = onRatingChanged,
                        onCommentChanged = onCommentChanged,
                        onSaveClick = onSaveClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewLoadingContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator()

        Text(
            text = "Cargando calificación...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReviewFormContent(
    uiState: OrderReviewUiState,
    onRatingChanged: (Double) -> Unit,
    onCommentChanged: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val isEnabled =
        uiState.eligibility == ReviewEligibility.Available

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(
                    if (isEnabled) {
                        1f
                    } else {
                        0.45f
                    }
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HalfStarRating(
                rating = uiState.selectedRating,
                onRatingChanged = onRatingChanged,
                enabled = isEnabled && !uiState.isSaving,
                readOnly = false,
                starSize = 30.dp,
                activeColor = AccentGold,
                inactiveColor = TextMuted.copy(alpha = 0.45f),
                disabledColor = TextMuted.copy(alpha = 0.30f)
            )


            Text(
                text = ratingLabel(uiState.selectedRating),
                color = if (isEnabled) {
                    TextSecondary
                } else {
                    TextMuted
                },
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = uiState.comment,
                onValueChange = onCommentChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = isEnabled && !uiState.isSaving,
                label = {
                    Text("Comentario opcional")
                },
                placeholder = {
                    Text("Describe cómo fue tu experiencia.")
                },
                supportingText = {
                    Text(
                        text = "${uiState.comment.length}/" +
                                MAX_REVIEW_COMMENT_LENGTH
                    )
                },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledTextColor = TextMuted,

                    focusedContainerColor = BgSurface,
                    unfocusedContainerColor = BgSurface,
                    disabledContainerColor =
                        BgSurface.copy(alpha = 0.60f),

                    focusedBorderColor = AccentViolet,
                    unfocusedBorderColor = TextMuted,
                    disabledBorderColor =
                        TextMuted.copy(alpha = 0.40f),

                    focusedLabelColor = AccentVioletLight,
                    unfocusedLabelColor = TextSecondary,
                    disabledLabelColor = TextMuted,

                    focusedPlaceholderColor = TextMuted,
                    unfocusedPlaceholderColor = TextMuted,
                    disabledPlaceholderColor =
                        TextMuted.copy(alpha = 0.60f),

                    focusedSupportingTextColor = TextMuted,
                    unfocusedSupportingTextColor = TextMuted,
                    disabledSupportingTextColor =
                        TextMuted.copy(alpha = 0.60f),

                    cursorColor = AccentViolet
                )
            )

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled =
                    uiState.canSubmitReview &&
                            !uiState.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentViolet,
                    contentColor = BgDark,
                    disabledContainerColor = BgSurface,
                    disabledContentColor = TextMuted
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = BgDark,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (uiState.isEditingReview) {
                            "Actualizar calificación"
                        } else {
                            "Guardar calificación"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (
            uiState.eligibility ==
            ReviewEligibility.SinpeNotPaid
        ) {
            Surface(
                color = BgSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Debes completar el pago por SINPE " +
                            "para poder calificar.",
                    modifier = Modifier.padding(12.dp),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        uiState.successMessage?.let { message ->
            Text(
                text = message,
                color = AccentMint,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = AccentRed,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun ratingLabel(rating: Double): String {
    return if (rating < 1.0) {
        "Selecciona entre 1 y 5 estrellas."
    } else {
        "$rating de 5 estrellas"
    }
}