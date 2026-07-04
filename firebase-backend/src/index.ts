import { initializeApp } from "firebase-admin/app";

initializeApp();

// ── Intercambio (sobres) ──
export { releaseUnpaidExchanges } from "./exchange/releaseUnpaidExchanges";
