package com.example.diaryapp.presentation.screens.write

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.diaryapp.model.*
import com.example.diaryapp.presentation.components.GalleryUploader
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WriteContent(
    uiState: UiState,
    paddingValues: PaddingValues,
    pagerState: PagerState,
    galleryState: GalleryState,
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSaveClicked: (Diary) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onImageClicked: (GalleryImage) -> Unit
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = scrollState.maxValue) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() //resize screen when keyboard is opened
            .navigationBarsPadding()
            .padding(top = paddingValues.calculateTopPadding())
            .padding(bottom = 24.dp)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(state = scrollState)
        ) {
            HorizontalPager(
                modifier = Modifier.padding(vertical = 30.dp),
                state = pagerState,
                count = Mood.values().size
            ) { page ->
                AsyncImage(
                    modifier = Modifier.size(120.dp),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Mood.values()[page].icon)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Mood Image"
                )
            }
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = onTitleChanged,
                placeholder = {
                    Text(text = "Title")
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = {
                        scope.launch { scrollState.animateScrollTo(Int.MAX_VALUE) }
                        focusManager.moveFocus(FocusDirection.Down)
                    }
                ),
                maxLines = 1,
                singleLine = true
            )
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = description,
                onValueChange = onDescriptionChanged,
                placeholder = {
                    Text(text = "Tell me about it.")
                },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Unspecified,
                    disabledIndicatorColor = Color.Unspecified,
                    unfocusedIndicatorColor = Color.Unspecified,
                    placeholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.clearFocus() }
                )
            )
        }
        Column(verticalArrangement = Arrangement.Bottom) {
            Spacer(modifier = Modifier.height(12.dp))
            GalleryUploader(
                galleryState = galleryState,
                onAddClicked = {
                    focusManager.clearFocus() //close keyboard
                },
                onImageSelected = onImageSelected,
                onImageClicked = {
                    onImageClicked(it)
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                onClick = {
                    if (uiState.title.isNotEmpty() && uiState.desc.isNotEmpty()) {
                        onSaveClicked(Diary().apply {
                            this.title = uiState.title
                            this.desc = uiState.desc
                            this.images = galleryState.images.map { it.remoteImagePath }.toRealmList()
                        })
                    } else {
                        Toast.makeText(context, "Fields cannot be empty!", Toast.LENGTH_SHORT).show()
                    }
                },
                shape = Shapes().small
            ) {
                Text(text = "Save")
            }
        }
    }
}