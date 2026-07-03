import { onSchedule } from "firebase-functions/v2/scheduler";
import { logger } from "firebase-functions/v2";
import { getFirestore } from "firebase-admin/firestore";
import { CardStatus, Collections, ExchangeStatus } from "../contract";

/**
 * Reintegro cuando no se realiza el pago SINPE.
 *
 * Corre todos los jueves a las 12:00 md (America/Costa_Rica) y libera los sobres que
 * siguen esperando el comprobante SINPE.
 *   - el sobre pasa a EXPIRED
 *   - todas sus cartas vuelven a AVAILABLE
 */
export const releaseUnpaidExchanges = onSchedule(
  {
    schedule: "0 12 * * 4", // min hora * * díaDeLaSemana (4 = jueves)
    timeZone: "America/Costa_Rica",
  },
  async () => {
    const db = getFirestore();

    const pending = await db
      .collection(Collections.exchanges)
      .where("status", "==", ExchangeStatus.WAITING_SINPE_PROOF)
      .get();

    if (pending.empty) {
      logger.info("Reintegro: no hay sobres esperando comprobante SINPE.");
      return;
    }

    let releasedExchanges = 0;
    let releasedCards = 0;

    // Un batch por sobre
    for (const doc of pending.docs) {
      const data = doc.data();

      // Doble chequeo por si entró el comprobante entre la query y esta iteración.
      if (data.sinpeProofUrl) continue;

      const cardIds: string[] = Array.isArray(data.cardIds) ? data.cardIds : [];
      const batch = db.batch();

      batch.update(doc.ref, { status: ExchangeStatus.EXPIRED });
      for (const cardId of cardIds) {
        batch.update(
          db.collection(Collections.gameCards).doc(cardId),
          { status: CardStatus.AVAILABLE },
        );
      }

      try {
        await batch.commit();
        releasedExchanges++;
        releasedCards += cardIds.length;
      } catch (error) {
        logger.error(`Reintegro: falló liberar el sobre ${doc.id}`, error);
      }
    }

    logger.info(
      `Reintegro: ${releasedExchanges} sobres EXPIRED, ${releasedCards} cartas AVAILABLE.`,
    );
  },
);
