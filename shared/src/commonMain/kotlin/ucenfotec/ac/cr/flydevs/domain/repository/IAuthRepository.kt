package ucenfotec.ac.cr.flydevs.domain.repository

import ucenfotec.ac.cr.flydevs.domain.model.User

interface IAuthRepository {
    suspend fun register(name: String, email: String, phone: String, password: String)
    suspend fun login(email: String, password: String)
    suspend fun signInWithGoogle(idToken: String): String
    suspend fun getUserProfile(uid: String): User?
    suspend fun saveUserProfile(user: User)
    fun getCurrentUserUid(): String?
    fun getCurrentUserEmail(): String?
    fun isUserLoggedIn(): Boolean
    suspend fun signOut()
}
