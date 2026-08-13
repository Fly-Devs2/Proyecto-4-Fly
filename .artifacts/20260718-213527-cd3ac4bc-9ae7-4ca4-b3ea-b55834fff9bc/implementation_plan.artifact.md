# Implementation Plan - Display Store Tag in CardDetailScreen

This plan outlines the changes to display the source store name as a tag (pill) within the `CardDetailScreen`, consistent with other card attributes.

## Proposed Changes

### UI Layer (Compose App)

#### [CardDetailScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/CardDetailScreen.kt)

- Update `CardTagPills` to accept `sourceStoreName: String? = null`.
- Modify `CardTagPills` to include a specific tag for the store if `sourceStoreName` is provided. This tag will use the `AccentMint` color for its background to align with the design system for store-related items.
- Remove the call to `StoreBadge` and the `StoreBadge` composable itself if it's no longer used.
- Update the call to `CardTagPills` in the main layout to pass `state.sourceStoreName`.

```kotlin
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardTagPills(card: GameCard, sourceStoreName: String? = null) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sourceStoreName?.let { name ->
             TagPill(text = name, backgroundColor = AccentMint.copy(alpha = 0.2f), textColor = AccentMint)
        }
        card.expansion?.let { TagPill(it) }
        card.rarity?.let { TagPill(it) }
        TagPill(card.condition.label)
        TagPill(card.language.label)
    }
}

@Composable
private fun TagPill(
    text: String,
    backgroundColor: Color = BgSurface,
    textColor: Color = TextSecondary
) {
    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    )
}
```

## Verification Plan

### Manual Verification
1.  Navigate to the `CardDetailScreen` for a card that has a source store assigned.
2.  Verify that a tag with the store name appears below the card name and ID.
3.  Verify the tag has the correct styling (e.g. `AccentMint` highlights).
4.  Verify that the old `StoreBadge` is no longer visible.
