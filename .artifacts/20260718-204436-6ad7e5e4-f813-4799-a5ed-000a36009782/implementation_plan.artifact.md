# Implementation Plan - Store Selection Feature (Phase 3 & 4)

This plan outlines the integration of store selection and enhanced visibility across orders and envelopes.

## Proposed Changes

### Presentation Layer (ViewModels & UI)

#### [OrderDetailUiState.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/orderDetail/OrderDetailUiState.kt)
- Add `sourceStoreName: String? = null` and `destinationStoreName: String? = null`.

#### [OrderDetailViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/orderDetail/OrderDetailViewModel.kt)
- Inject `IStoreRepository`.
- Fetch store names for `sourceStore` and `destinationStore` when the order is loaded.

#### [OrderDetailScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/OrderDetailScreen.kt)
- Update `OrderDataSection` to display "Tienda de origen" and "Tienda de destino".
- Update `TrackingStepItem` to include dynamic messages like "Esperando envío a 'Store'".

#### [CardEnvelopesUIState.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/Envelopes/CardEnvelopesUIState.kt)
- Add `sourceStoreNames: Map<String, String> = emptyMap()`.

#### [CardEnvelopesViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/Envelopes/CardEnvelopesViewModel.kt)
- Fetch store names for envelopes.
- **Validation**: Ensure `selectedGlobalStore` is set before generating bulk orders.

#### [MyEnvelopesScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/MyEnvelopesScreen.kt)
- Display source store name in `EnvelopeListItem`.
- Ensure snackbar shows errors if global store is missing.

#### [CardEnvelopeUiState.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/envelope/CardEnvelopeUiState.kt)
- Add `sourceStoreName: String? = null`.

#### [CardEnvelopeViewModel.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/envelope/CardEnvelopeViewModel.kt)
- Fetch `sourceStoreName`.
- **Validation**: Ensure `selectedStore` is set before generating order.

#### [MyEnvelopeScreen.kt](file:///C:/Users/basti/Desktop/Cenfotec/9no Cuatrimestre/Proyecto 4/Fly App/Fly App/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/MyEnvelopeScreen.kt)
- Display source store in top bar.
- Ensure snackbar shows errors if store is missing.

---

## Verification Plan

### Manual Verification
- **Validation**:
    1. Open an envelope without selecting a destination store.
    2. Click "Reservar sobre".
    3. Verify an error message "Debes seleccionar una tienda de destino" appears.
- **Bulk Validation**:
    1. Go to "Mis sobres" without selecting a global store.
    2. Click "Generar orden para todos".
    3. Verify an error message appears.
