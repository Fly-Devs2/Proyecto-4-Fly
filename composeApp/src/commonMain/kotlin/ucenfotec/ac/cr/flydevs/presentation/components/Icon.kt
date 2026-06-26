package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

enum class FlyIconType { Back, ChevronDown, Minus, Plus, Home, Compass, Package, Profile, Eye, EyeOff }

@Composable
fun Icon(
    type: FlyIconType,
    modifier: Modifier = Modifier,
    color: Color = TextPrimary,
) {
    Canvas(modifier) {
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        when (type) {
            FlyIconType.Back -> {
                // arrow-left: horizontal shaft + arrow head
                drawLine(color, Offset(w * .79f, h * .5f), Offset(w * .21f, h * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * .5f, h * .79f), Offset(w * .21f, h * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * .21f, h * .5f), Offset(w * .5f, h * .21f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlyIconType.ChevronDown -> {
                drawLine(color, Offset(w * .2f, h * .35f), Offset(w * .5f, h * .65f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * .5f, h * .65f), Offset(w * .8f, h * .35f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlyIconType.Minus -> drawLine(color, Offset(w * .2f, h * .5f), Offset(w * .8f, h * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            FlyIconType.Plus -> {
                drawLine(color, Offset(w * .2f, h * .5f), Offset(w * .8f, h * .5f), strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, Offset(w * .5f, h * .2f), Offset(w * .5f, h * .8f), strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlyIconType.Home -> {
                drawLine(color, Offset(w * .12f, h * .5f), Offset(w * .5f, h * .15f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * .5f, h * .15f), Offset(w * .88f, h * .5f), strokeWidth = stroke.width)
                drawRect(color, Offset(w * .25f, h * .46f), Size(w * .5f, h * .4f), style = stroke)
            }
            FlyIconType.Compass -> {
                drawCircle(color, w * .4f, Offset(w * .5f, h * .5f), style = stroke)
                val a = Offset(w * .677f, h * .323f)
                val b = Offset(w * .588f, h * .588f)
                val c = Offset(w * .323f, h * .677f)
                val d = Offset(w * .412f, h * .412f)
                drawLine(color, a, b, strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, b, c, strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, c, d, strokeWidth = stroke.width, cap = StrokeCap.Round)
                drawLine(color, d, a, strokeWidth = stroke.width, cap = StrokeCap.Round)
            }
            FlyIconType.Package -> {
                drawRect(color, Offset(w * .15f, h * .2f), Size(w * .7f, h * .65f), style = stroke)
                drawLine(color, Offset(w * .15f, h * .42f), Offset(w * .85f, h * .42f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * .5f, h * .2f), Offset(w * .5f, h * .42f), strokeWidth = stroke.width)
            }
            FlyIconType.Profile -> {
                drawCircle(color, w * .2f, Offset(w * .5f, h * .32f), style = stroke)
                drawArc(color, 200f, 140f, false, Offset(w * .18f, h * .48f), Size(w * .64f, h * .45f), style = stroke)
            }
            FlyIconType.Eye -> {
                drawArc(color, 210f, 120f, false, Offset(w * .1f, h * .25f), Size(w * .8f, h * .5f), style = stroke)
                drawArc(color, 30f, 120f, false, Offset(w * .1f, h * .25f), Size(w * .8f, h * .5f), style = stroke)
                drawCircle(color, w * .15f, Offset(w * .5f, h * .5f), style = stroke)
            }
            FlyIconType.EyeOff -> {
                drawArc(color, 210f, 120f, false, Offset(w * .1f, h * .25f), Size(w * .8f, h * .5f), style = stroke)
                drawArc(color, 30f, 120f, false, Offset(w * .1f, h * .25f), Size(w * .8f, h * .5f), style = stroke)
                drawCircle(color, w * .15f, Offset(w * .5f, h * .5f), style = stroke)
                drawLine(color, Offset(w * .2f, h * .2f), Offset(w * .8f, h * .8f), strokeWidth = stroke.width)
            }
        }
    }
}
