# Evidence Mapping and Consistency Plan

Ensure all fields in `BatchEvidence` (including `downloadUrl`, `storagePath`, `note`, `uploadedAt`, and `uploadedBy`) are correctly mapped when uploading to Firestore and consistently displayed in the UI. If these fields are missing from existing documents, they will be added during the upload transaction.

## Proposed Changes

### Data Layer

#### [BatchRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/BatchRepositoryImpl.kt)

- Update `confirmPickup` and `confirmDelivery` to explicitly map all fields from the `evidence` parameter into the Firestore document.
- This ensures that even if the document previously lacked these fields (e.g., `note` or `downloadUrl`), they are now present and correctly typed.

```diff
             set(
                 documentRef = batchReference,
                 data = currentBatch.copy(
-                    pickupEvidence = evidence.copy(
+                    pickupEvidence = BatchEvidence(
+                        storagePath = evidence.storagePath,
+                        downloadUrl = evidence.downloadUrl,
+                        uploadedAt = now,
+                        uploadedBy = courierId,
                         type = BatchEvidenceType.PICKUP,
-                        uploadedAt = now,
-                        uploadedBy = courierId
+                        note = evidence.note
                     ),
                     pickupAt = now,
                     status = BatchStatus.IN_TRANSIT,
```

### Presentation Layer

#### [ShipmentDetailScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ShipmentDetailScreen.kt)

- Audit `BatchEvidencePolishedSection` to ensure it uses the `evidence` object fields correctly (it already uses `uploadedAt` and `note`).
- Verify `EvidenceThumbnail` uses the `imageUrl` provided (which comes from `downloadUrl`).

---

## Verification Plan

### Manual Verification
1. **Pickup Evidence Upload**:
    - Perform a pickup action, upload an image, and add a note.
    - Inspect Firestore (simulated via `ShipmentDetailScreen` reload) to ensure `pickupEvidence` has:
        - `downloadUrl`: [VALID_URL]
        - `note`: "Test Note"
        - `uploadedAt`: [TIMESTAMP]
        - `uploadedBy`: [COURIER_UID]
2. **Delivery Evidence Upload**:
    - Perform a delivery action, upload an image, and add a note.
    - Inspect UI to ensure `deliveryEvidence` is correctly populated.
3. **Consistency Check**:
    - Verify that both sections in `ShipmentDetailScreen` display the thumbnail, the formatted upload date, and the note if present.
