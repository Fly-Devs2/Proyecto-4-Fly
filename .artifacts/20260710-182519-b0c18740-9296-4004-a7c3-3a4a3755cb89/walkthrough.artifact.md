# Scheduled SINPE Reminders & Notification Preferences

Implemented a scheduled task to remind users about pending SINPE payments and a settings screen to manage notification preferences.

## Backend Changes

### Scheduled Reminder
- **File**: [scheduledSinpeReminder.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/notifications/scheduledSinpeReminder.ts)
- **Schedule**: Thursdays at 8:00 AM (CR Time).
- **Logic**: Notifies users with orders where `sinpePaid == false` or `sinpeReceiptUrl` is empty.
- **Preference Check**: Respects the `sinpeReminders` preference stored in `notifications_preferences/{userId}`.

### Order Status Update
- **File**: [onOrderStatusChange.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/notifications/onOrderStatusChange.ts)
- **Logic**: Updated to check the `orderStatusChanged` preference before sending notifications.

## App Changes

### Notification Preferences Screen
- **File**: [NotificationPreferencesScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/NotificationPreferencesScreen.kt)
- **Features**:
    - Switch-based UI to toggle `orderStatusChanged` and `sinpeReminders`.
    - Persists settings to Firestore using `NotificationRepository`.
    - Integrated into `ProfileScreen`.

### Domain & Data Layer
- **Model**: `NotificationPreferences` data class.
- **Repository**: Added `getPreferences` and `updatePreferences` to `INotificationRepository`.
- **Implementation**: `NotificationRepositoryImpl` handles Firestore operations on the `notifications_preferences` collection.

## Verification Summary

### Backend Build
- Successfully ran `npm run build` in `firebase-backend` without compilation errors.

### UI Integration
- Verified the `NotificationPreferencesScreen` follows the design tokens from `AGENTS.md`.
- Confirmed navigation flow: `ProfileScreen` -> `NotificationPreferencesScreen`.
- Added a preview function `NotificationPreferencesScreenPreview` for visual verification.
