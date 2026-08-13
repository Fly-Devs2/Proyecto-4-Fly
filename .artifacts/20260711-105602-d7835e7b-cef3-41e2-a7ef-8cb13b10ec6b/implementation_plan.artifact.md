# Implementation Plan - Base64 QR Email Fix (Gmail Mobile Support)

This plan fixes the issue where QR codes are invisible in the Gmail mobile app by embedding the QR image as a **Base64 Data URI** directly in the email HTML. This bypasses the Gmail Image Proxy that often blocks Firebase Storage links.

## Proposed Changes

### Firebase Backend

#### [generateOrderQr.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/exchange/generateOrderQr.ts)

- Update the QR generation logic to also store a `qrBase64` string in the order document.
- Use a smaller width (e.g., 256px) for the base64 version to keep the document size small.

```diff
         const qrBuffer = await QRCode.toBuffer(qrPayload, {
           type: "png",
-          width: 512,
+          width: 256,
           margin: 2,
           errorCorrectionLevel: "M",
         });
+        const qrBase64 = `data:image/png;base64,${qrBuffer.toString("base64")}`;
```

#### [onOrderStatusChange.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/notifications/onOrderStatusChange.ts)

- Pass the `qrBase64` field from the order to the `sendOrderQrEmail` helper.

```diff
-        const qrImageUrl = after.qrImageUrl;
+        const qrImageUrl = after.qrImageUrl;
+        const qrBase64 = after.qrBase64;

-        if (userEmail && qrImageUrl) {
+        if (userEmail && (qrBase64 || qrImageUrl)) {
           try {
             await sendOrderQrEmail({
               email: userEmail,
               orderId: event.params.orderId,
-              qrImageUrl: qrImageUrl,
+              qrImageUrl: qrImageUrl,
+              qrBase64: qrBase64,
             });
```

#### [sendOrderQrEmail.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/notifications/sendOrderQrEmail.ts)

- Update the HTML template to use the `qrBase64` string as the primary image source.
- Keep the `qrImageUrl` as a fallback text link.

```typescript
export interface SendOrderQrEmailInput {
  email: string;
  orderId: string;
  qrImageUrl: string;
  qrBase64?: string;
}

// ... inside the template ...
<img src="${input.qrBase64 || input.qrImageUrl}"
     alt="Código QR de Retiro"
     width="250"
     height="250"
     style="display: block; margin: 0 auto; border: 2px solid #7C5CFF; border-radius: 10px;" />
```

## Verification Plan

### Automated Tests
- Build success: `cd firebase-backend && npm run build`

### Manual Verification
1. Manually add a `qrBase64` string to an existing order in Firestore.
2. Trigger the status change to `WAITING_STORE_SHIPMENT`.
3. Verify that the received email in the Gmail mobile app now displays the QR code.
4. Verify that the fallback link still works.
