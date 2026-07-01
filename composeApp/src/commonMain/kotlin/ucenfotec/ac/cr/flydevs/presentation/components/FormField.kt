package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun FormField(
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            if (required) {
                Text("*", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
        content()
    }
}
