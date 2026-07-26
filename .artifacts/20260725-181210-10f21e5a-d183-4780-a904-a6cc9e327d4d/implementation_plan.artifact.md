# Fix QR Scan Re-entry Error

The error "El lote ya no está disponible" (which may appear as "El sobre..." in some contexts) occurs when a courier scans a QR code for a batch that has already been accepted. The current logic strictly requires the batch to be in `READY_FOR_PICKUP` status to process the scan.

This plan updates the QR scanner to be smarter: if a courier scans a batch they have already accepted, the app will skip the acceptance step and take them directly to the pickup evidence screen.

## Proposed Changes

### Presentation Layer

#### [ScanQrViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/batch/ScanQrViewModel.kt)

- Update `resolveAndAcceptBatch` to check the current status and courier assignment of the batch before trying to accept it.
- If the batch is already assigned to the current courier, simply return the ID as a success to trigger navigation to the evidence screen.

```kotlin
    private fun resolveAndAcceptBatch(labelOrId: String) {
        // ...
        viewModelScope.launch {
            runCatching {
                val resolvedId = batchRepository.findBatchIdByLabel(labelOrId) ?: labelOrId
                val batch = batchRepository.getBatchById(resolvedId)

                // If batch already assigned to current user, just proceed to evidence
                if (batch != null && batch.courierId == courierId) {
                    return@runCatching resolvedId
                }

                // ... proceed with normal acceptance ...
            }
            // ...
        }
    }
```

## Verification Plan

### Manual Verification
1.  **Accept a Batch**: Log in as a courier and scan a fresh batch QR. It should work as normal and take you to the pickup evidence screen.
2.  **Re-scan the same Batch**: While the batch is still in `ACCEPTED` status, scan the same QR again.
3.  **Result**: Instead of the error "El lote ya no está disponible", the app should immediately open the pickup evidence screen for that batch.
