import { onCall, HttpsError } from "firebase-functions/v2/https";
import { logger } from "firebase-functions";
import { defineSecret } from "firebase-functions/params";
import { getApps, initializeApp } from "firebase-admin/app";
import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import * as crypto from "crypto";
import QRCode from "qrcode";

if (!getApps().length) {
  initializeApp();
}

const db = getFirestore();
const bucket = getStorage().bucket();

const QR_HMAC_SECRET = defineSecret("QR_HMAC_SECRET");


const ORDER_COLLECTION = "orders";

type GenerateOrderQrRequest = {
  orderId: string;
  forceRegenerate?: boolean;
};

type GenerateOrderQrResponse = {
  orderId: string;
  qrId: string;
  qrImagePath: string;
  qrImageUrl: string;
  reused: boolean;
  status: "ACTIVE";
};
type QrLockResult =
  | {
      shouldGenerate: false;
      qrId: string;
      qrImagePath: string;
      qrImageUrl: string;
    }
  | {
      shouldGenerate: true;
      qrLockId: string;
    };

function generateSecureToken(): string {
  return crypto.randomBytes(32).toString("base64url");
}

function generateQrId(): string {
  return crypto.randomUUID();
}

function signQr(orderId: string, token: string): string {
  const secret = QR_HMAC_SECRET.value();

  return crypto
    .createHmac("sha256", secret)
    .update(`${orderId}.${token}`)
    .digest("hex");
}

function buildQrPayload(orderId: string, token: string, signature: string): string {
  return `flydevs://order-qr?oid=${encodeURIComponent(orderId)}&t=${encodeURIComponent(token)}&s=${encodeURIComponent(signature)}`;
}

async function retry<T>(
  action: () => Promise<T>,
  maxAttempts = 3
): Promise<T> {
  let lastError: unknown;

  for (let attempt = 1; attempt <= maxAttempts; attempt++) {
    try {
      return await action();
    } catch (error) {
      lastError = error;

      logger.error("QR generation attempt failed", {
        attempt,
        maxAttempts,
        error,
      });

      await new Promise((resolve) => setTimeout(resolve, 500 * attempt));
    }
  }

  throw lastError;
}

export const generateOrderQr = onCall(
  {
    region: "us-central1",
    secrets: [QR_HMAC_SECRET],
  },
  async (request): Promise<GenerateOrderQrResponse> => {
    if (!request.auth) {
      throw new HttpsError(
        "unauthenticated",
        "User must be authenticated to generate order QR."
      );
    }

    const data = request.data as GenerateOrderQrRequest;
    const orderId = data.orderId;
    const forceRegenerate = data.forceRegenerate === true;

    if (!orderId || typeof orderId !== "string") {
      throw new HttpsError(
        "invalid-argument",
        "orderId is required."
      );
    }

    const orderRef = db.collection(ORDER_COLLECTION).doc(orderId);

    try {
      return await retry(async () => {
        const now = Date.now();

        const lockResult: QrLockResult = await db.runTransaction(
          async (transaction): Promise<QrLockResult> => {
            const orderSnap = await transaction.get(orderRef);

          if (!orderSnap.exists) {
            throw new HttpsError(
              "not-found",
              `Order not found: ${orderId}`
            );
          }

          const order = orderSnap.data() || {};

          const currentQrId = order.currentQrId as string | undefined;
          const qrImagePath = order.qrImagePath as string | undefined;
          const qrStatus = order.qrStatus as string | undefined;
          const qrExpiresAt = order.qrExpiresAt as number | undefined;
          const qrImageUrl = order.qrImageUrl as string | undefined;

         if (!forceRegenerate && qrStatus === "ACTIVE" && currentQrId && qrImagePath && qrImageUrl &&
                     (!qrExpiresAt || qrExpiresAt > now)
                   ) {
                     return {
                       shouldGenerate: false,
                       qrId: currentQrId,
                       qrImagePath: qrImagePath,
                       qrImageUrl,
                     };
                   }

          const qrLockId = generateQrId();

          transaction.update(orderRef, {
            qrStatus: "CREATING",
            qrLockId,
            qrLastError: null,
            qrLastAttemptAt: now,
            updatedAt: FieldValue.serverTimestamp(),
          });

          return {
            shouldGenerate: true,
            qrLockId,
          };
        });

        if (lockResult.shouldGenerate === false) {
          return {
            orderId,
            qrId: lockResult.qrId,
            qrImagePath: lockResult.qrImagePath,
            qrImageUrl: lockResult.qrImageUrl,
            reused: true,
            status: "ACTIVE",
          };
        }

        const qrId = generateQrId();
        const token = generateSecureToken();
        const signature = signQr(orderId, token);
        const qrPayload = buildQrPayload(orderId, token, signature);

        const qrBuffer = await QRCode.toBuffer(qrPayload, {
          type: "png",
          width: 512,
          margin: 2,
          errorCorrectionLevel: "M",
        });

        const qrImagePath = `exchange_qrs/${orderId}/${qrId}.png`;
        const file = bucket.file(qrImagePath);

        const downloadToken = generateQrId();

        await file.save(qrBuffer, {
          contentType: "image/png",
          metadata: {
            cacheControl: "private, max-age=0, no-transform",
            metadata: {
              firebaseStorageDownloadTokens: downloadToken,
            },
          },
        });

        const encodedPath = encodeURIComponent(qrImagePath);

        const qrImageUrl =
          `https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodedPath}?alt=media&token=${downloadToken}`;

        const qrRef = orderRef.collection("qrs").doc(qrId);
        const tokenRef = db.collection("qr_tokens").doc(signature);

        await db.runTransaction(async (transaction) => {
          const orderSnap = await transaction.get(orderRef);

          if (!orderSnap.exists) {
            throw new HttpsError(
              "not-found",
              `Order not found after QR upload: ${orderId}`
            );
          }

          const order = orderSnap.data() || {};

          if (order.qrLockId !== lockResult.qrLockId) {
            throw new HttpsError(
              "aborted",
              "QR generation lock was replaced by another process."
            );
          }

          transaction.create(tokenRef, {
            orderId,
            qrId,
            status: "ACTIVE",
            createdAt: now,
            createdBy: request.auth?.uid || null,
          });

          transaction.set(qrRef, {
            qrId,
            orderId,
            signature,
            imagePath: qrImagePath,
            imageUrl: qrImageUrl,
            status: "ACTIVE",
            createdAt: now,
            expiresAt: null,
            createdBy: request.auth?.uid || null,
          });

          transaction.update(orderRef, {
            currentQrId: qrId,
            qrStatus: "ACTIVE",
            qrSignature: signature,
            qrImagePath,
            qrImageUrl,
            qrCreatedAt: now,
            qrExpiresAt: null,
            qrLastError: null,
            qrLockId: null,
            updatedAt: FieldValue.serverTimestamp(),
          });
        });

        return {
          orderId,
          qrId,
          qrImagePath,
          qrImageUrl,
          reused: false,
          status: "ACTIVE",
        };
      });
    } catch (error: any) {
      logger.error("Final QR generation failure", {
        orderId,
        error,
      });

      await orderRef.set(
        {
          qrStatus: "ERROR",
          qrLastError: error?.message || "Unknown QR generation error",
          qrLastErrorAt: Date.now(),
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      if (error instanceof HttpsError) {
        throw error;
      }

      throw new HttpsError(
        "internal",
        "QR generation failed. The error was logged."
      );
    }
  }
);