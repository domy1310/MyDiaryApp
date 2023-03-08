package com.example.diaryapp.presentation.screens.auth

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.diaryapp.util.Constants.CLIENT_ID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.stevdzasan.messagebar.ContentWithMessageBar
import com.stevdzasan.messagebar.MessageBarState
import com.stevdzasan.onetap.OneTapSignInState
import com.stevdzasan.onetap.OneTapSignInWithGoogle

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticationScreen(
    loadingState: Boolean,
    authenticated: Boolean,
    oneTapState: OneTapSignInState,
    messageBarState: MessageBarState,
    onButtonClicked: () -> Unit,
    onSuccessfullyFirebaseSignIn: (String) -> Unit,
    onFailedFirebaseSignIn: (Exception) -> Unit,
    onDialogDismissed: (String) -> Unit,
    navigateToHome: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
        content = {
            ContentWithMessageBar(messageBarState = messageBarState) {
                AuthenticationContent(
                    loadingState = loadingState,
                    onButtonClicked = onButtonClicked
                )
            }
        }
    )

    OneTapSignInWithGoogle(
        state = oneTapState,
        clientId = CLIENT_ID,
        onTokenIdReceived = { tokenId ->
            Log.d("Auth", tokenId)
            val credential = GoogleAuthProvider.getCredential(tokenId, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authTask ->
                    if (authTask.isSuccessful) {
                        onSuccessfullyFirebaseSignIn(tokenId)
                    } else {
                        authTask.exception?.let { onFailedFirebaseSignIn(it) }
                    }
                }
        },
        onDialogDismissed = { message ->
            Log.d("Auth", message)
            onDialogDismissed(message)
        }
    )
    
    LaunchedEffect(key1 = authenticated) {
        if (authenticated) {
            navigateToHome()
        }
    }
}