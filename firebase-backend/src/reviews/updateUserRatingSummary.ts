import {getFirestore} from "firebase-admin/firestore";
import {onDocumentWritten} from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";

type ReviewRole = "BUYER" | "SELLER";

interface ReviewDocument {
  reviewedUserId?: unknown;
  reviewedRole?: unknown;
  rating?: unknown;
  comment?: unknown;
  isActive?: unknown;
}

interface RatingTarget {
  userId: string;
  role: ReviewRole;
}

interface RatingDistribution {
  oneStarCount: number;
  twoStarCount: number;
  threeStarCount: number;
  fourStarCount: number;
  fiveStarCount: number;
}

function getRatingTarget(
  review: ReviewDocument | undefined
): RatingTarget | null {
  if (!review) {
    return null;
  }

  const reviewedUserId = review.reviewedUserId;
  const reviewedRole = review.reviewedRole;

  if (
    typeof reviewedUserId !== "string" ||
    reviewedUserId.trim().length === 0
  ) {
    return null;
  }

  if (
    reviewedRole !== "BUYER" &&
    reviewedRole !== "SELLER"
  ) {
    return null;
  }

  return {
    userId: reviewedUserId,
    role: reviewedRole,
  };
}

function isValidRating(
  rating: unknown
): rating is number {
  return (
    typeof rating === "number" &&
    Number.isFinite(rating) &&
    rating >= 1 &&
    rating <= 5 &&
    rating * 2 === Math.floor(rating * 2)
  );
}

/**
 * Las reseñas antiguas que todavía no tengan
 * isActive se consideran activas temporalmente.
 */
function isActiveReview(
  review: ReviewDocument
): boolean {
  return review.isActive !== false;
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
  if (rating >= 4.5) {
    return 5;
  }

  if (rating >= 3.5) {
    return 4;
  }

  if (rating >= 2.5) {
    return 3;
  }

  if (rating >= 1.5) {
    return 2;
  }

  return 1;
}

/**
 * Ahora el comentario también afecta el resumen,
 * porque almacenamos commentCount.
 */
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
  const firestore = getFirestore();
  const {userId, role} = target;

  const reviewsSnapshot = await firestore
    .collection("reviews")
    .where("reviewedUserId", "==", userId)
    .where("reviewedRole", "==", role)
    .get();

  let ratingTotal = 0;
  let validReviewCount = 0;
  let commentCount = 0;

  const distribution: RatingDistribution = {
    oneStarCount: 0,
    twoStarCount: 0,
    threeStarCount: 0,
    fourStarCount: 0,
    fiveStarCount: 0,
  };

  for (const reviewSnapshot of reviewsSnapshot.docs) {
    const review =
      reviewSnapshot.data() as ReviewDocument;

    /*
     * Las reseñas canceladas permanecen almacenadas,
     * pero no afectan la reputación.
     */
    if (!isActiveReview(review)) {
      continue;
    }

    const rating = review.rating;

    if (!isValidRating(rating)) {
      logger.warn(
        "A review with an invalid rating was ignored.",
        {
          reviewId: reviewSnapshot.id,
          reviewedUserId: userId,
          reviewedRole: role,
          rating,
        }
      );

      continue;
    }

    ratingTotal += rating;
    validReviewCount++;

    if (hasComment(review.comment)) {
      commentCount++;
    }

    const bucket = getStarBucket(rating);

    whenStarBucket(bucket, distribution);
  }

  const averageRating =
    validReviewCount === 0 ?
      0 :
      ratingTotal / validReviewCount;

  const roundedAverage =
    Math.round(averageRating * 100) / 100;

  const ratingFields =
    role === "SELLER" ?
      {
        sellerAverageRating: roundedAverage,
        sellerReviewCount: validReviewCount,
        sellerCommentCount: commentCount,

        sellerOneStarCount:
          distribution.oneStarCount,

        sellerTwoStarCount:
          distribution.twoStarCount,

        sellerThreeStarCount:
          distribution.threeStarCount,

        sellerFourStarCount:
          distribution.fourStarCount,

        sellerFiveStarCount:
          distribution.fiveStarCount,
      } :
      {
        buyerAverageRating: roundedAverage,
        buyerReviewCount: validReviewCount,
        buyerCommentCount: commentCount,

        buyerOneStarCount:
          distribution.oneStarCount,

        buyerTwoStarCount:
          distribution.twoStarCount,

        buyerThreeStarCount:
          distribution.threeStarCount,

        buyerFourStarCount:
          distribution.fourStarCount,

        buyerFiveStarCount:
          distribution.fiveStarCount,
      };

  await firestore
    .collection("user_ratings")
    .doc(userId)
    .set(
      {
        userId,
        ...ratingFields,
        updatedAt: Date.now(),
      },
      {
        merge: true,
      }
    );

  logger.info(
    "User rating summary updated successfully.",
    {
      userId,
      role,
      averageRating: roundedAverage,
      reviewCount: validReviewCount,
      commentCount,
      distribution,
    }
  );
}

function whenStarBucket(
  bucket: 1 | 2 | 3 | 4 | 5,
  distribution: RatingDistribution
): void {
  switch (bucket) {
  case 5:
    distribution.fiveStarCount++;
    break;

  case 4:
    distribution.fourStarCount++;
    break;

  case 3:
    distribution.threeStarCount++;
    break;

  case 2:
    distribution.twoStarCount++;
    break;

  case 1:
    distribution.oneStarCount++;
    break;
  }
}

export const updateUserRatingSummary =
  onDocumentWritten(
    "reviews/{reviewId}",
    async (event): Promise<void> => {
      const change = event.data;

      if (!change) {
        logger.warn(
          "The Firestore event did not contain document data.",
          {
            reviewId: event.params.reviewId,
          }
        );

        return;
      }

      const beforeReview: ReviewDocument | undefined =
        change.before.exists ?
          change.before.data() as ReviewDocument :
          undefined;

      const afterReview: ReviewDocument | undefined =
        change.after.exists ?
          change.after.data() as ReviewDocument :
          undefined;

      /*
       * Si solamente cambió updatedAt,
       * no hay que recalcular.
       */
      if (
        !summaryInformationChanged(
          beforeReview,
          afterReview
        )
      ) {
        logger.info(
          "Review changed without affecting the summary.",
          {
            reviewId: event.params.reviewId,
          }
        );

        return;
      }

      const targets =
        new Map<string, RatingTarget>();

      const beforeTarget =
        getRatingTarget(beforeReview);

      const afterTarget =
        getRatingTarget(afterReview);

      if (beforeTarget) {
        targets.set(
          `${beforeTarget.userId}_${beforeTarget.role}`,
          beforeTarget
        );
      }

      if (afterTarget) {
        targets.set(
          `${afterTarget.userId}_${afterTarget.role}`,
          afterTarget
        );
      }

      if (targets.size === 0) {
        logger.warn(
          "No valid rating target was found.",
          {
            reviewId: event.params.reviewId,
          }
        );

        return;
      }

      await Promise.all(
        Array.from(targets.values()).map(
          (target) =>
            recalculateUserRating(target)
        )
      );
    }
  );