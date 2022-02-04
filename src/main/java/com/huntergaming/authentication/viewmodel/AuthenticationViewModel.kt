package com.huntergaming.authentication.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.huntergaming.authentication.Authentication
import com.huntergaming.authentication.CreateAccountState
import com.huntergaming.authentication.LoginState
import com.huntergaming.authentication.UpdateState
import com.huntergaming.web.isConnected
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val hunterGamingAuth: Authentication?,
    @ApplicationContext private val context: Context // This is not a leak we get the Application here not the Activity.
) : ViewModel() {

    val loggedInState = hunterGamingAuth?.loggedInStatus?.asLiveData()
    val createAccountState = hunterGamingAuth?.createAccountState?.asLiveData()

    suspend fun createAccount(name: String, email: String, password: String) {
        if (isConnected(context)) hunterGamingAuth!!.createAccount(name, email, password)
        else hunterGamingAuth!!.createAccountState.value = CreateAccountState.NoInternet
    }

    suspend fun sendVerificationEmail() {
        if (isConnected(context)) hunterGamingAuth!!.sendVerificationEmail()
        else hunterGamingAuth!!.createAccountState.value = CreateAccountState.NoInternet
    }

    suspend fun login(email: String, password: String) {
        if (isConnected(context)) hunterGamingAuth!!.login(email, password)
        else hunterGamingAuth!!.loggedInStatus.emit(LoginState.NoInternet)
    }

    suspend fun logout() {
        if (isConnected(context)) hunterGamingAuth!!.logout()
        else hunterGamingAuth!!.loggedInStatus.emit(LoginState.NoInternet)
    }

    fun isLoggedIn() = hunterGamingAuth?.isLoggedIn()

    suspend fun changePassword(): UpdateState {

        if (isConnected(context)) {
            return hunterGamingAuth!!.resetPassword()
        }
        return UpdateState.NO_INTERNET
    }

    fun isValidField(value: String) = hunterGamingAuth!!.isValidField(value)
    fun isValidEmail(email: String) = hunterGamingAuth!!.isValidEmail(email)
    fun isValidPassword(password: String) = hunterGamingAuth!!.isValidPassword(password)
}