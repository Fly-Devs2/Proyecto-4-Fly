import { initializeApp } from "firebase-admin/app";

initializeApp();

// ── Intercambio (sobres) ──
export { releaseUnpaidExchanges } from "./exchange/releaseUnpaidExchanges";
export { generateOrderQr } from "./exchange/generateOrderQr";
export { groupOrdersIntoBatches, triggerGroupOrdersIntoBatches } from "./exchange/groupOrdersIntoBatches";

// ── Notificaciones ──
export { onOrderStatusChange } from "./notifications/onOrderStatusChange";
export { scheduledSinpeReminder } from "./notifications/scheduledSinpeReminder";
export { scheduledPickupReminder } from "./notifications/scheduledPickupReminder";

//── Reviews ──
export {updateUserRatingSummary} from "./reviews/updateUserRatingSummary";
export { updateOrderReputation,} from "./reviews/updateOrderReputation";

