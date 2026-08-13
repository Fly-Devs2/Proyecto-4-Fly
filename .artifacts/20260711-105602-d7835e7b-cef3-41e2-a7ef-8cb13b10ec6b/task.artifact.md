# Task: Implement Order QR Email Notification

- [x] Research existing backend implementation (Status, QR, Notifications)
- [x] Create implementation plan
- [x] Implement email notification feature
	- [x] Update `contract.ts` with `mail` collection
	- [x] Create `sendOrderQrEmail` helper function (Firestore-based)
	- [x] Update `onOrderStatusChange` trigger with conditions and email logic
- [x] Optimize logic and fix mobile display
	- [x] Update `onOrderStatusChange` with flexible trigger and duplicate prevention
	- [x] Update `sendOrderQrEmail` with mobile-friendly HTML and fallback link
- [x] Fix invisible images in Gmail (Base64 approach)
	- [x] Update `generateOrderQr` to store base64 string
	- [x] Update `onOrderStatusChange` to pass base64 to email helper
	- [x] Update `sendOrderQrEmail` to embed base64 image
- [x] Verify implementation
	- [x] Build backend to check for syntax/type errors
	- [x] Verify all requirements are met
- [x] Finalize walkthrough
