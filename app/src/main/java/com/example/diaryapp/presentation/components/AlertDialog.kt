package com.example.diaryapp.presentation.components


import android.graphics.Outline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayAlertDialog(
    title: String,
    message: String,
    dialogOpened: Boolean,
    onCloseDialog: () -> Unit,
    onConfirmClicked: () -> Unit
) {
    if (!dialogOpened) return
    AlertDialog(
        title = {
            Text(
                text = title,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold
            )
        },
        confirmButton = {
            Button(onClick = { 
                onConfirmClicked()
                onCloseDialog()
            }) {
                Text(text = "Confirm")
            }
            
        },
        dismissButton = {
            OutlinedButton(onClick = onCloseDialog) {
                Text(text = "Close")
            }
        },
        onDismissRequest = onCloseDialog
    )
}