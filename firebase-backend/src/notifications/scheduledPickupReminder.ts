import { onSchedule } from "firebase-functions/v2/scheduler";
import { logger } from "firebase-functions/v2";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { Collections, status } from "../contract";
import { notifyUser } from "./notifyUser";

const DAY_MS = 24 * 60 * 60 * 1000;
const GRACE_MS = 2 * DAY_MS;
const INTERVAL_MS = 2 * DAY_MS;

function orderCodeOf(orderId: string): string {
  return orderId.length > 7 ? orderId.slice(0, 7).toUpperCase() : orderId.toUpperCase();
}

/**
 * Recordatorio de cartas pendientes de retiro.
 *
 * Corre diario a las 9:00 AM (America/Costa_Rica) y notifica al comprador de cada orden que
 * lleva más de GRACE_MS en la tienda destino sin retirarse, repitiendo cada INTERVAL_MS.
 * Se detiene solo: al retirar, la orden sale de DELIVERED_TO_STORE.
 */
export const scheduledPickupReminder = onSchedule(
  {
    schedule: "0 9 * * *", // min hora * * * (todos los días)
    timeZone: "America/Costa_Rica",
  },
  async () => {
    const db = getFirestore();

    const snapshot = await db
      .collection(Collections.orders)
      .where("status", "==", status.DELIVERED_TO_STORE)
      .get();

    if (snapshot.empty) {
      logger.info("Recordatorio de retiro: no hay cartas esperando en tienda.");
      return;
    }

    const storesSnap = await db.collection(Collections.stores).get();
    const storesMap: Record<string, { name?: string; address?: string }> = {};
    storesSnap.forEach((doc) => {
      storesMap[doc.id] = doc.data();
    });

    const now = Date.now();
    let sent = 0;

    for (const doc of snapshot.docs) {
      const orderId = doc.id;
      const order = doc.data();
      const buyerId = order.buyerId;
      if (!buyerId) continue;

      const deliveredAt: number = order.deliveredToStoreAt ?? order.modifiedAt ?? order.createdAt ?? 0;
      if (!deliveredAt) continue;

      const lastSent: number = order.lastPickupReminderAt ?? 0;
      const due =
        lastSent === 0 ? now - deliveredAt >= GRACE_MS : now - lastSent >= INTERVAL_MS;
      if (!due) continue;

      const prefDoc = await db.collection(Collections.notificationPreferences).doc(buyerId).get();
      const prefs = prefDoc.data();

      if (prefs && prefs.pickupReminders === false) {
        logger.info(`Recordatorio de retiro: usuario ${buyerId} tiene los recordatorios desactivados.`);
        continue;
      }

      const store = storesMap[order.destinationStore] ?? {};
      const storeName = store.name || order.destinationStore || "tu tienda destino";
      const storeAddress = store.address || "";
      const cardCount = Array.isArray(order.cards) ? order.cards.length : 0;
      const waitingDays = Math.floor((now - deliveredAt) / DAY_MS);

      const title =
        cardCount > 1
          ? `Tus ${cardCount} cartas te esperan en ${storeName}`
          : `Tu carta te espera en ${storeName}`;
      const bodyParts = [
        `Pedido ${orderCodeOf(orderId)}`,
        `${cardCount} ${cardCount === 1 ? "carta" : "cartas"}`,
      ];
      if (storeAddress) bodyParts.push(storeAddress);
      const body = `${bodyParts.join(" · ")}. Llegó hace ${waitingDays} ${waitingDays === 1 ? "día" : "días"}.`;

      try {
        await notifyUser({
          userId: buyerId,
          type: "PICKUP_REMINDER",
          title,
          body,
          data: { orderId, storeId: order.destinationStore ?? "", storeName },
        });

        await doc.ref.update({
          lastPickupReminderAt: now,
          pickupRemindersSent: FieldValue.increment(1),
        });
        sent++;
      } catch (error) {
        logger.error(
          `Recordatorio de retiro: falló notificar al usuario ${buyerId} para la orden ${orderId}`,
          error,
        );
      }
    }

    logger.info(
      `Recordatorio de retiro: ${sent} recordatorios enviados de ${snapshot.size} órdenes en tienda.`,
    );
  },
);
