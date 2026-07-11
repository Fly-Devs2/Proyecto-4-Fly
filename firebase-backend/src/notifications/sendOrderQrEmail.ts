import { getFirestore } from "firebase-admin/firestore";
import { logger } from "firebase-functions/v2";
import { Collections } from "../contract";

export interface SendOrderQrEmailInput {
  email: string;
  orderId: string;
  qrImageUrl: string;
  qrBase64?: string;
}

/**
 * Encola un documento en la colección 'mail' para ser procesado por la extensión 'Trigger Email'.
 * HTML optimizado para visualización en Gmail Mobile usando Base64.
 */
export async function sendOrderQrEmail(input: SendOrderQrEmailInput): Promise<void> {
  const db = getFirestore();

  const mailDoc = {
    to: input.email,
    message: {
      subject: `Fly-App: QR de retiro de la orden ${input.orderId}`,
      html: `
        <div style="font-family: sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px; max-width: 600px; margin: auto;">
          <h2 style="color: #7C5CFF; text-align: center;">¡Tu pedido está listo para retiro!</h2>
          <p>Hola,</p>
          <p>Tu pago para la orden <strong>#${input.orderId.substring(0, 7).toUpperCase()}</strong> ha sido verificado.</p>
          <p>Presenta este código QR en la tienda destino para retirar tu paquete:</p>

          <div style="text-align: center; margin: 30px 0;">
            <img src="${input.qrBase64 || input.qrImageUrl}"
                 alt="Código QR de Retiro"
                 width="250"
                 height="250"
                 style="display: block; margin: 0 auto; border: 2px solid #7C5CFF; border-radius: 10px;" />
            <p style="margin-top: 15px;">
              <a href="${input.qrImageUrl}" style="color: #7C5CFF; text-decoration: underline;">Ver código QR en el navegador</a>
            </p>
          </div>

          <p style="font-size: 12px; color: #666; text-align: center; border-top: 1px solid #eee; padding-top: 10px;">
            Este código es confidencial y solo debe ser mostrado al personal de la tienda.
          </p>
        </div>
      `,
    },
  };

  try {
    await db.collection(Collections.mail).add(mailDoc);
    logger.info(`Documento de email encolado exitosamente para ${input.email} (Orden: ${input.orderId})`);
  } catch (error) {
    logger.error(`Error al encolar email para ${input.email}:`, error);
    throw error;
  }
}
