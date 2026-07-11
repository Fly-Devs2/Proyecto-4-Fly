import { onDocumentUpdatedWithAuthContext } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { Collections, statusLabels } from "../contract";
import { notifyUser } from "./notifyUser";

export const onOrderStatusChange = onDocumentUpdatedWithAuthContext(
  `${Collections.orders}/{orderId}`,
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;

    const label = statusLabels[after.status] ?? after.status;
    const orderId = event.params.orderId;

    const actor = event.authId;
    const recipients = new Set(
      [after.buyerId, after.sellerId].filter(
        (uid): uid is string => Boolean(uid) && uid !== actor,
      ),
    );

    logger.info(
      `Pedido ${orderId}: ${before.status} → ${after.status}, notificando a ${recipients.size} usuario(s).`,
    );

    await Promise.all(
      [...recipients].map(async (uid) => {
        // Verificar preferencias
        const db = event.data?.after.ref.firestore;
        if (db) {
            const prefDoc = await db.collection(Collections.notificationPreferences).doc(uid).get();
            const prefs = prefDoc.data();
            if (prefs && prefs.orderStatusChanged === false) {
                logger.info(`Pedido ${orderId}: Usuario ${uid} tiene desactivadas las notificaciones de estado.`);
                return;
            }
        }

        return notifyUser({
          userId: uid,
          type: "ORDER_STATUS_CHANGED",
          title: "Actualización de tu envío",
          body: `Tu pedido cambió a: ${label}`,
          data: { orderId, status: after.status },
        });
      }),
    );
  },
);
