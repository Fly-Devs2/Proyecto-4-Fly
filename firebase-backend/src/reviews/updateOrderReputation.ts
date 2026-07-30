import {getFirestore} from "firebase-admin/firestore";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";

type TransactionRole = "BUYER" | "SELLER";

interface OrderDocument {
  buyerId?: unknown;
  sellerId?: unknown;
  status?: unknown;
  sinpePaid?: unknown;
}

interface TransactionTarget {
  userId: string;
  role: TransactionRole;
}

function getStringValue(
  value: unknown
): string | null {
  if (
    typeof value !== "string" ||
    value.trim().length === 0
  ) {
    return null;
  }

  return value;
}

function shouldReviewsBeActive(
  order: OrderDocument | undefined
): boolean {
  if (!order) {
    return false;
  }

  return (
    order.sinpePaid === true &&
    order.status !== "CANCELLED"
  );
}

function addTransactionTarget(
  targets: Map<string, TransactionTarget>,
  userId: unknown,
  role: TransactionRole
): void {
  const validUserId = getStringValue(userId);

  if (!validUserId) {
    return;
  }

  targets.set(
    `${validUserId}_${role}`,
    {
      userId: validUserId,
      role,
    }
  );
}

async function updateOrderReviewVisibility(
  orderId: string,
  isActive: boolean
): Promise<void> {
  const firestore = getFirestore();

  const reviewsSnapshot = await firestore
    .collection("reviews")
    .where("orderId", "==", orderId)
    .get();

  if (reviewsSnapshot.empty) {
    logger.info(
      "No reviews were found for this order.",
      {
        orderId,
        isActive,
      }
    );

    return;
  }

  const batch = firestore.batch();
  let updatedReviews = 0;

  for (const reviewSnapshot of reviewsSnapshot.docs) {
    const currentIsActive =
      reviewSnapshot.get("isActive");

    if (currentIsActive === isActive) {
      continue;
    }

    batch.update(
      reviewSnapshot.ref,
      {
        isActive,
      }
    );

    updatedReviews++;
  }

  if (updatedReviews === 0) {
    return;
  }

  await batch.commit();

  logger.info(
    "Order review visibility updated.",
    {
      orderId,
      isActive,
      updatedReviews,
    }
  );
}

async function recalculateCompletedTransactions(
  target: TransactionTarget
): Promise<void> {
  const firestore = getFirestore();

  const participantField =
    target.role === "SELLER" ?
      "sellerId" :
      "buyerId";

  const ordersSnapshot = await firestore
    .collection("orders")
    .where(participantField, "==", target.userId)
    .where("status", "==", "PICKED_UP")
    .get();

  const completedTransactionCount =
    ordersSnapshot.size;

  const fields =
    target.role === "SELLER" ?
      {
        sellerCompletedTransactionCount:
          completedTransactionCount,
      } :
      {
        buyerCompletedTransactionCount:
          completedTransactionCount,
      };

  await firestore
    .collection("user_ratings")
    .doc(target.userId)
    .set(
      {
        userId: target.userId,
        ...fields,
        updatedAt: Date.now(),
      },
      {
        merge: true,
      }
    );

  logger.info(
    "Completed transaction count updated.",
    {
      userId: target.userId,
      role: target.role,
      completedTransactionCount,
    }
  );
}

function completedInformationChanged(
  before: OrderDocument | undefined,
  after: OrderDocument | undefined
): boolean {
  return (
    before?.status !== after?.status ||
    before?.buyerId !== after?.buyerId ||
    before?.sellerId !== after?.sellerId
  );
}

export const updateOrderReputation =
  onDocumentWritten(
    "orders/{orderId}",
    async (event): Promise<void> => {
      const change = event.data;

      if (!change) {
        logger.warn(
          "The order event did not contain document data.",
          {
            orderId: event.params.orderId,
          }
        );

        return;
      }

      const beforeOrder: OrderDocument | undefined =
        change.before.exists ?
          change.before.data() as OrderDocument :
          undefined;

      const afterOrder: OrderDocument | undefined =
        change.after.exists ?
          change.after.data() as OrderDocument :
          undefined;

      const wasActive =
        shouldReviewsBeActive(beforeOrder);

      const isActive =
        shouldReviewsBeActive(afterOrder);

      /*
       * Solo modifica reviews cuando cambia su visibilidad.
       */
      if (wasActive !== isActive) {
        await updateOrderReviewVisibility(
          event.params.orderId,
          isActive
        );
      }

      /*
       * Los contadores solo necesitan recalcularse
       * cuando cambia status o algún participante.
       */
      if (
        !completedInformationChanged(
          beforeOrder,
          afterOrder
        )
      ) {
        return;
      }

      const targets =
        new Map<string, TransactionTarget>();

      addTransactionTarget(
        targets,
        beforeOrder?.buyerId,
        "BUYER"
      );

      addTransactionTarget(
        targets,
        beforeOrder?.sellerId,
        "SELLER"
      );

      addTransactionTarget(
        targets,
        afterOrder?.buyerId,
        "BUYER"
      );

      addTransactionTarget(
        targets,
        afterOrder?.sellerId,
        "SELLER"
      );

      await Promise.all(
        Array.from(targets.values()).map(
          (target) =>
            recalculateCompletedTransactions(target)
        )
      );
    }
  );