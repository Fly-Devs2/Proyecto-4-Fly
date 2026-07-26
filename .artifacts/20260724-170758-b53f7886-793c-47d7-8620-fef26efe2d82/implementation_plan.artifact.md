# Implementation Plan - Batch QR Scanner and Evidence Screen

Implement a QR code reader for messengers to accept batches and a corresponding evidence submission screen to confirm pickup/delivery, following the provided designs and reusing existing components.

## Proposed Changes

### Shared Module (Logic & Data)

#### [NEW] [BatchEvidenceUiState.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/BatchEvidenceUiState.kt)
- Define the state for the evidence screen.
- Includes `DeliveryBatch` info, `isLoading`, `images` (list of URLs), `note`, `confirmChecked`, and `feedback`.

#### [NEW] [BatchEvidenceViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/BatchEvidenceViewModel.kt)
- Logic for loading batch details.
- Handles image picking and uploading to storage.
- Logic for `confirmPickup` or `confirmDelivery` based on current batch status.

#### [NEW] [ScanQrUiState.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/ScanQrUiState.kt)
- State for the QR scanner (loading, error, success).

#### [NEW] [ScanQrViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/ScanQrViewModel.kt)
- Logic for validating scanned QR payload.
- Calls `acceptBatch` in the repository.
- Triggers navigation to the evidence screen on success.

#### [PresentationModule.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/di/modules/PresentationModule.kt)
- Register `ScanQrViewModel` and `BatchEvidenceViewModel`.

---

### ComposeApp Module (UI)

#### [NEW] [ScanQrScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ScanQrScreen.kt)
- QR scanner UI matching the "EscanearQR" design.
- Custom frame overlay and bottom controls.
- Uses platform-specific `QrScannerCamera` component.

#### [NEW] [BatchEvidenceScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/BatchEvidenceScreen.kt)
- Evidence submission UI matching the "SubirEvidencia" design.
- Reuses `TopBar`, `PhotoUploadZone`, `PrimaryButton`, etc.
- Displays "QR validado" chip and batch summary card.

#### [App.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/App.kt)
- Add navigation routes for `ScanQrScreen` and `BatchEvidenceScreen`.
- Link `MessengerHomeScreen`'s `onScanQr` to the new route.

#### [MessengerHomeScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/MessengerHomeScreen.kt)
- Update `ActiveBatchActionButton` to navigate to `BatchEvidenceScreen` when taking pickup/delivery photos.

---

### Android Specific (Camera Logic)

#### [NEW] [QrScannerCamera.android.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/androidMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/components/QrScannerCamera.android.kt)
- Implementation of the QR scanner using Google Code Scanner API (`GmsBarcodeScanning`).
- This allows scanning without needing camera permissions manually in many cases and provides a standard UI or can be used via the `BarcodeScanner` API for custom overlays.
- I will implement a wrapper that integrates with the provided design.

## Verification Plan

### Automated Tests
- N/A (UI and integration focused, but I will verify state changes in ViewModel).

### Manual Verification
- **QR Scanning**: Use a mock QR code with a batch ID and verify it triggers `acceptBatch`.
- **Navigation**: Verify it goes from `MessengerHomeScreen` -> `ScanQrScreen` -> (Scan) -> `BatchEvidenceScreen`.
- **Evidence Upload**: Upload an image, add a note, and confirm. Verify the batch status updates in the UI (back to Home).
- **Design Check**: Compare implemented screens with provided "EscanearQR" and "SubirEvidencia" images.
