import { onSchedule } from "firebase-functions/v2/scheduler";
import { logger } from "firebase-functions/v2";
import { getFirestore, Filter } from "firebase-admin/firestore";
import { Collections } from "../contract";
import { notifyUser } from "./notifyUser";

/**
 * Recordatorio semanal de SINPE.
 *
 * Corre todos los jueves a las 8:00 AM (America/Costa_Rica).
 * Notifica a los usuarios con pedidos que tengan sinpePaid en false o sinpeReceiptUrl vacío.
 */
export const scheduledSinpeReminder = onSchedule(
  {
    schedule: "0 8 * * 4", // 8:00 AM todos los jueves
    timeZone: "America/Costa_Rica",
  },
  async () => {
    const db = getFirestore();

    // 1. Obtener órdenes con pago SINPE pendiente usando Filter.or para mayor eficiencia
    // Solo aplica para órdenes con estado WAITING_PAYMENT
    const snapshot = await db
      .collection(Collections.orders)
      .where("status", "==", "WAITING_PAYMENT")
      .where(
        Filter.or(
          Filter.where("sinpePaid", "==", false),
          Filter.where("sinpeReceiptUrl", "==", ""),
        ),
      )
      .get();

    if (snapshot.empty) {
      logger.info("Recordatorio SINPE: No hay pedidos pendientes.");
      return;
    }

    // 2. Notificar por cada pedido pendiente
    for (const doc of snapshot.docs) {
      const orderId = doc.id;
      const order = doc.data();
      const buyerId = order.buyerId;
      if (!buyerId) continue;

      // 3. Verificar preferencias del usuario
      const prefDoc = await db.collection(Collections.notificationPreferences).doc(buyerId).get();
      const prefs = prefDoc.data();

      // Si no existe el doc, por defecto notificamos (opt-out). Si existe, verificamos sinpeReminders.
      if (prefs && prefs.sinpeReminders === false) {
        logger.info(`Recordatorio SINPE: Usuario ${buyerId} tiene los recordatorios desactivados.`);
        continue;
      }

      try {
        await notifyUser({
          userId: buyerId,
          type: "SINPE_REMINDER",
          title: `Sube el comprobante del pedido ${orderId}`,
          body: "Recuerda subir tu comprobante de Sinpe hoy antes de las 12 mediodía",
          data: { orderId },
        });
      } catch (error) {
        logger.error(`Recordatorio SINPE: Falló notificar al usuario ${buyerId} para la orden ${orderId}`, error);
      }
    }

    logger.info(`Recordatorio SINPE: Procesadas ${snapshot.size} notificaciones.`);
  },
);
