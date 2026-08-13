# Cloud Functions Based Batch Grouper

Implement an automated batch grouping system in the Firebase backend that groups paid orders by their source and destination stores every Thursday.

## User Review Required

- **Batch ID Generation:** I plan to generate human-readable IDs in the format `L-XXXX` (e.g., `L-4829`) to match the existing seeder pattern.
- **Courier Reward:** I will use a default value of `2500` colones per batch, as seen in the test seeder. Please confirm if a different calculation logic is required.
- **Store Addresses:** The `DeliveryBatch` model requires store addresses. If the `stores` collection does not contain an `address` field for a store, I will use the store name/ID as a fallback.
- **Order Status:** Orders will remain in `WAITING_STORE_SHIPMENT` status after being batched, but they will have a `batchId` assigned. They will transition to `IN_TRANSIT` only when the courier confirms pickup (existing logic in `BatchRepositoryImpl`).

## Proposed Changes

### Shared Module (`shared`)

Add `batchId` to the `Order` model to track which batch an order belongs to and prevent duplicate grouping.

#### [Order.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/Order.kt)

- Add `val batchId: String? = null` to the `Order` data class.

#### [OrderRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/OrderRepositoryImpl.kt)

- Update `toOrder()` to map the `batchId` field from Firestore.

---

### Backend Module (`firebase-backend`)

Implement the grouping logic and scheduling.

#### [contract.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/contract.ts)

- Add `batches: "batches"` and `stores: "stores"` to the `Collections` object.
- Define `BatchStatus` and `BatchQrStatus` constants matching the Kotlin enums.

#### [NEW] [groupOrdersIntoBatches.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/exchange/groupOrdersIntoBatches.ts)

- Implement `groupOrdersIntoBatches`:
    - Scheduled for `5 12 * * 4` (Thursdays 12:05 PM CR time).
    - Queries orders where `status === 'WAITING_STORE_SHIPMENT'`, `sinpePaid === true`, and `batchId` is missing/null.
    - Groups results by `sourceStore` and `destinationStore`.
    - Creates `DeliveryBatch` documents with status `READY_FOR_PICKUP`.
    - Updates grouped orders with the new `batchId`.
- Implement `triggerGroupOrdersIntoBatches`:
    - An `onCall` function to manually trigger the grouping logic for testing.

#### [index.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/index.ts)

- Export `groupOrdersIntoBatches` and `triggerGroupOrdersIntoBatches`.

---

## Verification Plan

### Automated Tests
- I will create a script or use `firebase functions:shell` to invoke the `triggerGroupOrdersIntoBatches` function.
- **Command:** `firebase functions:shell` then `triggerGroupOrdersIntoBatches({})`.

### Manual Verification
1.  **Preparation:** Create 2-3 test orders in Firestore with `sinpePaid: true`, `status: 'WAITING_STORE_SHIPMENT'`, and different store pairs.
2.  **Execution:** Call the `triggerGroupOrdersIntoBatches` function via the Firebase console or the helper function.
3.  **Check:**
    - Verify that new documents are created in the `batches` collection.
    - Verify that each batch contains the correct `orderIds` based on store pairs.
    - Verify that the `orders` documents now have the correct `batchId`.
    - Verify that store names and addresses (if available) are correctly populated in the batch.
