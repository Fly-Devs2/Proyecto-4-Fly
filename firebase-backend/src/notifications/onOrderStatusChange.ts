import { onDocumentUpdatedWithAuthContext } from "firebase-functions/v2/firestore";
import { logger } from "firebase-functions/v2";
import { Collections, status, statusLabels } from "../contract";
import { notifyUser } from "./notifyUser";
import { sendOrderQrEmail } from "./sendOrderQrEmail";

export const onOrderStatusChange = onDocumentUpdatedWithAuthContext(
  `${Collections.orders}/{orderId}`,
  async (event) => {
    const before = event.data?.before.data();
    const after = event.data?.after.data();
    if (!before || !after || before.status === after.status) return;

    // Lógica optimizada para enviar email con QR vía Firestore
    const isPaid = after.sinpePaid === true && after.sinpeReceiptUrl;
    const isReadyForShipment = after.status === status.WAITING_STORE_SHIPMENT;
    const alreadySent = after.qrEmailSent === true;

    if (isReadyForShipment && isPaid && !alreadySent) {
      const buyerId = after.buyerId;
      const db = event.data?.after.ref.firestore;
      if (db && buyerId) {
        const userSnap = await db.collection(Collections.users).doc(buyerId).get();
        const userEmail = userSnap.data()?.email;
        const qrImageUrl = after.qrImageUrl;
        const qrBase64 = after.qrBase64;

        if (userEmail && (qrBase64 || qrImageUrl)) {
          try {
            await sendOrderQrEmail({
              email: userEmail,
              orderId: event.params.orderId,
              qrImageUrl: qrImageUrl,
              qrBase64: qrBase64,
            });
            // Marcar como enviado para evitar duplicados
            await event.data?.after.ref.update({ qrEmailSent: true });
          } catch (error) {
            logger.error(`Falló la creación del doc de email para la orden ${event.params.orderId}`, error);
          }
        } else {
          logger.warn(
            `Email no encolado para ${event.params.orderId}: Falta email (${!!userEmail}) o QR (${!!qrImageUrl})`
          );
        }
      }
    }

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
