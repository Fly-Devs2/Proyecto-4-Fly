package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun PhotoUploadZone(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Subir fotos de la carta",
    subtitle: String = "JPG o PNG · máx. 5 MB",
    accentColor: Color = AccentViolet,
    titleColor: Color = TextSecondary,
    subtitleColor: Color = TextMuted,
) {
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = accentColor.copy(alpha = .6f),
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(6.dp.toPx(), 6.dp.toPx()),
                        ),
                    ),
                )
            }
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CameraIcon(Modifier.size(32.dp), accentColor)
        Text(title, color = titleColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Text(subtitle, color = subtitleColor, fontSize = 11.sp)
    }
}

@Composable
private fun CameraIcon(modifier: Modifier, color: Color) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        // body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * .06f, h * .30f),
            size = Size(w * .88f, h * .52f),
            cornerRadius = CornerRadius(w * .1f),
            style = stroke,
        )

        drawLine(color, Offset(w * .33f, h * .30f), Offset(w * .4f, h * .16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(w * .4f, h * .16f), Offset(w * .6f, h * .16f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawLine(color, Offset(w * .6f, h * .16f), Offset(w * .67f, h * .30f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(color, w * .16f, Offset(w * .5f, h * .56f), style = stroke)
    }
}
