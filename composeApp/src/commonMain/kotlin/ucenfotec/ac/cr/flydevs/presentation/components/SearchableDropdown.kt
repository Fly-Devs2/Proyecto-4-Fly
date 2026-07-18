package ucenfotec.ac.cr.flydevs.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.collectLatest
import ucenfotec.ac.cr.flydevs.presentation.theme.*

@Composable
fun <T> SearchableDropdown(
    selected: T?,
    options: List<T>,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    var query by remember { mutableStateOf(value = selected?.let(label) ?: "") }
    var expanded by remember { mutableStateOf(value = false) }
    val interactionSource = remember { MutableInteractionSource() }
    val scrollState = rememberScrollState()

    // Update query if selected item changes from outside
    LaunchedEffect(selected) {
        query = selected?.let(label) ?: ""
    }

    // Expand when the text field is clicked/pressed
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            if (interaction is PressInteraction.Release) {
                expanded = true
            }
        }
    }

    val filteredOptions = remember(query, options, selected) {
        val selectedLabel = selected?.let(label)
        if (query.isEmpty() || (query == selectedLabel)) {
            options
        } else {
            options.filter { label(it).contains(query, ignoreCase = true) }
        }
    }

    Box(modifier.fillMaxWidth()) {
        // Styled Row to match regular Dropdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BgCard)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // BasicTextField for searching to avoid focus conflicts and Material 3 overhead
            BasicTextField(
                value = query,
                onValueChange = {
                    query = it
                    expanded = true
                },
                modifier = Modifier.weight(1f),
                textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
                cursorBrush = SolidColor(AccentViolet),
                singleLine = true,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty() && placeholder != null) {
                            Text(placeholder, color = TextMuted, fontSize = 13.sp)
                        }
                        innerTextField()
                    }
                }
            )
            Icon(
                type = FlyIconType.ChevronDown,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { expanded = !expanded },
                color = TextMuted
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = BgSurface,
            modifier = Modifier.heightIn(max = 200.dp),
            // CRITICAL: Prevents the menu from stealing focus and dismissing the keyboard
            properties = PopupProperties(focusable = false)
        ) {
            Box(modifier = Modifier.heightIn(max = 200.dp).width(IntrinsicSize.Max)) {
                Column(
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .padding(end = 12.dp) // Space for scrollbar
                ) {
                    filteredOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(label(option), color = TextPrimary, fontSize = 13.sp) },
                            onClick = {
                                onSelect(option)
                                query = label(option)
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(textColor = TextPrimary),
                        )
                    }
                }

                // Custom Dynamic Scrollbar implementation
                if (filteredOptions.size > 4) {
                    var trackHeight by remember { mutableStateOf(0f) }
                    
                    val scrollbarHeightFraction by remember(trackHeight) {
                        derivedStateOf {
                            val maxScroll = scrollState.maxValue.toFloat()
                            if (maxScroll > 0 && trackHeight > 0) {
                                // Ratio: Viewport (trackHeight) / Total Content (trackHeight + maxScroll)
                                (trackHeight / (maxScroll + trackHeight)).coerceIn(0.1f, 0.9f)
                            } else 0.3f
                        }
                    }

                    // Scrollbar track
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .width(4.dp)
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                            .onGloballyPositioned { trackHeight = it.size.height.toFloat() }
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                    ) {
                        if (trackHeight > 0) {
                            // Scrollbar thumb
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(scrollbarHeightFraction)
                                    .graphicsLayer {
                                        if (scrollState.maxValue > 0) {
                                            // The total track height available is size.height
                                            // The thumb height is size.height * scrollbarHeightFraction
                                            // Available travel is size.height - thumbHeight
                                            val thumbHeight = size.height
                                            val availableTravel = trackHeight - thumbHeight
                                            
                                            // Position = Ratio of current scroll * available travel
                                            translationY = (scrollState.value.toFloat() / scrollState.maxValue) * availableTravel
                                        }
                                    }
                                    .background(AccentViolet.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                            )
                        }
                    }
                }
            }
        }
    }
}
