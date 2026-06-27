package ucenfotec.ac.cr.flydevs.auth

class JvmGoogleAuthManager : GoogleAuthManager {
    override suspend fun signIn(): String? {
        // Desktop implementation for Google Sign-In would go here
        return null
    }
}
