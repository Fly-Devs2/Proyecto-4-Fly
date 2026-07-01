package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun <T> Dropdown(
    selected: T?,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    val isPlaceholder = selected == null
    val text = selected?.let(label) ?: placeholder.orEmpty()
    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(8.dp)).background(BgCard)
                .clickable { expanded = true }.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                color = if (isPlaceholder) TextMuted else TextPrimary,
                fontSize = 13.sp,
            )
            Icon(FlyIconType.ChevronDown, Modifier.size(16.dp), TextMuted)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = BgSurface,
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option), color = TextPrimary, fontSize = 13.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors(textColor = TextPrimary),
                )
            }
        }
    }
}
