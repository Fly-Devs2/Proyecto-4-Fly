import {getFirestore, FieldValue} from "firebase-admin/firestore";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";

type ReviewRole = "BUYER" | "SELLER";

interface ReviewDocument {
  reviewedUserId?: unknown;
  reviewedRole?: unknown;
  rating?: unknown;
  comment?: unknown;
  isActive?: unknown;
  updatedAt?: unknown;
  createdAt?: unknown;
}

interface RatingTarget {
  userId: string;
  role: ReviewRole;
}

function getRatingTarget(
  review: ReviewDocument | undefined
): RatingTarget | null {
  if (!review) return null;
  const reviewedUserId = review.reviewedUserId;
  const reviewedRole = review.reviewedRole;
  if (typeof reviewedUserId !== "string" || reviewedUserId.trim().length === 0) return null;
  if (reviewedRole !== "BUYER" && reviewedRole !== "SELLER") return null;
  return {userId: reviewedUserId, role: reviewedRole};
}

function isValidRating(
  rating: unknown
): rating is number {
  const r = Number(rating);
  return (
    !isNaN(r) &&
    Number.isFinite(r) &&
    r >= 0 && // Be lenient with 0-star reviews if they exist
    r <= 5
  );
}

function hasComment(
  comment: unknown
): boolean {
  return (
    typeof comment === "string" &&
    comment.trim().length > 0
  );
}

function getStarBucket(
  rating: number
): 1 | 2 | 3 | 4 | 5 {
  if (rating >= 4.5) return 5;
  if (rating >= 3.5) return 4;
  if (rating >= 2.5) return 3;
  if (rating >= 1.5) return 2;
  return 1;
}

function summaryInformationChanged(
  before: ReviewDocument | undefined,
  after: ReviewDocument | undefined
): boolean {
  return (
    before?.reviewedUserId !== after?.reviewedUserId ||
    before?.reviewedRole !== after?.reviewedRole ||
    before?.rating !== after?.rating ||
    before?.comment !== after?.comment ||
    before?.isActive !== after?.isActive
  );
}

async function recalculateUserRating(
  target: RatingTarget
): Promise<void> {
  const db = getFirestore();
  const {userId, role} = target;

  // Fetch ALL active reviews for this user to filter in-memory (resilient to missing timestamps)
  const reviewsSnap = await db.collection("reviews")
    .where("reviewedUserId", "==", userId)
    .where("reviewedRole", "==", role)
    .where("isActive", "==", true)
    .get();

  const now = Date.now();
  const yearMs = 365 * 24 * 60 * 60 * 1000;
  const monthMs = 30 * 24 * 60 * 60 * 1000;

  const buckets = {
    allTime: createEmptyBucket(),
    lastYear: createEmptyBucket(),
    last30Days: createEmptyBucket(),
  };

  reviewsSnap.forEach((doc) => {
    const data = doc.data() as ReviewDocument;
    const rating = Number(data.rating);

    if (isValidRating(rating)) {
      // Use updatedAt or fallback to createdAt or 0
      const timestamp = Number(data.updatedAt) || Number(data.createdAt) || 0;
      const age = now - timestamp;

      addToBucket(buckets.allTime, rating, data.comment);
      if (age <= yearMs) addToBucket(buckets.lastYear, rating, data.comment);
      if (age <= monthMs) addToBucket(buckets.last30Days, rating, data.comment);
    }
  });

  const prefix = role === "SELLER" ? "seller" : "buyer";
  const docRef = db.collection("user_ratings").doc(userId);

  await db.runTransaction(async (transaction) => {
    const doc = await transaction.get(docRef);
    const existingData = doc.data() || {};

    const update: any = {
      userId,
      updatedAt: now,
    };

    // ── Legacy Migration ──
    // If we detect legacy fields at the root, we migrate them and delete the old ones.
    const hasLegacyFields = existingData.sellerAverageRating !== undefined ||
                          existingData.buyerAverageRating !== undefined ||
                          existingData.sellerReviewCount !== undefined;

    if (hasLegacyFields) {
      logger.info(`Migrating legacy user_ratings document for ${userId}.`);

      // Ensure root-level totals are initialized if we are moving from flat schema
      if (existingData.totalCompletedTransactionCount === undefined) {
        update.totalCompletedTransactionCount =
          (Number(existingData.sellerCompletedTransactionCount) || 0) +
          (Number(existingData.buyerCompletedTransactionCount) || 0);
      }

      if (existingData.totalSalesCount === undefined) {
        update.totalSalesCount = Number(existingData.sellerSalesCount) || 0;
      }

      // Mark legacy fields for deletion
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

    // ── Apply New Stats ──
    Object.keys(buckets).forEach((key) => {
      const b = (buckets as any)[key];
      const avg = b.count === 0 ? 0 : Math.round((b.total / b.count) * 100) / 100;

      update[`${key}.${prefix}AverageRating`] = avg;
      update[`${key}.${prefix}ReviewCount`] = b.count;
      update[`${key}.${prefix}CommentCount`] = b.commentCount;

      // Consistent naming matching Kotlin model: sellerFiveStarCount, etc.
      update[`${key}.${prefix}FiveStarCount`] = b.dist.fiveStarCount;
      update[`${key}.${prefix}FourStarCount`] = b.dist.fourStarCount;
      update[`${key}.${prefix}ThreeStarCount`] = b.dist.threeStarCount;
      update[`${key}.${prefix}TwoStarCount`] = b.dist.twoStarCount;
      update[`${key}.${prefix}OneStarCount`] = b.dist.oneStarCount;
    });

    // Use set with merge to ensure nested objects are updated correctly
    transaction.set(docRef, update, {merge: true});
  });

  logger.info(`Updated rating summary for ${userId} (${role}).`);
}

function createEmptyBucket() {
  return {
    total: 0, count: 0, commentCount: 0,
    dist: {oneStarCount: 0, twoStarCount: 0, threeStarCount: 0, fourStarCount: 0, fiveStarCount: 0}
  };
}

function addToBucket(bucket: any, rating: number, comment: any) {
  bucket.total += rating;
  bucket.count++;
  if (hasComment(comment)) bucket.commentCount++;
  const star = getStarBucket(rating);
  switch (star) {
    case 5: bucket.dist.fiveStarCount++; break;
    case 4: bucket.dist.fourStarCount++; break;
    case 3: bucket.dist.threeStarCount++; break;
    case 2: bucket.dist.twoStarCount++; break;
    case 1: bucket.dist.oneStarCount++; break;
  }
}

export const updateUserRatingSummary =
  onDocumentWritten(
    "reviews/{reviewId}",
    async (event): Promise<void> => {
      const change = event.data;
      if (!change) return;

      const before = change.before.data() as ReviewDocument | undefined;
      const after = change.after.data() as ReviewDocument | undefined;

      if (!summaryInformationChanged(before, after)) return;

      const targets = new Map<string, RatingTarget>();
      const bTarget = getRatingTarget(before);
      const aTarget = getRatingTarget(after);

      if (bTarget) targets.set(`${bTarget.userId}_${bTarget.role}`, bTarget);
      if (aTarget) targets.set(`${aTarget.userId}_${aTarget.role}`, aTarget);

      await Promise.all(
        Array.from(targets.values()).map((t) => recalculateUserRating(t))
      );
    }
  );
