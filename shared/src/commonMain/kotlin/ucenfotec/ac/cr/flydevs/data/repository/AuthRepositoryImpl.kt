package ucenfotec.ac.cr.flydevs.data.repository

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import ucenfotec.ac.cr.flydevs.domain.model.User
import ucenfotec.ac.cr.flydevs.domain.model.UserRole
import ucenfotec.ac.cr.flydevs.domain.repository.IAuthRepository
import ucenfotec.ac.cr.flydevs.domain.repository.INotificationRepository
import ucenfotec.ac.cr.flydevs.getEpochMillis

class AuthRepositoryImpl(
    private val notificationRepository: INotificationRepository
) : IAuthRepository {
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
            role = UserRole.USER.name,
            createdAt = getEpochMillis()
        )

        saveUserProfile(newUser)
    }

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
        val uid = auth.currentUser?.uid ?: throw Exception("Error al obtener el UID")
        val profile = getUserProfile(uid)
        if (profile?.isActive == false) {
            auth.signOut()
            throw Exception("Usuario bloqueado")
        }
    }

    override suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.credential(idToken, null)
        val authResult = auth.signInWithCredential(credential)
        val uid = authResult.user?.uid ?: throw Exception("Error al obtener el UID de Google")

        val profile = getUserProfile(uid)
        if (profile?.isActive == false) {
            auth.signOut()
            throw Exception("Usuario bloqueado")
        }

        return uid
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
        val existingUser = getUserProfile(user.uid)

        val userToSave = if (existingUser != null) {

            user.copy(
                // El rol existente en Firestore no se modifica
                role = UserRole.fromFirestore(existingUser.role).name,

                // Conservamos la fecha original
                createdAt = if (existingUser.createdAt > 0L) {
                    existingUser.createdAt
                } else if (user.createdAt > 0L) {
                    user.createdAt
                } else {
                    getEpochMillis()
                }
            )

        } else {

            user.copy(
                // Los perfiles nuevos comienzan como USER
                role = UserRole.USER.name,

                createdAt = if (user.createdAt > 0L) {
                    user.createdAt
                } else {
                    getEpochMillis()
                }
            )
        }

        firestore
            .collection("users")
            .document(user.uid)
            .set(
                User.serializer(),
                userToSave,
                merge = true
            )
    }

    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override suspend fun signOut() {
        auth.currentUser?.uid?.let { notificationRepository.unregisterDeviceToken(it) }
        auth.signOut()
    }
}
