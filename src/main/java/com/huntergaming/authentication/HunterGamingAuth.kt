package com.huntergaming.authentication

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.huntergaming.ui.uitl.CreateAccountState
import com.huntergaming.ui.uitl.DataRequestState
import com.huntergaming.ui.uitl.LoginState
import com.huntergaming.ui.uitl.authIntermediary
import com.huntergaming.ui.uitl.createAccountState
import com.huntergaming.ui.uitl.loggedInStatus
import com.huntergaming.ui.uitl.AuthHandler
import com.huntergaming.ui.uitl.CommunicationAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

val saveNewPlayer = MutableStateFlow(listOf<String>())

internal class HunterGamingAuth @Inject constructor(
    @ApplicationContext private var context: Context,
    private val communicationAdapter: CommunicationAdapter
): Authentication {

    // companion objects

    companion object {
        private const val LOG_TAG = "HunterGamingAuth"
    }

    init {
        CoroutineScope(Dispatchers.Default).launch {
            authIntermediary.collect {
                when (it) {
                    is AuthHandler.CreateAccount -> createAccount(it.name, it.email, it.password)
                    is AuthHandler.Login -> login(it.email, it.password)
                    AuthHandler.Logout -> logout()
                    AuthHandler.SendVerificationEmail -> sendVerificationEmail()
                    null -> {}
                }
            }
        }
    }

    // overridden properties

    override val user: FirebaseUser? = Firebase.auth.currentUser

    // overridden functions

    override suspend fun createAccount(name: String, email: String, password: String) {
        createAccountState.value = CreateAccountState.InProgress

        Firebase.auth.createUserWithEmailAndPassword(email, password)

            .addOnCompleteListener {
                CoroutineScope(Dispatchers.IO).launch {

                    if (it.isSuccessful) {
                        savePlayer(name, email).collect { state ->
                            when (state) {
                                is DataRequestState.Success<*> -> {
                                    createAccountState.value = CreateAccountState.AccountCreated
                                }

                                else -> {
                                    communicationAdapter.message.value?.add(context.getString(R.string.error_save_player))
                                }
                            }
                        }
                    }
                    else {
                        communicationAdapter.message.value?.add(context.getString(R.string.create_account_failed))
                    }
                }
            }
    }

    override suspend fun login(email: String, password: String) {
        loggedInStatus.value = LoginState.InProgress

        Firebase.auth.signInWithEmailAndPassword(email, password)

            .addOnSuccessListener {

                if (user?.isEmailVerified == true) {
                    loggedInStatus.value = LoginState.LoggedIn
                }
                else {
                    communicationAdapter.message.value?.add(context.getString(R.string.login_not_verified))
                    Firebase.auth.signOut()
                }
            }

            .addOnCanceledListener {
                communicationAdapter.message.value?.add(context.getString(R.string.login_canceled))
            }

            .addOnFailureListener {
                communicationAdapter.message.value?.add(context.getString(R.string.error_login))
                Log.w(LOG_TAG, "Login to Firebase failed " + it.message, it)
            }
    }

    override suspend fun sendVerificationEmail() {
        user?.sendEmailVerification()

            ?.addOnSuccessListener {
                createAccountState.value = CreateAccountState.VerificationSent(context.getString(R.string.create_account_verification_sent))
            }

            ?.addOnCanceledListener {
                communicationAdapter.message.value?.add(context.getString(R.string.create_account_verification_canceled))
            }

            ?.addOnFailureListener {
                communicationAdapter.message.value?.add(context.getString(R.string.create_account_verification_failed))
                Log.e(LOG_TAG, "Sending verification failed " + it.message, it)
            }
    }

    override suspend fun logout() {
        loggedInStatus.value = LoginState.LogoutInProgress
        Firebase.auth.signOut()
        loggedInStatus.value = LoginState.LoggedOut
    }

    override fun isLoggedIn(): Boolean = user != null

    override suspend fun resetPassword(): UpdateState =

        suspendCancellableCoroutine { cont ->
            Firebase.auth.sendPasswordResetEmail(user?.email!!)

                .addOnSuccessListener { cont.resume(UpdateState.SUCCESS) }
                .addOnCanceledListener { cont.resume(UpdateState.FAILED) }

                .addOnFailureListener {
                    Log.e(LOG_TAG, "Sending verification failed " + it.message, it)
                    cont.resume(UpdateState.FAILED)
                }
        }

    // private functions

    private suspend fun savePlayer(name: String, email: String): Flow<DataRequestState> = flow {
        saveNewPlayer.emit(listOf(name, email))
    }
}

// interfaces/classes

interface Authentication {

    val user: FirebaseUser?

    suspend fun createAccount(name: String, email: String, password: String)
    suspend fun login(email: String, password: String)
    suspend fun sendVerificationEmail()
    fun isLoggedIn(): Boolean
    suspend fun logout()

    suspend fun resetPassword(): UpdateState
}

enum class UpdateState {
    SUCCESS, FAILED
}