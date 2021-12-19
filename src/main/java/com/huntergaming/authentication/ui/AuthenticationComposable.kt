package com.huntergaming.authentication.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.huntergaming.authentication.R
import com.huntergaming.ui.composable.HunterGamingAlertDialog
import com.huntergaming.ui.composable.HunterGamingBodyText
import com.huntergaming.ui.composable.HunterGamingButton
import com.huntergaming.ui.composable.HunterGamingTitleText
import com.huntergaming.ui.uitl.CommunicationAdapter

private const val LOG_TAG = "AuthenticationComposable"

@Composable
fun Authentication(communicationAdapter: CommunicationAdapter) {
    val failures = communicationAdapter.error.observeAsState()
    val closeDialog = remember { mutableStateOf(false) }

    if (!failures.value.isNullOrEmpty() && !closeDialog.value) {
        Log.e(LOG_TAG, failures.value!!)

        HunterGamingAlertDialog(
            confirmButton = {
                HunterGamingButton(
                    onClick = { closeDialog.value = true },
                    text = R.string.button_ok
                )
            },
            title = { HunterGamingTitleText(text = R.string.error_title) },
            text = { HunterGamingBodyText(text = failures.value!!) },
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
//    Authentication()
}