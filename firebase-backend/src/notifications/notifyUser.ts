import { getFirestore, FieldValue } from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { logger } from "firebase-functions/v2";
import { Collections } from "../contract";

export interface NotifyUserInput {
  userId: string;
  type: string;
  title: string;
  body: string;
  data?: Record<string, string>;
}

export async function notifyUser(input: NotifyUserInput): Promise<void> {
  const db = getFirestore();
  const data = input.data ?? {};

  await db.collection(Collections.notifications).add({
    userId: input.userId,
    type: input.type,
    title: input.title,
    body: input.body,
    data,
    read: false,
    createdAt: Date.now(),
  });

  const userRef = db.collection(Collections.users).doc(input.userId);
  const userSnap = await userRef.get();
  const tokens: string[] = userSnap.get("fcmTokens") ?? [];

  if (tokens.length === 0) {
    logger.info(`notifyUser: ${input.userId} sin tokens FCM, solo notificación in-app.`);
    return;
  }

  const response = await getMessaging().sendEachForMulticast({
    tokens,
    notification: { title: input.title, body: input.body },
    data: { type: input.type, ...data },
    android: { priority: "high" },
  });

  // Tokens de dispositivos desinstalados/expirados: se limpian para no reintentarlos.
  const invalidTokens = tokens.filter((_, i) => {
    const error = response.responses[i].error;
    return (
      error?.code === "messaging/registration-token-not-registered" ||
      error?.code === "messaging/invalid-argument"
    );
  });

  if (invalidTokens.length > 0) {
    await userRef.update({ fcmTokens: FieldValue.arrayRemove(...invalidTokens) });
    logger.info(`notifyUser: ${invalidTokens.length} tokens inválidos removidos de ${input.userId}.`);
  }
}
