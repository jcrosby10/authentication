package com.huntergaming.authentication.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModel
import com.huntergaming.authentication.Authentication
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@SuppressLint("StaticFieldLeak")
@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val hunterGamingAuth: Authentication?,
    @ApplicationContext private val context: Context // This is not a leak we get the Application here not the Activity.
) : ViewModel() {

    suspend fun createAccount(name: String, email: String, password: String) = hunterGamingAuth!!.createAccount(name, email, password)

    suspend fun sendVerificationEmail() = hunterGamingAuth!!.sendVerificationEmail()

    suspend fun login(email: String, password: String) = hunterGamingAuth!!.login(email, password)

    suspend fun logout() = hunterGamingAuth!!.logout()

    fun isLoggedIn() = hunterGamingAuth?.isLoggedIn()

    suspend fun resetPassword() = hunterGamingAuth!!.resetPassword()
}