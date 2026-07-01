package ucenfotec.ac.cr.flydevs.auth

interface GoogleAuthManager {
    suspend fun signIn(): String?
}
