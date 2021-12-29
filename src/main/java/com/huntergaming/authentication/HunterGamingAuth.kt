package com.huntergaming.authentication

import android.content.Context
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.huntergaming.gamedata.DataRequestState
import com.huntergaming.gamedata.PlayerRepo
import com.huntergaming.gamedata.model.Player
import com.huntergaming.ui.uitl.CommunicationAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow

internal class HunterGamingAuth @Inject constructor(
    @ApplicationContext private var context: Context,
    private val playerRepo: PlayerRepo,
    private val communicationAdapter: CommunicationAdapter
): Authentication {

    override val loggedInStatus: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)

    companion object {
        private const val LOG_TAG = "HunterGamingAuth"
    }

    override suspend fun createAccount(firstname: String, lastname: String, email: String, password: String) {
        TODO("Not yet implemented")
    }

    override suspend fun login(email: String, password: String) {
        loggedInStatus.emit(LoginState.InProgress)

        Firebase.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                when {
                    task.isSuccessful -> {
                        task.result?.user
                        loggedInStatus.value = LoginState.LoggedIn

                        suspend {
                            savePlayer("firesnamr", "lastname").collect {
                                when (it) {
                                    is DataRequestState.Error -> communicationAdapter.error.value = context.getString(R.string.error_save_player)
                                    else -> Log.i(LOG_TAG, "User successfully logged in.")
                                }
                            }
                        }
                    }
                    task.isCanceled -> {
                        loggedInStatus.value = LoginState.Failed("Login to Firebase was canceled.")
                    }
                    else -> {
                        loggedInStatus.value = LoginState.Error("Login to Firebase failed.")
                        Log.w(LOG_TAG, "Login to Google Play Games failed " + task.exception!!.message!!, task.exception)
                    }
                }
            }
    }

    override suspend fun logout() {
        loggedInStatus.emit(LoginState.LogoutInProgress)

        Firebase.auth.signOut()
        loggedInStatus.emit(LoginState.LoggedOut)
    }

    override fun isLoggedIn(): Boolean = Firebase.auth.currentUser != null


    override suspend fun changePassword() {
        TODO("Not yet implemented")
    }

    private suspend fun savePlayer(firstname: String, lastname: String): Flow<DataRequestState> = flow {
        playerRepo.create(Player(firstName = firstname, lastName =  lastname)).collect {
            emit(it)
        }
    }
}

interface Authentication {
    val loggedInStatus: MutableStateFlow<LoginState>

    suspend fun createAccount(firstname: String, lastname: String, email: String, password: String)
    suspend fun login(email: String, password: String)
    fun isLoggedIn(): Boolean
    suspend fun logout()
    suspend fun changePassword()
}

sealed class LoginState {
    object NoInternet : LoginState()
    object InProgress : LoginState()
    object LogoutInProgress : LoginState()
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
    class Failed(val message: String) : LoginState()
    class Error(val message: String) : LoginState()
}