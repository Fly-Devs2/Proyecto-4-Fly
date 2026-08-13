# Walkthrough - Global Transactions & Time-Based Reputation Filters

I have implemented a comprehensive update to the reputation system, introducing global transaction tracking and time-based filtering.

## Key Features

### 1. Global Transaction Tracking
- **New Criteria**: A "Transaction" is now strictly defined as an order that is both **SINPE Paid** and has **Seller Delivery Evidence**.
- **Global Count**: The UI now displays a "Total Transactions" count, which is the sum of all purchases and sales that meet the completion criteria.
- **Backend Sync**: Updated the Firebase Cloud Functions to automatically recalculate these counts whenever an order's payment or evidence status changes.

### 2. Time-Based Reputation Filters
- **New Filters**: Users can now filter reputation and stats by:
    - **Últimos 30 días**
    - **Último año**
    - **Todo** (All time)
- **Efficiency**: The backend now stores pre-calculated summaries for each of these timeframes, ensuring that the app remains fast even as the number of reviews grows.
- **UI Integration**: Added a timeframe selector (tabs) to the reputation view, allowing users to see how a seller or buyer has performed recently.

## Technical Changes

### Domain & Data Layer
- **[UserRatingSummary.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/UserRatingSummary.kt)**: Refactored to support nested `TimeframeSummary` objects.
- **[RoleReputationSummary.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/RoleReputationSummary.kt)**: Added `salesCount` and `completedTransactionCount` to the role-specific view.
- **[ReputationRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/ReputationRepositoryImpl.kt)**: Updated `getReviews` to include a timeframe filter in the Firestore query.

### Backend (Cloud Functions)
- **[updateOrderReputation.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/reviews/updateOrderReputation.ts)**: Implemented the new "Transaction" vs "Sales" logic. Transactions require payment + evidence, while Sales require `PICKED_UP` status.
- **[updateUserRatingSummary.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/reviews/updateUserRatingSummary.ts)**: Updated to recalculate star distributions and averages for all three timeframes simultaneously.

### UI Layer (Compose)
- **[ReputationContent.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/components/ReputationContent.kt)**: Added the `ReputationTimeframeSelector` and updated the summary card to show global stats.
- **[ProfileScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ProfileScreen.kt)**: Integrated the updated reputation content into the user profile.

## Verification Summary
- **Compilation**: Verified that both `shared` and `composeApp` modules compile correctly. Fixed initial issues with `kotlinx-datetime` (switched to project utility) and updated legacy references in `CardDetailViewModel`.
- **Logic**: The distinction between Sales (`PICKED_UP`) and Transactions (`SINPE` + `Evidence`) is now enforced at the database level.
- **UI**: Verified that the timeframe tabs correctly trigger a reload of the stats and reviews.
