package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import ucenfotec.ac.cr.flydevs.domain.model.ReputationReviewItem
import ucenfotec.ac.cr.flydevs.domain.model.ReviewRole
import ucenfotec.ac.cr.flydevs.domain.model.RoleReputationSummary
import ucenfotec.ac.cr.flydevs.presentation.components.HalfStarRating
import ucenfotec.ac.cr.flydevs.presentation.reputation.ReputationUiState
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentGold
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentRed
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentViolet
import ucenfotec.ac.cr.flydevs.presentation.theme.AccentVioletLight
import ucenfotec.ac.cr.flydevs.presentation.theme.BgCard
import ucenfotec.ac.cr.flydevs.presentation.theme.BgSurface
import ucenfotec.ac.cr.flydevs.presentation.theme.TextMuted
import ucenfotec.ac.cr.flydevs.presentation.theme.TextPrimary
import ucenfotec.ac.cr.flydevs.presentation.theme.TextSecondary

@Composable
fun ReputationContent(
    state: ReputationUiState,
    onRoleSelected: (ReviewRole) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    showIdentity: Boolean = true
) {
    if (state.isLoading) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = AccentViolet
            )
        }

        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "REPUTACIÓN",
            color = AccentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        if (showIdentity) {
            ReputationIdentity(
                userName = state.userName,
                userInitial = state.userInitial
            )
        }

        ReputationRoleSelector(
            selectedRole = state.selectedRole,
            onRoleSelected = onRoleSelected
        )

        ReputationSummaryCard(
            role = state.selectedRole,
            summary = state.roleSummary
        )

        RatingDistribution(
            summary = state.roleSummary
        )

        ReviewsSection(
            state = state,
            onLoadMore = onLoadMore
        )

        state.errorMessage?.let { message ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgCard)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = message,
                    color = AccentRed,
                    fontSize = 13.sp
                )

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentViolet
                    )
                ) {
                    Text(
                        text = "Reintentar",
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ReputationIdentity(
    userName: String,
    userInitial: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(BgSurface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInitial,
                color = AccentVioletLight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column {
            Text(
                text = userName.ifBlank {
                    "Usuario"
                },
                color = TextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Perfil de reputación",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ReputationRoleSelector(
    selectedRole: ReviewRole,
    onRoleSelected: (ReviewRole) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgSurface)
            .padding(4.dp)
    ) {
        ReputationRoleTab(
            text = "Como vendedor",
            selected = selectedRole == ReviewRole.SELLER,
            onClick = {
                onRoleSelected(ReviewRole.SELLER)
            },
            modifier = Modifier.weight(1f)
        )

        ReputationRoleTab(
            text = "Como comprador",
            selected = selectedRole == ReviewRole.BUYER,
            onClick = {
                onRoleSelected(ReviewRole.BUYER)
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ReputationRoleTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(
                if (selected) {
                    AccentViolet
                } else {
                    BgSurface
                }
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 10.dp,
                vertical = 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) {
                TextPrimary
            } else {
                TextSecondary
            },
            fontSize = 13.sp,
            fontWeight = if (selected) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            }
        )
    }
}

@Composable
private fun ReputationSummaryCard(
    role: ReviewRole,
    summary: RoleReputationSummary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formatRating(
                        summary.averageRating
                    ),
                    color = TextPrimary,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (role == ReviewRole.SELLER) {
                        "Calificación como vendedor"
                    } else {
                        "Calificación como comprador"
                    },
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                HalfStarRating(
                    rating = summary.averageRating,
                    onRatingChanged = {},
                    enabled = true,
                    readOnly = true,
                    starSize = 22.dp,
                    activeColor = AccentGold,
                    inactiveColor =
                        TextMuted.copy(alpha = 0.55f),
                    disabledColor =
                        TextMuted.copy(alpha = 0.30f)
                )

                Text(
                    text = pluralize(
                        count = summary.reviewCount,
                        singular = "reseña",
                        plural = "reseñas"
                    ),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            ReputationStatistic(
                value = summary.reviewCount,
                label = "Reseñas",
                modifier = Modifier.weight(1f)
            )

            ReputationStatistic(
                value = summary.commentCount,
                label = "Comentarios",
                modifier = Modifier.weight(1f)
            )

            ReputationStatistic(
                value =
                    summary.completedTransactionCount,
                label = if (role == ReviewRole.SELLER) {
                    "Ventas"
                } else {
                    "Compras"
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ReputationStatistic(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgSurface)
            .padding(
                horizontal = 6.dp,
                vertical = 12.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            color = TextPrimary,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RatingDistribution(
    summary: RoleReputationSummary
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Text(
            text = "DISTRIBUCIÓN DE CALIFICACIONES",
            color = AccentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        for (stars in 5 downTo 1) {
            val count =
                summary.countForStars(stars)

            val progress =
                if (summary.reviewCount == 0) {
                    0f
                } else {
                    count.toFloat() /
                            summary.reviewCount.toFloat()
                }

            RatingDistributionRow(
                stars = stars,
                count = count,
                progress = progress
            )
        }
    }
}

@Composable
private fun RatingDistributionRow(
    stars: Int,
    count: Int,
    progress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stars.toString(),
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.size(
                width = 12.dp,
                height = 18.dp
            )
        )

        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(15.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(100))
                .background(BgSurface)
        ) {
            if (progress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(
                            progress.coerceIn(
                                0f,
                                1f
                            )
                        )
                        .fillMaxHeight()
                        .clip(
                            RoundedCornerShape(100)
                        )
                        .background(AccentGold)
                )
            }
        }

        Text(
            text = count.toString(),
            color = TextMuted,
            fontSize = 12.sp,
            modifier = Modifier.size(
                width = 24.dp,
                height = 18.dp
            )
        )
    }
}

@Composable
private fun ReviewsSection(
    state: ReputationUiState,
    onLoadMore: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "RESEÑAS Y COMENTARIOS",
            color = AccentGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )

        if (
            state.isLoadingReviews &&
            state.reviews.isEmpty()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AccentViolet,
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
            }

            return
        }

        if (state.reviews.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgCard)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (
                        state.selectedRole ==
                        ReviewRole.SELLER
                    ) {
                        "Este usuario todavía no tiene reseñas como vendedor."
                    } else {
                        "Este usuario todavía no tiene reseñas como comprador."
                    },
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            return
        }

        state.reviews.forEach { reviewItem ->
            ReputationReviewCard(
                item = reviewItem
            )
        }

        if (state.hasMoreReviews) {
            Button(
                onClick = onLoadMore,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoadingReviews,
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BgSurface,
                    contentColor = TextPrimary
                )
            ) {
                if (state.isLoadingReviews) {
                    CircularProgressIndicator(
                        color = AccentViolet,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Ver más",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReputationReviewCard(
    item: ReputationReviewItem
) {
    val review = item.review

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(BgSurface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.reviewerInitial,
                    color = AccentVioletLight,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                modifier = Modifier.size(12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.reviewerName,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = when (review.reviewerRole) {
                        ReviewRole.BUYER ->
                            "Comprador"

                        ReviewRole.SELLER ->
                            "Vendedor"
                    },
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Text(
                text = formatRating(review.rating),
                color = AccentGold,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        HalfStarRating(
            rating = review.rating,
            onRatingChanged = {},
            enabled = true,
            readOnly = true,
            starSize = 19.dp,
            activeColor = AccentGold,
            inactiveColor =
                TextMuted.copy(alpha = 0.50f),
            disabledColor =
                TextMuted.copy(alpha = 0.25f)
        )

        Text(
            text = review.comment.ifBlank {
                "Sin comentario"
            },
            color = if (review.comment.isBlank()) {
                TextMuted
            } else {
                TextSecondary
            },
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

private fun formatRating(
    rating: Double
): String {
    val rounded =
        (rating * 10).roundToInt() / 10.0

    return if (rounded % 1.0 == 0.0) {
        "${rounded.toInt()}.0"
    } else {
        rounded.toString()
    }
}

private fun pluralize(
    count: Int,
    singular: String,
    plural: String
): String {
    return if (count == 1) {
        "$count $singular"
    } else {
        "$count $plural"
    }
}