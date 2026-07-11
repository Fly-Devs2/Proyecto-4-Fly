export const Collections = {
  orders: "orders",
  gameCards: "game_cards",
  users: "users",
  notifications: "notifications",
  notificationPreferences: "notifications_preferences",
  mail: "mail",
} as const;

export const status = {
    RESERVED: "RESERVED",

  WAITING_SELLER_DELIVERY: "WAITING_SELLER_DELIVERY",
  WAITING_PAYMENT: "WAITING_PAYMENT",
  WAITING_STORE_SHIPMENT: "WAITING_STORE_SHIPMENT",
  IN_TRANSIT: "IN_TRANSIT",
  DELIVERED_TO_STORE: "DELIVERED_TO_STORE",
  PICKED_UP: "PICKED_UP",
  DISPUTED: "DISPUTED",
  CANCELLED: "CANCELLED",
} as const;

export const CardStatus = {
  AVAILABLE: "AVAILABLE",
  RESERVED: "RESERVED",
  SOLD: "SOLD",
} as const;

export const statusLabels: Record<string, string> = {
  RESERVED: "Reservado",
  WAITING_SELLER_DELIVERY: "Esperando entrega del vendedor",
  WAITING_PAYMENT: "Esperando pago",
  WAITING_STORE_SHIPMENT: "Esperando envío entre tiendas",
  IN_TRANSIT: "Tránsito entre tiendas",
  DELIVERED_TO_STORE: "Entregado en destino",
  PICKED_UP: "Carta recogida",
  DISPUTED: "Disputa abierta",
  CANCELLED: "Cancelado",
};
