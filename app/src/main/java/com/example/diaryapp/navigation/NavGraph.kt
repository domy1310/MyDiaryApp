package com.example.diaryapp.navigation

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.diaryapp.model.GalleryImage
import com.example.diaryapp.model.Mood
import com.example.diaryapp.presentation.components.DisplayAlertDialog
import com.example.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.example.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.example.diaryapp.presentation.screens.home.HomeScreen
import com.example.diaryapp.presentation.screens.home.HomeViewModel
import com.example.diaryapp.presentation.screens.write.WriteScreen
import com.example.diaryapp.presentation.screens.write.WriteViewModel
import com.example.diaryapp.util.Constants.APP_ID
import com.example.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.example.diaryapp.model.RequestState
import com.example.diaryapp.model.rememberGalleryState
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

@RequiresApi(Build.VERSION_CODES.N)
@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {
    NavHost(
        startDestination = startDestination,
        navController = navController,
    ) {
        authenticationRoute(navigateToHome = {
            navController.popBackStack()
            navController.navigate(Screen.Home.route)
        }, onDataLoaded = onDataLoaded)
        homeRoute(navigateToWrite = {
            navController.navigate(Screen.Write.route)
        }, navigateToAuth = {
            navController.navigate(Screen.Authentication.route)
        }, onDataLoaded = onDataLoaded,
        navigateToWriteWithArgs = { diaryId ->
            navController.navigate(Screen.Write.passDiaryId(diaryId))
        })
        writeRoute(onBackPressed = {
            navController.popBackStack()
        })
    }
}

fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(
        route = Screen.Authentication.route
    ) {
        val viewModel: AuthenticationViewModel = viewModel()
        val loadingState by viewModel.loadingState
        val authenticated by viewModel.authenticated
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            loadingState = loadingState,
            authenticated = authenticated,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onButtonClicked = {
                oneTapState.open()
                viewModel.setLoadingState(true)
            },
            onSuccessfullyFirebaseSignIn = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully Authenticated!")
                        viewModel.setLoadingState(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoadingState(false)
                    })

            },
            onFailedFirebaseSignIn = {
                messageBarState.addError(it)
                viewModel.setLoadingState(false)
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoadingState(false)
            },
            navigateToHome = navigateToHome
        )
    }
}

@RequiresApi(Build.VERSION_CODES.N)
fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(
        route = Screen.Home.route
    ) {
        val viewModel: HomeViewModel = hiltViewModel()
        val diaries by viewModel.diaries
        val context = LocalContext.current
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var singOutDialogOpened by remember {
            mutableStateOf(false)
        }
        var deleteAllDialogOpened by remember {
            mutableStateOf(false)
        }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }

        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch(Dispatchers.Main) {
                    drawerState.open()
                }
            },
            onDateSelected = {viewModel.getDiaries(it) },
            onDateReset = { viewModel.getDiaries() },
            dateIsSelected = viewModel.dateIsSelected,
            onSingOutClicked = {
                singOutDialogOpened = true
            },
            onDeleteAllClicked = {
                deleteAllDialogOpened = true
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs
        )


        DisplayAlertDialog(
            title = "Sing Out",
            message = "Are you sure you want to Sign Out from your Google Account?",
            dialogOpened = singOutDialogOpened,
            onConfirmClicked = {
                scope.launch(Dispatchers.IO) {
                    App.create(APP_ID).currentUser?.let { user ->
                        user.logOut()
                        withContext(Dispatchers.Main) { navigateToAuth() }
                    }
                }
            },
            onCloseDialog = { singOutDialogOpened = false }
        )

        DisplayAlertDialog(
            title = "Delete All Diaries",
            message = "Are you sure you want to permanently delete all diaries?",
            dialogOpened = deleteAllDialogOpened,
            onConfirmClicked = {
                viewModel.deleteAllDiaries(
                    onSuccess = {
                        Toast.makeText(context, "Successfully deleted all diaries", Toast.LENGTH_SHORT).show()
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            if (it.message == "No Internet Connection") "We need an Internet Connection for this operation."
                            else it.message,
                            Toast.LENGTH_SHORT
                        ).show()
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            },
            onCloseDialog = { deleteAllDialogOpened = false }
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(
    onBackPressed: () -> Unit
) {
    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = hiltViewModel()
        val context = LocalContext.current
        val uiState = viewModel.uiState
        val pagerState = rememberPagerState()
        val galleryState = viewModel.galleryState
        val pageNumber by remember {
            derivedStateOf { pagerState.currentPage }
        }

        WriteScreen(
            uiState = uiState,
            moodName = { Mood.values()[pageNumber].name },
            pagerState = pagerState,
            galleryState = galleryState,
            onTitleChanged = { viewModel.setTitle(it) },
            onDescChanged = { viewModel.setDesc(it) },
            onDateTimeUpdated = { viewModel.updateDateTime(it) },
            onBackPressed = onBackPressed,
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = { onBackPressed() },
                    onError = { message ->
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onImageSelected = { imageUri ->
                val type = context.contentResolver.getType(imageUri)?.split("/")?.last() ?: "jpg"
                viewModel.addImage(
                    image = imageUri,
                    imageType = type
                )
            },
            onImageDeleteClicked = { galleryImage ->  
                galleryState.removeImage(galleryImage)
            }
        )
    }
}