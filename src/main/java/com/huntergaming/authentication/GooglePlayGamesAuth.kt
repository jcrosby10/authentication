package com.huntergaming.authentication

import android.app.Activity
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

@Singleton
internal class GooglePlayGamesAuth @Inject constructor(
    @ApplicationContext private val context: Context
): Authentication {

    companion object {
        private const val WEB_CLIENT_ID = "752872562416-1pvroqon1rommi0lmc4e0lu2uqegulrh.apps.googleusercontent.com"
        private const val LOG_TAG = "GooglePlayGamesAuth"
    }

    private val _loginManually = MutableStateFlow(false)
    val loginManually: Flow<Boolean> = _loginManually

    override suspend fun loginSilently(activity: Activity): Flow<LoginState> = callbackFlow {
        send(LoginState.InProgress)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(WEB_CLIENT_ID)
            .build()

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
            // already signed in
            val signedInAccount: GoogleSignInAccount = account!!

            send(LoginState.Success)
            channel.close()
        } else {
            // haven't been signed-in before. Try the silent sign-in first
            val signInClient = GoogleSignIn.getClient(activity, signInOptions)
            signInClient
                .silentSignIn()
                .addOnCompleteListener(
                    activity
                ) { task ->
                    if (task.isSuccessful) {
                        val signedInAccount: GoogleSignInAccount = task.result

                        trySend(LoginState.Success)
                        channel.close()
                    }
                    else {
                        _loginManually.value = true
                        trySend(LoginState.Failed)
                        channel.close()
                    }
                }
        }

        awaitClose()
    }

    override suspend fun logout(): Flow<LogoutState> = flow {
        TODO("Not yet implemented")
    }

    override suspend fun onManualLoginResult(signInResult: GoogleSignInResult, activity: Activity) {
        when (signInResult.isSuccess) {
            true -> {
                val signedInAccount = signInResult.signInAccount
                val firebaseAuth = Firebase.auth

                val credential = PlayGamesAuthProvider.getCredential(signedInAccount!!.serverAuthCode!!)
                firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            task.result.user
//                            Log.d(TAG, "signInWithCredential:success")
//                            val user = auth.currentUser
//                            updateUI(user)
                        } else {
                            task.isCanceled
                            task.result.
                            // login failed
                            Log.e(LOG_TAG, "Login to Google Play Games failed " + task.exception!!.message!!, task.exception)
//                            Log.w(TAG, "signInWithCredential:failure", task.exception)
                        }
                    }
            }
            false -> {
                var errorMessage = signInResult.status.statusMessage

            }
        }
    }
}

interface Authentication {
    suspend fun loginSilently(activity: Activity): Flow<LoginState>
    suspend fun logout(): Flow<LogoutState>
    suspend fun onManualLoginResult(signInResult: GoogleSignInResult, activity: Activity)
}

sealed class LoginState {
    object NoInternet: LoginState()
    object InProgress: LoginState()
    object Success: LoginState()
    object Failed: LoginState()
    class Error(val message: String): LoginState()
}

sealed class LogoutState {
    object NoInternet: LoginState()
    object InProgress: LoginState()
    object Success: LoginState()
    class Error(val message: String): LoginState()
}