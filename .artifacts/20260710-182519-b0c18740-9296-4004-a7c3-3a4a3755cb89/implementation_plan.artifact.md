# Scheduled SINPE Reminders & Notification Preferences

Implement a scheduled task to remind users to upload SINPE receipts and a settings screen to manage notification preferences.

## User Review Required

- **SINPE Pending Logic**: The reminder will be sent for orders where `sinpePaid == false` OR `sinpeReceiptUrl` is null/empty. This ensures users are reminded if the payment isn't confirmed or the proof is missing.
- **Preference Storage**: Preferences will be stored in `notifications_preferences/{userId}`.

## Proposed Changes

### Firebase Backend

#### [contract.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/contract.ts)
- Add `notifications_preferences` to `Collections`.

#### [NEW] [scheduledSinpeReminder.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/notifications/scheduledSinpeReminder.ts)
- Implement `onSchedule` for Thursdays at 8:00 AM.
- Query orders where `sinpePaid == false` or `sinpeReceiptUrl` is empty.
- Check user preferences before notifying.

#### [onOrderStatusChange.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/notifications/onOrderStatusChange.ts)
- Update to check `orderStatusChanged` preference before calling `notifyUser`.

#### [index.ts](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/firebase-backend/src/index.ts)
- Export `scheduledSinpeReminder`.

---

### Shared Module (KMP)

#### [NEW] [NotificationPreferences.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/model/NotificationPreferences.kt)
- Data class for preferences: `orderStatusChanged`, `sinpeReminders`.

#### [INotificationRepository.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/domain/repository/INotificationRepository.kt)
- Add `getPreferences(userId: String)` and `updatePreferences(userId: String, prefs: NotificationPreferences)`.

#### [NotificationRepositoryImpl.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/data/repository/NotificationRepositoryImpl.kt)
- Implement preference methods using Firestore.

#### [NEW] [NotificationPreferencesUiState.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/notifications/NotificationPreferencesUiState.kt)
#### [NEW] [NotificationPreferencesViewModel.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/shared/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/notifications/NotificationPreferencesViewModel.kt)

---

### Compose App

#### [Routes.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/navigation/Routes.kt)
- Add `NotificationSettings` route.

#### [ProfileScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/ProfileScreen.kt)
- Add "Configuración de notificaciones" button.

#### [NEW] [NotificationPreferencesScreen.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/presentation/screens/NotificationPreferencesScreen.kt)
- Switch-based UI for managing preferences.
- **Design Alignment**: Use existing components like `TopBar` and `PrimaryButton`.
- **Theming**: Follow `AGENTS.md` tokens (e.g., `BgDarkest` for background, `AccentViolet` for primary actions).

#### [App.kt](file:///C:/Users/Sebbie/Desktop/Cenfotec/Proyecto-4-Fly/composeApp/src/commonMain/kotlin/ucenfotec/ac/cr/flydevs/App.kt)
- Add composable for `NotificationSettings`.

## Verification Plan

### Automated Tests
- `npm run build` in `firebase-backend` to check for syntax errors.

### Manual Verification
- Render `NotificationPreferencesScreen` preview.
- Verify navigation from `ProfileScreen`.
