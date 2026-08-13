import { onSchedule } from "firebase-functions/v2/scheduler";
import { onCall, HttpsError } from "firebase-functions/v2/https";
import { logger } from "firebase-functions/v2";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { BatchQrStatus, BatchStatus, Collections, status } from "../contract";

/**
 * Genera un ID legible para el lote (ej: L-4829).
 */
function generateReadableBatchId(): string {
  const random = Math.floor(1000 + Math.random() * 9000);
  return `L-${random}`;
}

/**
 * Lógica central para agrupar órdenes pagadas en lotes.
 */
async function runGroupingLogic() {
  const db = getFirestore();
  const now = Date.now();

  // 1. Obtener órdenes pagadas esperando envío que no tengan batchId
  const ordersSnap = await db
    .collection(Collections.orders)
    .where("status", "==", status.WAITING_STORE_SHIPMENT)
    .where("sinpePaid", "==", true)
    .get();

  const orders = ordersSnap.docs
    .map((doc) => ({ id: doc.id, ...doc.data() } as any))
    .filter((order) => !order.batchId);

  if (orders.length === 0) {
    logger.info("Grouping: No orders found for batching.");
    return { createdBatches: 0, updatedOrders: 0 };
  }

  // 2. Agrupar órdenes por tienda origen y destino
  const groups: Record<string, any[]> = {};
  for (const order of orders) {
    const key = `${order.sourceStore}_${order.destinationStore}`;
    if (!groups[key]) {
      groups[key] = [];
    }
    groups[key].push(order);
  }

  // 3. Obtener información de tiendas para nombres y direcciones
  const storesSnap = await db.collection(Collections.stores).get();
  const storesMap: Record<string, any> = {};
  storesSnap.forEach((doc) => {
    storesMap[doc.id] = { id: doc.id, ...doc.data() };
  });

  let createdBatches = 0;
  let updatedOrders = 0;

  for (const key in groups) {
    const groupOrders = groups[key];
    const sourceStoreId = groupOrders[0].sourceStore;
    const destinationStoreId = groupOrders[0].destinationStore;

    const sourceStore = storesMap[sourceStoreId] || { name: sourceStoreId };
    const destinationStore = storesMap[destinationStoreId] || { name: destinationStoreId };

    const batchData = {
      batchId: generateReadableBatchId(),
      orderIds: groupOrders.map((o) => o.id),
      sourceStoreId,
      sourceStoreName: sourceStore.name || sourceStoreId,
      sourceStoreAddress: sourceStore.address || sourceStore.name || sourceStoreId,
      destinationStoreId,
      destinationStoreName: destinationStore.name || destinationStoreId,
      destinationStoreAddress: destinationStore.address || destinationStore.name || destinationStoreId,
      status: BatchStatus.READY_FOR_PICKUP,
      courierReward: 2500, // Recompensa por defecto según requerimiento
      qrStatus: BatchQrStatus.ACTIVE,
      qrImageUrl: "", // Se genera después o via trigger si es necesario
      qrImagePath: "",
      createdAt: now,
      updatedAt: now,
      courierId: null,
      courierName: null,
    };

    try {
      const batch = db.batch();
      const batchRef = db.collection(Collections.batches).doc();
      batch.set(batchRef, batchData);

      // Actualizar órdenes con el ID del lote
      for (const order of groupOrders) {
        const orderRef = db.collection(Collections.orders).doc(order.id);
        batch.update(orderRef, {
          batchId: batchData.batchId,
          updatedAt: FieldValue.serverTimestamp(),
        });
        updatedOrders++;
      }

      await batch.commit();
      createdBatches++;
      logger.info(`Grouping: Created batch ${batchData.batchId} with ${groupOrders.length} orders.`);
    } catch (error) {
      logger.error(`Grouping: Failed to create batch for ${key}`, error);
    }
  }

  return { createdBatches, updatedOrders };
}

/**
 * Función programada: Corre los jueves a las 12:05 PM (después de releaseUnpaidExchanges).
 */
export const groupOrdersIntoBatches = onSchedule(
  {
    schedule: "5 12 * * 4",
    timeZone: "America/Costa_Rica",
  },
  async () => {
    logger.info("Grouping: Starting scheduled batch grouping...");
    const result = await runGroupingLogic();
    logger.info(`Grouping: Finished. Created ${result.createdBatches} batches.`);
  }
);

/**
 * Trigger manual para pruebas.
 */
export const triggerGroupOrdersIntoBatches = onCall(async (request) => {
  // Opcional: Verificar permisos de admin aquí
  logger.info("Grouping: Manual trigger received.", { data: request.data });
  try {
    const result = await runGroupingLogic();
    return { success: true, ...result };
  } catch (error: any) {
    logger.error("Grouping: Manual trigger failed", error);
    throw new HttpsError("internal", error.message);
  }
});
