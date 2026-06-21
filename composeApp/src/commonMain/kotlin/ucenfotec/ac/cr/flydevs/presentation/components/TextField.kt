package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun TextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Int = 44,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().height(minHeight.dp).clip(RoundedCornerShape(8.dp)).background(BgCard),
        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        decorationBox = { inner ->
            Box(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
                if (value.isEmpty()) Text(placeholder, color = TextMuted, fontSize = 13.sp)
                inner()
            }
        },
    )
}
