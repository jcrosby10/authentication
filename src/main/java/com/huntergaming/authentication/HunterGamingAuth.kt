package com.huntergaming.authentication

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.firebase.auth.PlayGamesAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.huntergaming.gamedata.DataRequestState
import com.huntergaming.gamedata.PlayerRepo
import com.huntergaming.gamedata.model.Player
import com.huntergaming.ui.uitl.CommunicationAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class HunterGamingAuth @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playerRepo: PlayerRepo,
    private val communicationAdapter: CommunicationAdapter
): Authentication {

    private val _loggedInStatus: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.None)
    val loggedInStatus: Flow<LoginState> = _loggedInStatus

    companion object {
        private const val WEB_CLIENT_ID = "752872562416-1pvroqon1rommi0lmc4e0lu2uqegulrh.apps.googleusercontent.com"
        private const val LOG_TAG = "HunterGamingAuth"
    }

    private val _loginManually = MutableStateFlow(false)
    override val loginManually: Flow<Boolean> = _loginManually

    override lateinit var googleSignInClient: GoogleSignInClient

    override suspend fun loginSilently(activity: Activity) {
        _loggedInStatus.emit(LoginState.InProgress)

        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            .requestServerAuthCode(WEB_CLIENT_ID)
            .build()

        googleSignInClient = GoogleSignIn.getClient(activity, signInOptions)

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
        if (GoogleSignIn.hasPermissions(account, *signInOptions.scopeArray)) {
            // already signed in
            val signedInAccount = account!!

            savePlayer(signedInAccount.givenName ?: "Player", signedInAccount.familyName ?: "Player")
            loginFirebase(signedInAccount, activity)
            _loggedInStatus.emit(LoginState.Success)
        }
        else {
            // haven't been signed-in before. Try the silent sign-in first
            googleSignInClient
                .silentSignIn()
                .addOnCompleteListener(
                    activity
                ) { task ->
                    if (task.isSuccessful) {
                        val signedInAccount = task.result
                        loginFirebase(signedInAccount, activity)

                        CoroutineScope(Dispatchers.IO).launch { savePlayer(signedInAccount.givenName ?: "Player", signedInAccount.familyName ?: "Player") }

                        _loggedInStatus.value = LoginState.Success
                    }
                    else {
                        _loginManually.value = true
                        _loggedInStatus.value = LoginState.Failed("Silent login to Google Play Games failed, trying manually.")
                    }
                }
        }
    }

    override suspend fun logout(activity: Activity): Flow<LogoutState> = callbackFlow {
        send(LogoutState.InProgress)

        Firebase.auth.signOut()

        googleSignInClient.signOut().addOnCompleteListener(activity) {
            when (it.isSuccessful) {
                true -> trySend(LogoutState.Success)
                false -> trySend(LogoutState.Error(context.getString(R.string.error_sign_out)))
            }

            channel.close()
        }

        awaitClose()
    }

    override suspend fun onManualLoginResult(signInResult: GoogleSignInResult, activity: Activity) {
        when (signInResult.isSuccess) {
            true -> {
                loginFirebase(signInResult.signInAccount, activity)
                _loggedInStatus.emit(LoginState.Success)
            }
            false -> {
                _loggedInStatus.value = LoginState.Error("Login to Google Play Games failed.")
                Log.w(LOG_TAG, "Login to Google Play Games failed " + signInResult.status.statusMessage)
            }
        }
    }

    private fun loginFirebase(signedInAccount: GoogleSignInAccount?, activity: Activity) {
        val credential = PlayGamesAuthProvider.getCredential(signedInAccount!!.serverAuthCode!!)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                when {
                    task.isSuccessful -> {
                        task.result.user
                        _loggedInStatus.value = LoginState.Success
                    }
                    task.isCanceled -> {
                        _loggedInStatus.value = LoginState.Failed("Login to Firebase was canceled.")
                    }
                    else -> {
                        _loggedInStatus.value = LoginState.Error("Login to Firebase failed.")
                        Log.w(LOG_TAG, "Login to Google Play Games failed " + task.exception!!.message!!, task.exception)
                    }
                }
            }
    }

    private suspend fun savePlayer(firstname: String, lastname: String) {
        playerRepo.create(Player(firstName = firstname, lastName =  lastname)).collect {
            when (it) {
                is DataRequestState.Error -> { communicationAdapter.error.value = context.getString(R.string.error_save_player) }
                else -> {}
            }
        }
    }
}

interface Authentication {
    val loginManually: Flow<Boolean>
    var googleSignInClient: GoogleSignInClient

    suspend fun loginSilently(activity: Activity)
    suspend fun logout(activity: Activity): Flow<LogoutState>
    suspend fun onManualLoginResult(signInResult: GoogleSignInResult, activity: Activity)
}

sealed class LoginState {
    object None : LoginState()
    object NoInternet : LoginState()
    object InProgress : LoginState()
    object Success : LoginState()
    class Failed(val message: String) : LoginState()
    class Error(val message: String) : LoginState()
}

sealed class LogoutState {
    object NoInternet : LogoutState()
    object InProgress : LogoutState()
    object Success : LogoutState()
    class Error(val message: String) : LogoutState()
}