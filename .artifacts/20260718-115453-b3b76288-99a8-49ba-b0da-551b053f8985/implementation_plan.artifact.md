# Fix Scrollbar Bottom Alignment

The goal is to ensure the scrollbar thumb accurately reflects the scroll position and reaches the absolute bottom of the track when the user reaches the end of the list.

## User Review Required
- I am assuming the `200.dp` max height is the primary constraint. If the menu is shorter than `200.dp` (fewer items), the `trackHeight` will correctly capture the smaller size.

## Proposed Changes

### [UI Components]

#### [SearchableDropdown.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/components/SearchableDropdown.kt)

I will replace the existing scrollbar logic with a more robust version that uses the measured `trackHeight` as the anchor.

- **Source of Truth**: Use `onGloballyPositioned` on the track to get the precise pixel height of the available scrollbar area.
- **Pure Math**: Inside `graphicsLayer`, calculate the `translationY` by mapping the `scrollState.value` directly to the `trackHeight` minus the calculated thumb height.
- **Fixed Scaling**: Use a more stable calculation for the thumb height that doesn't rely on estimated fractions.

```kotlin
// Inside DropdownMenu
Box(modifier = Modifier.heightIn(max = 200.dp).width(IntrinsicSize.Max)) {
    // ... Column ...

    if (filteredOptions.size > 4) {
        var trackHeight by remember { mutableStateOf(0f) }

        // Use derivedStateOf for smooth, performant updates
        val scrollbarHeightFraction by remember(trackHeight) {
            derivedStateOf {
                val maxScroll = scrollState.maxValue.toFloat()
                if (maxScroll > 0 && trackHeight > 0) {
                    // Ratio: Viewport / Total Content
                    (trackHeight / (maxScroll + trackHeight)).coerceIn(0.1f, 0.9f)
                } else 0.3f
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(4.dp)
                .padding(vertical = 4.dp) // Visual padding from edges
                .onGloballyPositioned { trackHeight = it.size.height.toFloat() }
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
        ) {
            if (trackHeight > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(scrollbarHeightFraction)
                        .graphicsLayer {
                            if (scrollState.maxValue > 0) {
                                // Important: We use the track's height minus the thumb's height
                                // to get the "Available Travel Distance"
                                val thumbHeight = size.height // Current height of this thumb box
                                val availableTravel = trackHeight - thumbHeight

                                // Position = Percentage of scroll * travel distance
                                translationY = (scrollState.value.toFloat() / scrollState.maxValue) * availableTravel
                            }
                        }
                        .background(AccentViolet.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
```

## Verification Plan

### Manual Verification
- **Bottom Check**: Scroll to the last item in the "Tipo de juego" list and verify the purple thumb is flush with the bottom of the track (accounting for the 4dp padding).
- **Top Check**: Verify the thumb starts exactly at the top.
- **Mid Check**: Verify the thumb is roughly in the middle when the scroll is in the middle.
- **Dynamic Content**: Filter the list so the number of items changes, and verify the thumb size updates and remains accurate.
