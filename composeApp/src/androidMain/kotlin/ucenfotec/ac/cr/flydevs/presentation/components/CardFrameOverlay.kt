package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
internal fun CardFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val frame = cardFrameSize(size.width, size.height)
        val left = (size.width - frame.width) / 2f
        val top = (size.height - frame.height) / 2f
        val scrim = Color.Black.copy(alpha = 0.55f)

        drawRect(color = scrim, size = Size(size.width, top))
        drawRect(
            color = scrim,
            topLeft = Offset(0f, top + frame.height),
            size = Size(size.width, size.height - top - frame.height),
        )
        drawRect(color = scrim, topLeft = Offset(0f, top), size = Size(left, frame.height))
        drawRect(
            color = scrim,
            topLeft = Offset(left + frame.width, top),
            size = Size(left, frame.height),
        )

        // Borde del marco.
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(frame.width, frame.height),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}
