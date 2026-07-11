package ucenfotec.ac.cr.flydevs

// Desktop no tiene FCM: solo notificaciones in-app vía Firestore.
actual suspend fun getPushToken(): String? = null
