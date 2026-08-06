import {getFirestore, FieldValue} from "firebase-admin/firestore";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";

interface OrderDocument {
  buyerId?: unknown;
  sellerId?: unknown;
  status?: unknown;
  sinpePaid?: unknown;
  sellerEvidenceUrls?: unknown;
  buyerEvidenceUrls?: unknown;
  modifiedAt?: unknown;
}

interface TransactionTarget {
  userId: string;
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
  userId: unknown
): void {
  const validUserId = getStringValue(userId);

  if (!validUserId) {
    return;
  }

  targets.set(validUserId, {userId: validUserId});
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

/**
 * Calcula estadísticas de transacciones (SINPE + Evidence) y ventas (PICKED_UP).
 * Filtering in-memory to be resilient to missing modifiedAt fields.
 */
async function calculateStats(
  userId: string,
  role: "buyer" | "seller"
): Promise<{allTime: any, lastYear: any, last30Days: any}> {
  const db = getFirestore();
  const field = role === "buyer" ? "buyerId" : "sellerId";

  // Fetch ALL relevant orders for this user
  const ordersSnap = await db.collection("orders")
    .where(field, "==", userId)
    .get();

  const now = Date.now();
  const yearMs = 365 * 24 * 60 * 60 * 1000;
  const monthMs = 30 * 24 * 60 * 60 * 1000;

  const results = {
    allTime: {transactions: 0, sales: 0},
    lastYear: {transactions: 0, sales: 0},
    last30Days: {transactions: 0, sales: 0},
  };

  ordersSnap.forEach((doc) => {
    const data = doc.data() as OrderDocument;

    // Status-based check for Sales
    const isSale = role === "seller" && data.status === "PICKED_UP";

    // Evidence-based check for Transactions
    const hasSellerEvidence = Array.isArray(data.sellerEvidenceUrls) && data.sellerEvidenceUrls.length > 0;
    const hasBuyerEvidence = Array.isArray(data.buyerEvidenceUrls) && data.buyerEvidenceUrls.length > 0;
    const isTransaction = data.sinpePaid === true && (hasSellerEvidence || hasBuyerEvidence);

    // Fallback to 0 if modifiedAt is missing
    const modifiedAt = Number(data.modifiedAt) || 0;
    const age = now - modifiedAt;

    if (isSale) {
      results.allTime.sales++;
      if (age <= yearMs) results.lastYear.sales++;
      if (age <= monthMs) results.last30Days.sales++;
    }

    if (isTransaction) {
      results.allTime.transactions++;
      if (age <= yearMs) results.lastYear.transactions++;
      if (age <= monthMs) results.last30Days.transactions++;
    }
  });

  return results;
}

async function recalculateUserStats(
  userId: string
): Promise<void> {
  const db = getFirestore();
  const docRef = db.collection("user_ratings").doc(userId);

  const [buyerStats, sellerStats] = await Promise.all([
    calculateStats(userId, "buyer"),
    calculateStats(userId, "seller"),
  ]);

  const now = Date.now();

  await db.runTransaction(async (transaction) => {
    const doc = await transaction.get(docRef);
    const existingData = doc.data() || {};

    // Use dot notation to avoid overwriting rating fields in nested objects
    const update: any = {
      userId,
      updatedAt: now,
      totalCompletedTransactionCount: sellerStats.allTime.transactions + buyerStats.allTime.transactions,
      totalSalesCount: sellerStats.allTime.sales,

      "allTime.buyerCompletedTransactionCount": buyerStats.allTime.transactions,
      "allTime.sellerCompletedTransactionCount": sellerStats.allTime.transactions,
      "allTime.sellerSalesCount": sellerStats.allTime.sales,

      "lastYear.buyerCompletedTransactionCount": buyerStats.lastYear.transactions,
      "lastYear.sellerCompletedTransactionCount": sellerStats.lastYear.transactions,
      "lastYear.sellerSalesCount": sellerStats.lastYear.sales,

      "last30Days.buyerCompletedTransactionCount": buyerStats.last30Days.transactions,
      "last30Days.sellerCompletedTransactionCount": sellerStats.last30Days.transactions,
      "last30Days.sellerSalesCount": sellerStats.last30Days.sales,
    };

    // ── Legacy Migration & Cleanup ──
    const hasLegacyFields = existingData.sellerCompletedTransactionCount !== undefined ||
                          existingData.buyerCompletedTransactionCount !== undefined ||
                          existingData.sellerAverageRating !== undefined;

    if (hasLegacyFields) {
      logger.info(`Migrating legacy user_ratings document for ${userId} (Order Stats).`);

      const fieldsToDelete = [
        "sellerAverageRating", "sellerReviewCount", "sellerCommentCount",
        "sellerCompletedTransactionCount", "sellerSalesCount",
        "sellerFiveStarCount", "sellerFourStarCount", "sellerThreeStarCount",
        "sellerTwoStarCount", "sellerOneStarCount",
        "buyerAverageRating", "buyerReviewCount", "buyerCommentCount",
        "buyerCompletedTransactionCount",
        "buyerFiveStarCount", "buyerFourStarCount", "buyerThreeStarCount",
        "buyerTwoStarCount", "buyerOneStarCount"
      ];

      fieldsToDelete.forEach(f => {
        if (existingData[f] !== undefined) {
          update[f] = FieldValue.delete();
        }
      });
    }

    transaction.set(docRef, update, {merge: true});
  });

  logger.info(`Updated order stats for ${userId}. Total Trans: ${sellerStats.allTime.transactions + buyerStats.allTime.transactions}`);
}

function completedInformationChanged(
  before: OrderDocument | undefined,
  after: OrderDocument | undefined
): boolean {
  return (
    before?.status !== after?.status ||
    before?.buyerId !== after?.buyerId ||
    before?.sellerId !== after?.sellerId ||
    before?.sinpePaid !== after?.sinpePaid ||
    (Array.isArray(before?.sellerEvidenceUrls) && Array.isArray(after?.sellerEvidenceUrls) &&
     before?.sellerEvidenceUrls.length !== after?.sellerEvidenceUrls.length) ||
    (Array.isArray(before?.buyerEvidenceUrls) && Array.isArray(after?.buyerEvidenceUrls) &&
     before?.buyerEvidenceUrls.length !== after?.buyerEvidenceUrls.length)
  );
}

export const updateOrderReputation =
  onDocumentWritten(
    "orders/{orderId}",
    async (event): Promise<void> => {
      const change = event.data;

      if (!change) return;

      const beforeOrder = change.before.data() as OrderDocument | undefined;
      const afterOrder = change.after.data() as OrderDocument | undefined;

      const wasActive = shouldReviewsBeActive(beforeOrder);
      const isActive = shouldReviewsBeActive(afterOrder);

      if (wasActive !== isActive) {
        await updateOrderReviewVisibility(event.params.orderId, isActive);
      }

      if (!completedInformationChanged(beforeOrder, afterOrder)) {
        return;
      }

      const targets = new Map<string, TransactionTarget>();
      addTransactionTarget(targets, beforeOrder?.buyerId);
      addTransactionTarget(targets, beforeOrder?.sellerId);
      addTransactionTarget(targets, afterOrder?.buyerId);
      addTransactionTarget(targets, afterOrder?.sellerId);

      await Promise.all(
        Array.from(targets.values()).map((target) => recalculateUserStats(target.userId))
      );
    }
  );
