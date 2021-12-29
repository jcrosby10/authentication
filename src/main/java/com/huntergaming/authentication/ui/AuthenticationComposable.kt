package com.huntergaming.authentication.ui

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.LifecycleOwner
import com.huntergaming.authentication.LoginState
import com.huntergaming.authentication.R
import com.huntergaming.authentication.viewmodel.AuthenticationViewModel
import com.huntergaming.ui.composable.HunterGamingAlertDialog
import com.huntergaming.ui.composable.HunterGamingButton
import com.huntergaming.ui.composable.HunterGamingColumn
import com.huntergaming.ui.composable.HunterGamingFieldRow
import com.huntergaming.ui.composable.HunterGamingRow
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

    HunterGamingAlertDialog(
        onConfirm = {},
        title = R.string.dialog_error_title,
        text = textState.value,
        state = statusDialogState
    )

    communicationAdapter.error.observe(owner) {
        textState.value = it
        statusDialogState.value = true
    }

    authViewModel.loggedInState?.observe(owner) {
        when (it) {
            LoginState.NoInternet -> {
                textState.value = context.getString(R.string.error_no_internet)
                statusDialogState.value = true
            }

            LoginState.InProgress -> {}
            LoginState.LogoutInProgress -> {}

            LoginState.LoggedIn -> {}
            LoginState.LoggedOut -> {} // this is the initial state dont handle anything that cant be also done initially

            is LoginState.Failed -> Log.e(LOG_TAG, it.message)
            is LoginState.Error -> Log.e(LOG_TAG, it.message)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
    ) {

        Image(
            modifier = Modifier
                .fillMaxSize(),
            painter = painterResource(id = R.drawable.bg),
            contentDescription = stringResource(id = R.string.content_description_not_needed),
            contentScale = ContentScale.FillBounds
        )

        Login()
    }

    val coroutineScope = rememberCoroutineScope()

    // will only recompose if key changes
    LaunchedEffect(true) {
        coroutineScope.launch {
            //authViewModel.login()
        }
    }
}

@Composable
private fun Login() {
    HunterGamingColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        HunterGamingRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {

            val email = remember { mutableStateOf(TextFieldValue(text =  "")) }
            HunterGamingFieldRow(
                fieldNameString = R.string.email_input,
                hintString = R.string.email_input,
                onValueChanged = {},
                textState = email
            )

            val password = remember { mutableStateOf(TextFieldValue(text =  "")) }
            val hidePassword = remember { mutableStateOf(true) }
            HunterGamingFieldRow(
                fieldNameString = R.string.password_input,
                hintString = R.string.password_input,
                onValueChanged = {},
                textState = password,
                isPassword = hidePassword
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

        HunterGamingRow(modifier = Modifier
            .align(Alignment.CenterHorizontally)) {

            HunterGamingButton(
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large)),
                onClick = {  },
                text = R.string.button_login
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

            HunterGamingButton(
                modifier = Modifier
                    .padding(all = dimensionResource(id = R.dimen.padding_large)),
                onClick = {  },
                text = R.string.button_create_account
            )
        }
    }
}

@Composable
private fun CreateAccount() {
    HunterGamingColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {

        HunterGamingRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {

            val firstname = remember { mutableStateOf(TextFieldValue(text =  "")) }
            HunterGamingFieldRow(
                fieldNameString = R.string.firstname_input,
                hintString = R.string.firstname_input,
                onValueChanged = {},
                textState = firstname
            )

            val lastname = remember { mutableStateOf(TextFieldValue(text =  "")) }
            HunterGamingFieldRow(
                fieldNameString = R.string.lastname_input,
                hintString = R.string.lastname_input,
                onValueChanged = {},
                textState = lastname
            )
        }

        HunterGamingRow(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        ) {
            val email = remember { mutableStateOf(TextFieldValue(text =  "")) }
            HunterGamingFieldRow(
                fieldNameString = R.string.email_input,
                hintString = R.string.email_input,
                onValueChanged = {},
                textState = email
            )

            val password = remember { mutableStateOf(TextFieldValue(text =  "")) }
            val hidePassword = remember { mutableStateOf(true) }
            HunterGamingFieldRow(
                fieldNameString = R.string.password_input,
                hintString = R.string.password_input,
                onValueChanged = {},
                textState = password,
                isPassword = hidePassword
            )
        }

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacer_size)))

        HunterGamingButton(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(all = dimensionResource(id = R.dimen.padding_large)),
            onClick = {  },
            text = R.string.button_create_account
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