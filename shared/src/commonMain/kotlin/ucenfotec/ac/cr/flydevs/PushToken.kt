package ucenfotec.ac.cr.flydevs

/** Token FCM del dispositivo, o null en plataformas sin soporte push (desktop). */
expect suspend fun getPushToken(): String?
