package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.repository.AuthRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class AuthRepositoryImpl : AuthRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override suspend fun register(name: String, email: String, phone: String, password: String) {
        val authResult = auth.createUserWithEmailAndPassword(email, password)
        val uid = authResult.user?.uid ?: throw Exception("Error al obtener el UID del usuario")

        val newUser = User(
            uid = uid,
            name = name,
            email = email,
            phone = phone,
            createdAt = getEpochMillis()
        )

        saveUserProfile(newUser)
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
    }

    override suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.credential(idToken, null)
        val authResult = auth.signInWithCredential(credential)
        return authResult.user?.uid ?: throw Exception("Error al obtener el UID de Google")
    }

    override suspend fun getUserProfile(uid: String): User? {
        val snapshot = firestore.collection("users").document(uid).get()
        return if (snapshot.exists) {
            snapshot.data(User.serializer())
        } else {
            null
        }
    }

    override suspend fun saveUserProfile(user: User) {
        val existing = getUserProfile(user.uid)
        val userToSave = if (existing != null && user.createdAt == 0L) {
            user.copy(createdAt = existing.createdAt)
        } else if (user.createdAt == 0L) {
            user.copy(createdAt = getEpochMillis())
        } else {
            user
        }
        
        firestore.collection("users").document(user.uid).set(User.serializer(), userToSave)
    }

    override suspend fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    override suspend fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
