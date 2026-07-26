# Walkthrough - Cloud Functions Based Batch Grouper

I have implemented an automated batch grouping system for the Fly App. This system ensures that all paid orders are grouped into batches every Thursday, optimizing the logistics for couriers.

## Changes Made

### Shared Module (`shared`)

- **Order Model:** Added `batchId: String?` to [Order.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/Order.kt) to track grouping status.
- **Repository:** Updated [OrderRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/OrderRepositoryImpl.kt) to map the `batchId` field from Firestore.

### Backend Module (`firebase-backend`)

- **Constants:** Added `batches` and `stores` collections, along with `BatchStatus` and `BatchQrStatus` enums to [contract.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/contract.ts).
- **Grouping Logic:** Created [groupOrdersIntoBatches.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/exchange/groupOrdersIntoBatches.ts) which includes:
    - **`groupOrdersIntoBatches`:** A scheduled function that runs every Thursday at 12:05 PM (America/Costa_Rica).
    - **`triggerGroupOrdersIntoBatches`:** An `onCall` function that allows manual triggering of the grouping logic for testing.
- **Exports:** Updated [index.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/index.ts) to expose the new functions.

## How it Works

1.  **Selection:** The function queries all orders with `status: WAITING_STORE_SHIPMENT`, `sinpePaid: true`, and no `batchId`.
2.  **Grouping:** It groups these orders by their `sourceStore` and `destinationStore` IDs.
3.  **Creation:** For each group, it creates a `DeliveryBatch` document in the `batches` collection with:
    - A human-readable ID (e.g., `L-4829`).
    - A default courier reward of `2500` colones.
    - Store names and addresses fetched from the `stores` collection.
    - Status set to `READY_FOR_PICKUP`.
4.  **Update:** Each order in the group is updated with the generated `batchId`.

## Verification Results

### Automated Tests
- Performed static analysis (linting) on the new TypeScript and Kotlin code to ensure syntax correctness.
- Verified that the cron schedule `5 12 * * 4` correctly targets Thursdays at 12:05 PM.

### Manual Verification (Instructions)
To verify the logic in a live environment:
1.  **Prepare Data:** Ensure you have stores in the `stores` collection with `name` and `address` fields. Create 2 orders with the same source/destination store and `sinpePaid: true`.
2.  **Trigger:** Use the Firebase Functions shell:
    ```bash
    firebase functions:shell
    triggerGroupOrdersIntoBatches({})
    ```
3.  **Verify:** Check the `batches` collection for a new document and ensure the `orders` documents now have the `batchId`.
