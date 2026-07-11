import { initializeApp } from "firebase-admin/app";

initializeApp();

// ── Intercambio (sobres) ──
export { releaseUnpaidExchanges } from "./exchange/releaseUnpaidExchanges";
export { generateOrderQr } from "./exchange/generateOrderQr";

// ── Notificaciones ──
export { onOrderStatusChange } from "./notifications/onOrderStatusChange";
export { scheduledSinpeReminder } from "./notifications/scheduledSinpeReminder";
