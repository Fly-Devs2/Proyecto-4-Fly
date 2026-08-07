# Scryfall API Integration for Card Prices and Versions

Integrate Scryfall API to display real-time market prices (USD, EUR, TIX) and different versions of a card in the `CardDetailScreen`.

## User Review Required

> [!IMPORTANT]
> - **Search Logic:** I will search Scryfall using the card name exactly (`!"name"`). This will return all prints of that card.
> - **UI Layout:** The section will be placed between `CardTagPills` and `SellerSection`.
> - **Titles:** The section title will be "Versiones" if multiple prints are found, or "Precio" if only one.
> - **Colors:** I will use `AccentVioletLight` for USD/EUR prices and `AccentGold` for TIX to match the project's theme while respecting the visual hierarchy of the original Scryfall layout.

## Proposed Changes

### [shared] Data Layer

#### [NEW] [ScryfallApiService.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/remote/ScryfallApiService.kt)
- Define Ktor service to fetch card data from `https://api.scryfall.com/cards/search`.
- Use query parameter `q=!"{name}"+unique:prints` to get all versions.

#### [NEW] [ScryfallDto.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/remote/dto/ScryfallDto.kt)
- Data classes for parsing Scryfall response (List, Card, Prices).

#### [NEW] [ScryfallRepositoryImpl.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/ScryfallRepositoryImpl.kt)
- Implementation of `IScryfallRepository`.
- Map DTOs to Domain models.

#### [DataModule.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/di/modules/DataModule.kt)
- Register `ScryfallApiService` and `IScryfallRepository` in Koin.

---

### [shared] Domain Layer

#### [NEW] [ScryfallCard.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/ScryfallCard.kt)
- Domain model for Scryfall card info (name, set, collector number, prices).

#### [NEW] [IScryfallRepository.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/repository/IScryfallRepository.kt)
- Interface for the repository.

---

### [shared] Presentation Layer

#### [CardDetailUiState.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/cardDetail/CardDetailUiState.kt)
- Add `scryfallVersions: List<ScryfallCard> = emptyList()`
- Add `isLoadingScryfall: Boolean = false`

#### [CardDetailViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/cardDetail/CardDetailViewModel.kt)
- Inject `IScryfallRepository`.
- Add `fetchScryfallData(cardName: String)` method.
- Call it within `loadCard`.

---

### [composeApp] UI

#### [CardDetailScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no%20Cuatrimestre/Proyecto%204/Fly%20App/Fly%20App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/CardDetailScreen.kt)
- Implement `ScryfallPricesSection` composable.
- Use a table-like layout with `Column` and `Row`.
- Header: `VERSIÓN`, `USD`, `EUR`, `TIX`.
- Respect project typography and colors (`BgCard`, `AccentGold`, `AccentVioletLight`).

---

## Verification Plan

### Automated Tests
- **Unit Test:** `ScryfallMappingTest.kt` to verify DTO to Domain mapping.
- **Command:** `./gradlew :shared:test`

### Manual Verification
1. Open a card detail screen (e.g., "Teenage Mutant Ninja Turtles Eternal").
2. Verify the "Versiones" / "Precio" section appears.
3. Verify prices match Scryfall data.
4. Verify the UI matches the project's design system.
5. Test with cards having multiple versions vs. one version.
6. Test with network offline (should handle error gracefully without crashing).
