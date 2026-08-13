# Walkthrough - Display Store Tag in CardDetailScreen

I have updated the `CardDetailScreen` to display the source store name as a tag (pill) alongside other card attributes like expansion and rarity.

## Changes

### UI Layer (Compose App)

#### [CardDetailScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/CardDetailScreen.kt)

- **Updated `CardTagPills`**: Now accepts `sourceStoreName`. If a store name is provided, it is displayed as the first tag in the row.
- **Styling**: The store tag uses `AccentMint` (with 20% alpha background) to distinguish it from other tags, following the design system's convention for store-related information.
- **Refactoring**:
    - Replaced the manual tag creation in `CardTagPills` with a reusable `TagPill` composable.
    - Removed the redundant `StoreBadge` component and its call site.
    - Passed `state.sourceStoreName` from the ViewModel to the `CardTagPills` component.

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
```

## Verification Results

### Code Analysis
- Verified that the file compiles and follows the architectural patterns.
- Ensured `CardDetailViewModel` already correctly provides the `sourceStoreName` in the `UiState`.
- Removed dead code (`StoreBadge`).

### Manual Verification (Recommended)
1. Open a card detail view that has an associated store.
2. Observe the new mint-colored tag at the top of the tag list.
3. Confirm that the previous "TIENDA:" badge is no longer present.
