package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun QuantityStepper(
    quantity: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(8.dp)).background(BgCard),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(FlyIconType.Minus, Modifier.padding(horizontal = 10.dp).size(16.dp).clickable(onClick = onDecrease), TextSecondary)
        Text(
            text = quantity.toString(),
            modifier = Modifier.weight(1f),
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Icon(FlyIconType.Plus, Modifier.padding(horizontal = 10.dp).size(16.dp).clickable(onClick = onIncrease), TextSecondary)
    }
}
