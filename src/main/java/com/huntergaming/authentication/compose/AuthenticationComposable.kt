package com.huntergaming.authentication.compose

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Login
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material.icons.twotone.Login
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
import com.huntergaming.ui.composable.HunterGamingFieldRow
import com.huntergaming.ui.composable.HunterGamingSmallCaptionText
import com.huntergaming.ui.uitl.CommunicationAdapter
import com.huntergaming.ui.uitl.Message
import kotlinx.coroutines.launch

// constants

private const val LOG_TAG = "AuthenticationComposable"
const val NAV_TO_MAIN_MENU = "mainMenu"
const val POP_STACK = "popstack"

// composables

@Composable
fun Authentication(
    owner: LifecycleOwner,
    authViewModel: AuthenticationViewModel,
    context: Context,
    communicationAdapter: CommunicationAdapter
) {

    if (authViewModel.isLoggedIn() == true) {
        communicationAdapter.message.value = Message(listOf(NAV_TO_MAIN_MENU, POP_STACK))
    }

    val statusDialogState = remember { mutableStateOf(false) }
    val textState = remember { mutableStateOf("") }
    val titleState = remember { mutableStateOf(-1) }

    val showProgressIndicator = remember { mutableStateOf(false) }

    // wont recompose unless the key changes
    LaunchedEffect(key1 = true) {
        communicationAdapter.error.observe(owner) {
            titleState.value = R.string.dialog_error_title
            textState.value = it
            statusDialogState.value = true
        }
    }

    val createAccount = remember { mutableStateOf(false) }

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

                LoginState.LoggedIn -> {
                    showProgressIndicator.value = false
                    communicationAdapter.message.value = Message(listOf(NAV_TO_MAIN_MENU, POP_STACK))
                }

                LoginState.LoggedOut -> showProgressIndicator.value = false

                is LoginState.Failed -> {
                    showProgressIndicator.value = false
                    Log.e(LOG_TAG, it.message)
                }
                is LoginState.Error -> {
                    showProgressIndicator.value = false
                    titleState.value = R.string.dialog_error_title
                    textState.value = context.getString(R.string.error_login)
                    statusDialogState.value = true

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

                    createAccount.value = false

                    coroutineScope.launch { authViewModel.logout() }
                }
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        val email = remember { mutableStateOf(TextFieldValue(text =  "")) }
        val isEmailError = remember { mutableStateOf(true) }
        val password = remember { mutableStateOf(TextFieldValue(text =  "")) }
        val isPasswordError = remember { mutableStateOf(true) }

        HunterGamingBackgroundImage(image = R.drawable.bg)

        if (createAccount.value) {
            CreateAccount(
                authViewModel = authViewModel,
                email = email,
                password = password,
                isEmailError = isEmailError,
                isPasswordError = isPasswordError
            )
        }
        else Login(
            authViewModel = authViewModel,
            email = email,
            password = password,
            isEmailError = isEmailError,
            isPasswordError = isPasswordError,
            createAccount = createAccount
        )

        if (showProgressIndicator.value) CircularProgressIndicator()

        HunterGamingAlertDialog(
            onConfirm = {},
            title = titleState.value,
            text = textState.value,
            state = statusDialogState,
            backgroundImage = R.drawable.dialog_bg
        )
    }
}

// private composables

@Composable
private fun Login(
    authViewModel: AuthenticationViewModel,
    isPasswordError: MutableState<Boolean>,
    password: MutableState<TextFieldValue>,
    isEmailError: MutableState<Boolean>,
    email: MutableState<TextFieldValue>,
    createAccount: MutableState<Boolean>
) {

    Column(
        modifier = Modifier
            .width(dimensionResource(id = R.dimen.auth_width))
            .height(dimensionResource(id = R.dimen.auth_height))
            .padding(all = dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HunterGamingFieldRow(
            fieldNameString = R.string.email_input,
            hintString = R.string.email_input,
            onValueChanged = { isEmailError.value = authViewModel.isValidEmail(it.text) != true },
            textState = email,
            isError = isEmailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        )

        HunterGamingFieldRow(
            fieldNameString = R.string.password_input,
            hintString = R.string.password_input,
            onValueChanged = { isPasswordError.value = authViewModel.isValidPassword(it.text) != true },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textState = password,
            isPassword = true,
            isError = isPasswordError
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

        Row {
            val coroutineScope = rememberCoroutineScope()
            HunterGamingButton(
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large)),

                onClick = {
                    coroutineScope.launch {
                        authViewModel.login(email = email.value.text, password = password.value.text)
                    }
                },

                icon = if (isSystemInDarkTheme()) Icons.TwoTone.Login else Icons.Outlined.Login,
                isEnabled = !isEmailError.value && !isPasswordError.value,
                contentDescription = R.string.content_description_login
            )

            HunterGamingButton(
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large)),

                onClick = { createAccount.value = true },

                icon = if (isSystemInDarkTheme()) Icons.TwoTone.Add else Icons.Outlined.Add,
                contentDescription = R.string.content_description_create_account
            )
        }
    }
}

@Composable
private fun CreateAccount(
    authViewModel: AuthenticationViewModel,
    isPasswordError: MutableState<Boolean>,
    password: MutableState<TextFieldValue>,
    isEmailError: MutableState<Boolean>,
    email: MutableState<TextFieldValue>
) {

    val name = remember { mutableStateOf(TextFieldValue(text =  "")) }
    val isNameError = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .width(dimensionResource(id = R.dimen.auth_width))
            .height(dimensionResource(id = R.dimen.auth_height))
            .padding(all = dimensionResource(id = R.dimen.padding_medium)),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HunterGamingFieldRow(
            fieldNameString = R.string.name_input,
            hintString = R.string.name_input,
            onValueChanged = { isNameError.value = authViewModel.isValidField(it.text) != true },
            textState = name,
            isError = isNameError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                capitalization = KeyboardCapitalization.Words
            )
        )

        HunterGamingFieldRow(
            fieldNameString = R.string.email_input,
            hintString = R.string.email_input,
            onValueChanged = { isEmailError.value = authViewModel.isValidEmail(it.text) != true },
            textState = email,
            isError = isEmailError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        HunterGamingFieldRow(
            fieldNameString = R.string.password_input,
            hintString = R.string.password_input,
            onValueChanged = { isPasswordError.value = authViewModel.isValidPassword(it.text) != true },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textState = password,
            isPassword = true,
            isError = isPasswordError
        )
        HunterGamingSmallCaptionText(text = R.string.create_account_password_rules)

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

            icon = if (isSystemInDarkTheme()) Icons.TwoTone.Add else Icons.Outlined.Add,
            isEnabled = !isNameError.value && !isEmailError.value && !isPasswordError.value,
            contentDescription = R.string.content_description_create_account
        )
    }
}

// previews

@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
private fun DefaultPreview() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, LocalContext.current),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}

@Preview(showBackground = true, widthDp = 1920, heightDp = 1080)
@Composable
private fun DefaultPreview2() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, LocalContext.current),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 480)
@Composable
private fun DefaultPreview3() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, LocalContext.current),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}

@Preview(showBackground = true, widthDp = 854, heightDp = 480)
@Composable
private fun DefaultPreview4() {
    Authentication(
        owner = ComponentActivity(),
        authViewModel = AuthenticationViewModel(null, LocalContext.current),
        context = ComponentActivity(),
        communicationAdapter = CommunicationAdapter()
    )
}