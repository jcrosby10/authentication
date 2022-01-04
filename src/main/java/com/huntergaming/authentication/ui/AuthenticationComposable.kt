package com.huntergaming.authentication.ui

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import com.huntergaming.authentication.CreateAccountState
import com.huntergaming.authentication.LoginState
import com.huntergaming.authentication.R
import com.huntergaming.authentication.viewmodel.AuthenticationViewModel
import com.huntergaming.ui.composable.HunterGamingAlertDialog
import com.huntergaming.ui.composable.HunterGamingBackgroundImage
import com.huntergaming.ui.composable.HunterGamingButton
import com.huntergaming.ui.composable.HunterGamingColumn
import com.huntergaming.ui.composable.HunterGamingFieldRow
import com.huntergaming.ui.composable.HunterGamingRow
import com.huntergaming.ui.composable.HunterGamingSmallCaptionText
import com.huntergaming.ui.uitl.CommunicationAdapter
import kotlinx.coroutines.launch

private const val LOG_TAG = "AuthenticationComposable"

@Composable
fun Authentication(
    owner: LifecycleOwner,
    authViewModel: AuthenticationViewModel,
    context: Context,
    communicationAdapter: CommunicationAdapter
) {

    val statusDialogState = remember { mutableStateOf(false) }
    val textState = remember { mutableStateOf("") }
    val titleState = remember { mutableStateOf(-1) }

    val showProgressIndicator = remember { mutableStateOf(false) }

    HunterGamingAlertDialog(
        onConfirm = {},
        title = titleState.value,
        text = textState.value,
        state = statusDialogState
    )

    // wont recompose unless the key changes
    LaunchedEffect(key1 = true) {
        communicationAdapter.error.observe(owner) {
            titleState.value = R.string.dialog_error_title
            textState.value = it
            statusDialogState.value = true
        }
    }

    LaunchedEffect(key1 = true) {
        authViewModel.loggedInState?.observe(owner) {
            when (it) {
                LoginState.NoInternet -> {
                    titleState.value = R.string.dialog_error_title
                    textState.value = context.getString(R.string.error_no_internet)
                    statusDialogState.value = true
                }

                LoginState.EmailNotVerified -> {
                    titleState.value = R.string.dialog_title_success
                    textState.value = context.getString(R.string.create_account_verification_sent)
                    showProgressIndicator.value = false
                }
                LoginState.InProgress -> showProgressIndicator.value = true
                LoginState.LogoutInProgress -> showProgressIndicator.value = true

                LoginState.LoggedIn -> showProgressIndicator.value = false
                LoginState.LoggedOut -> showProgressIndicator.value = false // this is the initial state dont handle anything that cant be also done initially

                is LoginState.Failed -> {
                    showProgressIndicator.value = false
                    Log.e(LOG_TAG, it.message)
                }
                is LoginState.Error -> {
                    showProgressIndicator.value = false
                    Log.e(LOG_TAG, it.message)
                }
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        authViewModel.createAccountState?.observe(owner) {
            when (it) {
                CreateAccountState.AccountCreated -> coroutineScope.launch { authViewModel.sendVerificationEmail() }
                is CreateAccountState.Error -> {
                    titleState.value = R.string.dialog_error_title
                    textState.value = it.message
                    statusDialogState.value = true
                    showProgressIndicator.value = false
                }

                is CreateAccountState.Failed -> {
                    titleState.value = R.string.dialog_error_title
                    textState.value = it.message
                    statusDialogState.value = true
                    showProgressIndicator.value = false
                }

                CreateAccountState.InProgress -> showProgressIndicator.value = true
                CreateAccountState.Initial -> {}
                CreateAccountState.NoInternet -> {
                    titleState.value = R.string.dialog_error_title
                    textState.value = context.getString(R.string.error_no_internet)
                    statusDialogState.value = true
                    showProgressIndicator.value = false
                }

                is CreateAccountState.VerificationSent -> {
                    titleState.value = R.string.dialog_title_success
                    textState.value = it.message
                    statusDialogState.value = true
                    showProgressIndicator.value = false

                    coroutineScope.launch { authViewModel.logout() }
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        HunterGamingBackgroundImage(image = R.drawable.bg)

        val createAccount = remember { mutableStateOf(false) }

        if (createAccount.value) CreateAccount(authViewModel = authViewModel)
        else Login(
            createAccount = createAccount,
            authViewModel = authViewModel
        )

        if (showProgressIndicator.value) CircularProgressIndicator()
    }
}

@Composable
private fun Login(
    createAccount: MutableState<Boolean>,
    authViewModel: AuthenticationViewModel
) {
    val isError = remember { mutableStateOf(true) }

    HunterGamingColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val email = remember { mutableStateOf(TextFieldValue(text =  "")) }
        val password = remember { mutableStateOf(TextFieldValue(text =  "")) }

        HunterGamingRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {

            HunterGamingFieldRow(
                fieldNameString = R.string.email_input,
                hintString = R.string.email_input,
                onValueChanged = {},
                textState = email,
                isError = isError.value
            )

            val hidePassword = remember { mutableStateOf(true) }
            HunterGamingFieldRow(
                fieldNameString = R.string.password_input,
                hintString = R.string.password_input,
                onValueChanged = {},
                textState = password,
                isPassword = hidePassword,
                isError = isError.value
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

        HunterGamingRow(modifier = Modifier
            .align(Alignment.CenterHorizontally)) {

            val coroutineScope = rememberCoroutineScope()

            HunterGamingButton(
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large)),
                onClick = { coroutineScope.launch { authViewModel.login(email.value.text, password.value.text) } },
                text = R.string.button_login
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

            HunterGamingButton(
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large)),
                onClick = { createAccount.value = true },
                text = R.string.button_create_account
            )
        }
    }
}

@Composable
private fun CreateAccount(authViewModel: AuthenticationViewModel) {

    HunterGamingColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        val name = remember { mutableStateOf(TextFieldValue(text =  "")) }
        val isNameError = remember { mutableStateOf(true) }
        val email = remember { mutableStateOf(TextFieldValue(text =  "")) }
        val isEmailError = remember { mutableStateOf(true) }
        val password = remember { mutableStateOf(TextFieldValue(text =  "")) }
        val isPasswordError = remember { mutableStateOf(true) }

        HunterGamingRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {

            HunterGamingFieldRow(
                fieldNameString = R.string.name_input,
                hintString = R.string.name_input,
                onValueChanged = { isNameError.value = authViewModel.isValidField(it.text) != true },
                textState = name,
                isError = isNameError.value
            )
        }

        HunterGamingRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {

            HunterGamingFieldRow(
                fieldNameString = R.string.email_input,
                hintString = R.string.email_input,
                onValueChanged = { isEmailError.value = authViewModel.isValidEmail(it.text) != true },
                textState = email,
                isError = isEmailError.value
            )

            val hidePassword = remember { mutableStateOf(true) }
            HunterGamingFieldRow(
                fieldNameString = R.string.password_input,
                hintString = R.string.password_input,
                label = { HunterGamingSmallCaptionText(text = R.string.create_account_password_rules) },
                onValueChanged = { isPasswordError.value = authViewModel.isValidPassword(it.text) != true },
                textState = password,
                isPassword = hidePassword,
                isError = isPasswordError.value
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

        val coroutineScope = rememberCoroutineScope()
        HunterGamingButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = dimensionResource(id = R.dimen.padding_large)),
            onClick = {
                coroutineScope.launch {
                    authViewModel.createAccount(
                        name = name.value.text,
                        email = email.value.text,
                        password = password.value.text
                    )
                }
            },
            text = R.string.button_create_account,
            isEnabled = !isNameError.value && !isEmailError.value && !isPasswordError.value
        )
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
private fun DefaultPreview() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, null),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
private fun DefaultPreview2() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, null),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun DefaultPreview3() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, null),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}

@Preview(showBackground = true, widthDp = 854, heightDp = 480)
@Composable
private fun DefaultPreview4() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, null),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}