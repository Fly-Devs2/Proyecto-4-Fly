package ucenfotec.ac.cr.flydevs

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.messaging.messaging

actual suspend fun getPushToken(): String? {
    return try {
        Firebase.messaging.getToken()
    } catch (e: Exception) {
        println("DEBUG_NOTIFICATIONS: no se pudo obtener token FCM: ${e.message}")
        null
    }
}
