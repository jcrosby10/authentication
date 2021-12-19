package com.huntergaming.authentication.adapter

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.huntergaming.authentication.Authentication
import com.huntergaming.authentication.LogoutState
import javax.inject.Inject
import kotlinx.coroutines.flow.filter

class AuthenticationAdapter @Inject constructor(
    private val googlePlayGamesAuth: Authentication
) : ViewModel() {

    val loginManually = googlePlayGamesAuth.loginManually
        .filter { it }
        .asLiveData()

    val signInClient = googlePlayGamesAuth.googleSignInClient

    suspend fun onManualResult(signInResult: GoogleSignInResult, activity: Activity) = googlePlayGamesAuth.onManualLoginResult(signInResult, activity)

    suspend fun login(activity: Activity) = googlePlayGamesAuth.loginSilently(activity)
    suspend fun logout(activity: Activity): LiveData<LogoutState> = googlePlayGamesAuth.logout(activity).asLiveData()
}