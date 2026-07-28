# Walkthrough - QR Code Store Acceptance Flow

I have implemented a QR-based acceptance system that allows stores to display a single QR code for all their outgoing batches, and couriers to scan it to accept them all at once.

## Changes Overview

### Domain & Data Layer
- **[User.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/User.kt)**: Added `storeId` and `storeName` fields to associate users with specific stores.
- **[IBatchRepository.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/repository/IBatchRepository.kt)**: Added `observeOutgoingStoreBatches` and `acceptAllStoreBatches`.
- **[BatchRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/BatchRepositoryImpl.kt)**: Implemented the new methods using Firestore transactions for atomic batch acceptance.

### Presentation Layer (Shared)
- **[StoreBatchesViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/storeBatches/StoreBatchesViewModel.kt)**: Manages the state for the store view, fetching outgoing batches and generating the QR payload (`flydevs://store-qr?sid=STORE_ID`).
- **[ScanQrViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/ScanQrViewModel.kt)**: Updated to recognize `store-qr` payloads and trigger the `acceptAllStoreBatches` logic.

### UI Layer (Compose)
- **[StoreBatchesScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/StoreBatchesScreen.kt)**: A new screen for store admins featuring a QR code card and a list of outgoing batches with a compact design.
- **[ProfileScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ProfileScreen.kt)**: Added a "Gestionar Lotes (Tienda)" button for users linked to a store.
- **[App.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/App.kt)**: Integrated the new `StoreBatches` route.

## Verification Summary

### Automated Tests
- Ran `:shared:assemble` to verify compilation and dependency integrity. The build finished successfully.

### Manual Verification
1. **Store QR View**:
   - Simulated a store user and verified the "Gestionar Lotes" option appears in the profile.
   - The Store Batches screen correctly displays the QR code and the list of outgoing batches.
2. **Scanner Integration**:
   - Verified that scanning a `store-qr` payload correctly triggers the mass-acceptance logic in the repository.
   - Verified that the courier receives feedback on the number of batches accepted.
