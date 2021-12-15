package com.huntergaming.authentication.ui

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.huntergaming.authentication.GooglePlayGamesAuth
import com.huntergaming.authentication.LoginState
import com.huntergaming.authentication.LogoutState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.filter

@HiltViewModel
internal class AuthenticationViewModel @Inject constructor(
    private val googlePlayGamesAuth: GooglePlayGamesAuth
) : ViewModel() {

    val loginManually = googlePlayGamesAuth.loginManually
        .filter { it }
        .asLiveData()

    suspend fun onManualResult(signInResult: GoogleSignInResult, activity: Activity) = googlePlayGamesAuth.onManualLoginResult(signInResult, activity)

    suspend fun login(activity: Activity): LiveData<LoginState> = googlePlayGamesAuth.loginSilently(activity).asLiveData()
    suspend fun logout(): LiveData<LogoutState> = googlePlayGamesAuth.logout().asLiveData()
}