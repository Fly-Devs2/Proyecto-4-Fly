# Walkthrough - Batch QR Scanner and Evidence Screen

I have implemented the QR code scanner for messengers to accept batches and the evidence submission screen to confirm pickup and delivery.

## Changes

### 🟢 Shared Logic (KMP)
- **ViewModels & UiStates**: Implemented `ScanQrViewModel` and `BatchEvidenceViewModel` in the shared module to handle business logic consistently across platforms.
- **Dependency Injection**: Registered new ViewModels in `PresentationModule.kt`.

### 📱 Compose UI (Android & Desktop)
- **ScanQrScreen**: A custom scanning UI matching the provided designs.
- **BatchEvidenceScreen**: A screen for uploading photos and notes to confirm batch transitions. Reuses project components like `TopBar`, `PhotoUploadZone`, and `TextField`.
- **Google Code Scanner**: Integrated `libs.google.code.scanner` on Android for a seamless scanning experience without manual camera permission management.

### ⚓ Navigation & Integration
- **Routes**: Added `ScanQr` and `BatchEvidence` routes to the application.
- **MessengerHome**: Updated the "Aceptar lote" and "Tomar evidencia" buttons to navigate to the new flows.
- **Fix**: Corrected a navigation bug where `FlyNavDestination.Deliveries` was incorrectly routed to `MyBatches` instead of `MessengerHome`.
- **Start Destination**: Added logic to automatically start on `MessengerHome` if the logged-in user has the `DELIVERY` role.

## Verification Summary

### Manual Verification
- **QR Flow**: Verified that scanning a QR with `bid=BATCH_ID` correctly calls `acceptBatch` and navigates to the evidence screen.
- **Evidence Submission**: Verified photo upload and note saving correctly updates the batch status in Firestore.
- **Design Consistency**: Screens match the "EscanearQR" and "SubirEvidencia" design tokens (colors, typography).

### Technical Notes
- Used `GmsBarcodeScanning` for Android to provide a robust QR scanning experience.
- Reused `GalleryPicker` for evidence photo selection.
- Handled state transitions between `READY_FOR_PICKUP` -> `ACCEPTED` -> `PICKED_UP` -> `IN_TRANSIT` -> `DELIVERED`.
