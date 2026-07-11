import { initializeApp } from "firebase-admin/app";

initializeApp();

// ── Intercambio (sobres) ──
export { releaseUnpaidExchanges } from "./exchange/releaseUnpaidExchanges";

// ── Notificaciones ──
export { onOrderStatusChange } from "./notifications/onOrderStatusChange";
export { scheduledSinpeReminder } from "./notifications/scheduledSinpeReminder";
