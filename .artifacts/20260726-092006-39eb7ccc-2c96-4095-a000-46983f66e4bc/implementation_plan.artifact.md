# QR Code Store Acceptance Flow

This plan implements a QR-based acceptance system for stores and couriers. Stores can display a QR code representing all their outgoing batches, and couriers can scan it to accept them all at once.

## User Review Required

> [!IMPORTANT]
> - **Store Identification**: I'm adding an optional `storeId` and `storeName` to the `User` model to link store admins to their respective stores.
> - **QR Generation**: Since there's no built-in KMP QR generator, I'll use `api.qrserver.com` to display the QR image in the `StoreBatchesScreen`.
> - **Navigation**: I'll add the `StoreBatches` route to `Routes.kt` and integrate it into `App.kt`. For now, I'll add a trigger in the `ProfileScreen` to access this view.

## Proposed Changes

### Domain & Data Layer

#### [User.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/User.kt)
- Add optional `storeId` and `storeName` fields to the `User` data class.

#### [IBatchRepository.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/repository/IBatchRepository.kt)
- Add `observeOutgoingStoreBatches(storeId: String): Flow<List<DeliveryBatch>>`.
- Add `acceptAllStoreBatches(storeId: String, courierId: String, courierName: String): Int`.

#### [BatchRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/BatchRepositoryImpl.kt)
- Implement `observeOutgoingStoreBatches` querying by `sourceStoreId` and `status: READY_FOR_PICKUP`.
- Implement `acceptAllStoreBatches` using a Firestore transaction to update all eligible batches.

---

### Presentation Layer (Shared)

#### [NEW] [StoreBatchesUiState.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/storeBatches/StoreBatchesUiState.kt)
- Define state for the store view: `batches`, `isLoading`, `errorMessage`, `storeName`, `storeId`.

#### [NEW] [StoreBatchesViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/storeBatches/StoreBatchesViewModel.kt)
- Logic to fetch outgoing batches and calculate summaries (total batches, total cards).
- Construct the QR payload: `flydevs://store-qr?sid=STORE_ID`.

#### [ScanQrViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/ScanQrViewModel.kt)
- Update `onQrScanned` to handle `store-qr?sid=STORE_ID`.
- Call `acceptAllStoreBatches` when a store QR is scanned.

---

### UI Layer (Compose)

#### [Routes.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/navigation/Routes.kt)
- Add `@Serializable object StoreBatches` to the navigation routes.

#### [NEW] [StoreBatchesScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/StoreBatchesScreen.kt)
- Implement the screen as shown in the screenshots:
    - QR Code Card with store name.
    - Summary section (X lotes, Y cartas).
    - List of "Lotes Salientes" using the requested compact style.

#### [App.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/App.kt)
- Add the `composable<StoreBatches>` route to the `NavHost`.
- Integrate `StoreBatchesScreen` with its ViewModel and navigation.

#### [ProfileScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ProfileScreen.kt)
- Add a "Gestionar Lotes" button or row that navigates to `StoreBatches`.

## Verification Plan

### Automated Tests
- No new automated tests planned due to UI/Firestore dependency, but manual verification will be thorough.

### Manual Verification
1. **Store View**:
   - Navigate to the Store Batches screen via Profile.
   - Verify the summary matches the list of batches.
   - Verify the QR code is displayed correctly.
2. **Scanner Flow**:
   - Use a second device or an emulator with a QR image to scan the store QR.
   - Verify that all `READY_FOR_PICKUP` batches for that store are assigned to the courier.
   - Verify the courier sees a success message "X lotes asignados".
   - Verify the batches now appear in the courier's "Mis Lotes" screen.
3. **UI Consistency**:
   - Verify colors and typography match the design tokens in `AGENTS.md`.
