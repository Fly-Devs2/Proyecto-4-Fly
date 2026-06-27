package ucenfotec.ac.cr.flydevs.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidGoogleAuthManager(
    private val context: Context,
    private val serverClientId: String,
) : GoogleAuthManager {

    private val credentialManager = CredentialManager.create(context)

    override suspend fun signIn(): String? = withContext(Dispatchers.Main) {
        val activity = context.findActivity() 
            ?: throw Exception("No se pudo encontrar el contexto de Activity")

        try {
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts = false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false) // Disable auto-select to force the picker
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = activity, // Critical: Use Activity context
                request = request
            )

            val credential = result.credential
            println("Debug: Received credential type: ${credential::class.simpleName}")
            
            if (credential is GoogleIdTokenCredential) {
                println("Debug: Success! Received ID Token")
                return@withContext credential.idToken
            } else if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    println("Debug: Success! Received ID Token via CustomCredential parsing")
                    return@withContext googleIdTokenCredential.idToken
                } catch (e: Exception) {
                    println("Debug: Failed to parse CustomCredential as GoogleIdTokenCredential: ${e.message}")
                }
            }
            
            println("Debug: Credential is NOT GoogleIdTokenCredential. Full type: ${credential.type}")
            return@withContext null
        } catch (e: Exception) {
            throw e // Rethrow to let ViewModel handle the specific error
        }
    }

    private fun Context.findActivity(): ComponentActivity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is ComponentActivity) return context
            context = context.baseContext
        }
        return null
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
