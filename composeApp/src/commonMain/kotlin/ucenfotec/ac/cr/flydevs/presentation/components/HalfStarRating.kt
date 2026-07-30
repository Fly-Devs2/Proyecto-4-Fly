package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private const val MAX_STARS = 5
@Composable
fun HalfStarRating(
    rating: Double,
    onRatingChanged: (Double) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    starSize: Dp = 30.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline,
    disabledColor: Color = MaterialTheme.colorScheme.onSurface.copy(
        alpha = 0.30f
    )
) {
    val selectedColor = if (enabled) {
        activeColor
    } else {
        disabledColor
    }

    val emptyColor = if (enabled) {
        inactiveColor
    } else {
        disabledColor
    }

    Row(

        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(MAX_STARS) { index ->
            val starNumber = index + 1

            val fillFraction = getStarFillFraction(
                rating = rating,
                starNumber = starNumber
            )

            RatingStar(
                fillFraction = fillFraction,
                starNumber = starNumber,
                enabled = enabled,
                readOnly = readOnly,
                starSize = starSize,
                selectedColor = selectedColor,
                emptyColor = emptyColor,
                onRatingChanged = onRatingChanged
            )
        }
    }
}

@Composable
private fun RatingStar(
    fillFraction: Float,
    starNumber: Int,
    enabled: Boolean,
    readOnly: Boolean,
    starSize: Dp = 30.dp,
    selectedColor: Color,
    emptyColor: Color,
    onRatingChanged: (Double) -> Unit
) {
    val normalizedFill = fillFraction.coerceIn(
        minimumValue = 0f,
        maximumValue = 1f
    )

    Box(
        modifier = Modifier.size(20.dp),
        contentAlignment = Alignment.Center
    ) {
        /*
         * Estrella vacía de fondo.
         */
        Icon(
            imageVector = Icons.Filled.StarBorder,
            contentDescription = null,
            tint = emptyColor,
            modifier = Modifier.size(starSize)
        )

        /*
         * Estrella amarilla superpuesta.
         *
         * El ícono siempre conserva su tamaño completo.
         * drawWithContent muestra solamente el porcentaje
         * indicado por fillFraction.
         */
        if (normalizedFill > 0f) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = selectedColor,
                modifier = Modifier
                    .size(starSize)
                    .drawWithContent {
                        clipRect(
                            left = 0f,
                            top = 0f,
                            right = size.width * normalizedFill,
                            bottom = size.height
                        ) {
                            this@drawWithContent.drawContent()
                        }
                    }
            )
        }

        /*
         * Área izquierda: media estrella.
         * Área derecha: estrella completa.
         */
        if (enabled && !readOnly) {
            Row(
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                val newRating = if (starNumber == 1) {
                                    1.0
                                } else {
                                    starNumber - 0.5
                                }

                                onRatingChanged(newRating)
                            }
                        )
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember {
                                MutableInteractionSource()
                            },
                            indication = null,
                            role = Role.Button,
                            onClick = {
                                onRatingChanged(
                                    starNumber.toDouble()
                                )
                            }
                        )
                )
            }
        }
    }
}

private fun getStarFillFraction(
    rating: Double,
    starNumber: Int
): Float {
    val difference = rating - (starNumber - 1)

    return when {
        difference >= 1.0 -> 1f
        difference >= 0.5 -> 0.5f
        else -> 0f
    }
}














