package com.huntergaming.authentication.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.huntergaming.authentication.Authentication
import com.huntergaming.authentication.LoginState
import com.huntergaming.web.isConnected
//import com.huntergaming.web.InternetStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val hunterGamingAuth: Authentication?,
    @ApplicationContext private val context: Context? // This is not a leak we get the Application here not the Activity.
) : ViewModel() {

    val loggedInState = hunterGamingAuth?.loggedInStatus?.asLiveData()

    suspend fun login(email: String, password: String) {
        if (isConnected(context!!)) hunterGamingAuth?.login(email, password)
        else hunterGamingAuth?.loggedInStatus?.emit(LoginState.NoInternet)
    }

    suspend fun logout() {
        if (isConnected(context!!)) hunterGamingAuth?.logout()
        else hunterGamingAuth?.loggedInStatus?.emit(LoginState.NoInternet)
    }

    fun isLoggedIn() = hunterGamingAuth?.isLoggedIn()

    suspend fun changePassword() = hunterGamingAuth?.changePassword()
}