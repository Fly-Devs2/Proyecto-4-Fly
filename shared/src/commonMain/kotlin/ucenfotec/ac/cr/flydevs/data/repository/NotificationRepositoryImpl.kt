package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ucenfotec.ac.cr.flydevs.domain.model.AppNotification
import ucenfotec.ac.cr.flydevs.domain.model.NotificationPreferences
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository
import ucenfotec.ac.cr.flydevs.getPushToken

class NotificationRepositoryImpl : INotificationRepository {
    private val firestore = Firebase.firestore
    private val notificationsCollection = firestore.collection("notifications")
    private val usersCollection = firestore.collection("users")
    private val prefsCollection = firestore.collection("notifications_preferences")

    override fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> {
        return notificationsCollection
            .where { "userId" equalTo userId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data(AppNotification.serializer()).copy(id = doc.id)
                    } catch (e: Exception) {
                        println("DEBUG_NOTIFICATIONS: doc ${doc.id} inválido: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.createdAt }
            }
    }

    override suspend fun markAsRead(notificationId: String) {
        notificationsCollection.document(notificationId).update(mapOf("read" to true))
    }

    override suspend fun markAllAsRead(userId: String) {
        val unread = notificationsCollection
            .where { "userId" equalTo userId }
            .where { "read" equalTo false }
            .get()
        unread.documents.forEach { doc ->
            notificationsCollection.document(doc.id).update(mapOf("read" to true))
        }
    }

    override suspend fun registerDeviceToken(userId: String) {
        val token = getPushToken() ?: return
        try {
            usersCollection.document(userId)
                .update(mapOf("fcmTokens" to FieldValue.arrayUnion(token)))
        } catch (e: Exception) {
            println("DEBUG_NOTIFICATIONS: no se pudo guardar token FCM: ${e.message}")
        }
    }

    override suspend fun unregisterDeviceToken(userId: String) {
        val token = getPushToken() ?: return
        try {
            usersCollection.document(userId)
                .update(mapOf("fcmTokens" to FieldValue.arrayRemove(token)))
        } catch (e: Exception) {
            println("DEBUG_NOTIFICATIONS: no se pudo remover token FCM: ${e.message}")
        }
    }

    override suspend fun getPreferences(userId: String): NotificationPreferences {
        return try {
            val doc = prefsCollection.document(userId).get()
            if (doc.exists) {
                doc.data(NotificationPreferences.serializer())
            } else {
                NotificationPreferences()
            }
        } catch (e: Exception) {
            NotificationPreferences()
        }
    }

    override suspend fun updatePreferences(userId: String, prefs: NotificationPreferences) {
        prefsCollection.document(userId).set(NotificationPreferences.serializer(), prefs)
    }
}
