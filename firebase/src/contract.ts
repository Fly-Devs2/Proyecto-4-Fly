export const Collections = {
  exchanges: "exchanges",
  gameCards: "game_cards",
} as const;

export const ExchangeStatus = {
  WAITING_STORE_DELIVERY: "WAITING_STORE_DELIVERY",
  WAITING_SINPE_PROOF: "WAITING_SINPE_PROOF",
  PROOF_RECEIVED: "PROOF_RECEIVED",
  SHIPPING: "SHIPPING",
  COMPLETED: "COMPLETED",
  EXPIRED: "EXPIRED",
} as const;

export const CardStatus = {
  AVAILABLE: "AVAILABLE",
  RESERVED: "RESERVED",
  SOLD: "SOLD",
} as const;
