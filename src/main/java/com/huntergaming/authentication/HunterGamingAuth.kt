package com.huntergaming.authentication

import android.content.Context
import android.util.Log
import android.util.Patterns
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import com.huntergaming.gamedata.DataRequestState
import com.huntergaming.gamedata.PlayerRepo
import com.huntergaming.gamedata.model.Player
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

internal class HunterGamingAuth @Inject constructor(
    @ApplicationContext private var context: Context,
    private val playerRepo: PlayerRepo
): Authentication {

    // COMPANION OBJECTS

    companion object {
        private const val LOG_TAG = "HunterGamingAuth"
    }

    // OVERRIDDEN PROPERTIES

    override val loggedInStatus: MutableStateFlow<LoginState> = MutableStateFlow(LoginState.LoggedOut)
    override val createAccountState: MutableStateFlow<CreateAccountState> = MutableStateFlow(CreateAccountState.Initial)

    // OVERRIDDEN FUNCTIONS

    override suspend fun createAccount(name: String, email: String, password: String) {
        createAccountState.emit(CreateAccountState.InProgress)

        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {

                when {
                    it.isSuccessful -> {
                        CoroutineScope(Dispatchers.IO).launch {
                            savePlayer(name, email).collect { state ->
                                when (state) {
                                    is DataRequestState.Success<*> -> {
                                        createAccountState.value = CreateAccountState.AccountCreated
                                    }
                                    is DataRequestState.Error -> {
                                        createAccountState.value = CreateAccountState.Error(context.getString(R.string.error_save_player))
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }

                    it.isCanceled -> createAccountState.value = CreateAccountState.Failed(context.getString(R.string.create_account_canceled))
                    else -> createAccountState.value = CreateAccountState.Failed(context.getString(R.string.create_account_failed))
                }
            }
    }

    override suspend fun login(email: String, password: String) {
        loggedInStatus.emit(LoginState.InProgress)

        if (Firebase.auth.currentUser?.isEmailVerified == true) {
            Firebase.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    when {
                        task.isSuccessful -> loggedInStatus.value = LoginState.LoggedIn
                        task.isCanceled -> loggedInStatus.value = LoginState.Failed(context.getString(R.string.login_canceled))
                        else -> {
                            loggedInStatus.value = LoginState.Error(context.getString(R.string.error_login))
                            Log.w(LOG_TAG, "Login to Firebase failed " + task.exception!!.message!!, task.exception)
                        }
                    }
                }
        }
        else {
            loggedInStatus.value = LoginState.Error(context.getString(R.string.login_not_verified))
        }
    }

    override suspend fun sendVerificationEmail() {
        Firebase.auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener {
                when {
                    it.isSuccessful -> createAccountState.value = CreateAccountState.VerificationSent(context.getString(R.string.create_account_verification_sent))
                    it.isCanceled -> createAccountState.value = CreateAccountState.Failed(context.getString(R.string.create_account_verification_canceled))
                    else -> {
                        createAccountState.value = CreateAccountState.Error(context.getString(R.string.create_account_verification_failed))
                        Log.e(LOG_TAG, "Sending verification failed " + it.exception!!.message!!, it.exception)
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

    override fun isValidField(value: String): Boolean = value.isNotEmpty()

    override fun isValidEmail(email: String): Boolean = email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    override fun isValidPassword(password: String): Boolean =
        password.isNotEmpty() && password.length >= 10 && password.contains(Regex("(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*\\W)"))

    // PRIVATE FUNCTIONS

    private suspend fun savePlayer(name: String, email: String): Flow<DataRequestState> = flow {
        playerRepo.create(id = Firebase.auth.currentUser?.uid!!, name = name, email =  email).collect {
            emit(it)
        }

        val profileUpdate = userProfileChangeRequest {
            displayName = name
        }

        Firebase.auth.currentUser?.updateProfile(profileUpdate)
    }
}

// INTERFACES/CLASSES

interface Authentication {
    val loggedInStatus: MutableStateFlow<LoginState>
    val createAccountState: MutableStateFlow<CreateAccountState>

    suspend fun createAccount(name: String, email: String, password: String)
    suspend fun login(email: String, password: String)
    suspend fun sendVerificationEmail()
    fun isLoggedIn(): Boolean
    suspend fun logout()

    suspend fun changePassword()

    fun isValidField(value: String): Boolean
    fun isValidEmail(email: String): Boolean
    fun isValidPassword(password: String): Boolean
}

sealed class CreateAccountState {
    object Initial : CreateAccountState()
    object NoInternet : CreateAccountState()
    object InProgress : CreateAccountState()
    object AccountCreated : CreateAccountState()
    class VerificationSent(val message: String) : CreateAccountState()
    class Failed(val message: String) : CreateAccountState()
    class Error(val message: String) : CreateAccountState()
}

sealed class LoginState {
    object NoInternet : LoginState()
    object EmailNotVerified : LoginState()
    object InProgress : LoginState()
    object LogoutInProgress : LoginState()
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
    class Failed(val message: String) : LoginState()
    class Error(val message: String) : LoginState()
}