package ucenfotec.ac.cr.flydevs.domain.repository

import kotlinx.coroutines.flow.Flow
import ucenfotec.ac.cr.flydevs.domain.model.AppNotification

interface INotificationRepository {
    /** Emite en tiempo real las notificaciones del usuario, más recientes primero. */
    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>>

    suspend fun markAsRead(notificationId: String)

    suspend fun markAllAsRead(userId: String)

    /** Registra el token FCM del dispositivo actual. */
    suspend fun registerDeviceToken(userId: String)

    /** Quita el token de este dispositivo del usuario para que deje de recibir sus push. */
    suspend fun unregisterDeviceToken(userId: String)
}
