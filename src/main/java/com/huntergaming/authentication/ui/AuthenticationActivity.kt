package com.huntergaming.authentication.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.Auth
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AuthenticationActivity : ComponentActivity() {

    companion object {
        private const val PLAY_GAMES_SIGN_IN = "googlePlaySignIn"
    }

    private val authViewModel: AuthenticationViewModel by viewModels()

    private val loginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data!!.hasExtra(PLAY_GAMES_SIGN_IN)) {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    val googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(result.data!!)
                    authViewModel.onManualResult(googleSignInResult!!, this@AuthenticationActivity)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Authentication()
        }

        authViewModel.loginManually.observe(this, {
            val signInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
            val intent = signInClient.signInIntent
            intent.putExtra(PLAY_GAMES_SIGN_IN, PLAY_GAMES_SIGN_IN)
            loginResult.launch(intent)
        })
    }
}

@Composable
fun Authentication() {

}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    Authentication()
}