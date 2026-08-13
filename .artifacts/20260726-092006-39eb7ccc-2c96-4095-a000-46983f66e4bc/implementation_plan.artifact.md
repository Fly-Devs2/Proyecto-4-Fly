# Global Transaction Count and Time-Based Reputation Filters

This plan implements a global transaction count for each user (combining purchases and sales) and adds time-based filters (Last 30 Days, Last Year, All) to the reputation view.

## User Review Required

> [!IMPORTANT]
> - **Transaction Definition**: A "Transaction" is counted if the order is SINPE paid **AND** seller delivery evidence exists. I will update the backend trigger to reflect this logic.
> - **Time-Based Summary**: The `UserRatingSummary` document in Firestore will now store multiple summaries (e.g., `last30Days`, `lastYear`, `allTime`) to support efficient filtering without recalculating on the fly from the mobile app.

## Proposed Changes

### Domain & Data Layer

#### [UserRatingSummary.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/UserRatingSummary.kt)
- Add `totalCompletedTransactionCount` (Global sum).
- Refactor to include nested summaries for timeframes: `last30Days`, `lastYear`, and `allTime`.

#### [RoleReputationSummary.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/RoleReputationSummary.kt)
- Add `totalCompletedTransactionCount` to the summary.

#### [IReputationRepository.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/repository/IReputationRepository.kt)
- Update `getReviews` to accept a `timeframe` parameter for filtering.

---

### Backend (Cloud Functions)

#### [updateOrderReputation.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/reviews/updateOrderReputation.ts)
- Update criteria for "Completed Transaction": `order.sinpePaid === true && order.sellerEvidenceUrls.length > 0`.
- Update `recalculateCompletedTransactions` to calculate both role-specific and global counts.
- **New Logic**: Calculate counts for the 3 timeframes (30d, 1y, All).

#### [updateUserRatingSummary.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/reviews/updateUserRatingSummary.ts)
- Update `recalculateUserRating` to update the nested timeframe summaries (Average rating, review count, distribution) for the 3 periods.

---

### Presentation Layer (Shared)

#### [ReputationUiState.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/reputation/ReputationUiState.kt)
- Add `selectedTimeframe` enum (LAST_30_DAYS, LAST_YEAR, ALL).
- Update `roleSummary` getter to return the summary corresponding to the selected timeframe.

#### [ReputationViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/reputation/ReputationViewModel.kt)
- Add `setTimeframe(timeframe: ReputationTimeframe)` method.
- Update `loadReviews` to pass the timeframe to the repository.

---

### UI Layer (Compose)

#### [ReputationContent.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/components/ReputationContent.kt)
- **New Component**: `ReputationTimeframeSelector` (segmented control or chips).
- **ReputationSummaryCard**: Display the "Global Transactions" count (Compras + Ventas) in a prominent way.
- Ensure pluralization logic is applied correctly ("1 transacción" vs "X transacciones").

#### [ProfileScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ProfileScreen.kt)
- Display the global transaction count in the profile header or statistics section.

## Verification Plan

### Automated Tests
- Run `shared:assemble` to verify compilation.
- **Backend**: Test the trigger locally with `firebase functions:shell` using orders that meet/don't meet the new criteria.

### Manual Verification
1. **Global Count**: Complete a purchase and a sale; verify the total count in Profile is 2.
2. **Filters**:
   - Add a review from 2 months ago and one from today.
   - Select "Ultimos 30 dias": Only the today review and its rating should show.
   - Select "Ultimo anno": Both should show.
3. **UI Consistency**: Verify all labels use correct Spanish grammar and pluralization.
