import { initializeApp } from "firebase-admin/app";

initializeApp();

// ── Intercambio (sobres) ──
export { releaseUnpaidExchanges } from "./exchange/releaseUnpaidExchanges";
export { generateOrderQr } from "./exchange/generateOrderQr";
