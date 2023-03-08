package com.example.diaryapp.presentation.screens.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.diaryapp.util.Constants.APP_ID
import io.realm.kotlin.mongodb.App
import io.realm.kotlin.mongodb.Credentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class AuthenticationViewModel: ViewModel() {

    private val _loadingState = mutableStateOf(false)
    val loadingState: State<Boolean> = _loadingState

    private val _authenticated = mutableStateOf(false)
    val authenticated: State<Boolean> = _authenticated

    fun setLoadingState(loadingState: Boolean) {
        _loadingState.value = loadingState
    }

    fun signInWithMongoAtlas(
        tokenId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    App.Companion.create(APP_ID).login(
                        Credentials.jwt(tokenId)
                        //Credentials.google(tokenId, GoogleAuthType.ID_TOKEN)
                    ).loggedIn
                }
                withContext(Dispatchers.Main) {
                    if (result) {
                        onSuccess()
                        delay(500L)
                        _authenticated.value = true
                    } else {
                        onError(Exception("User is not logged in."))
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw CancellationException()
                }
                withContext(Dispatchers.Main) {
                    onError(e)
                }
            }
        }
    }

}