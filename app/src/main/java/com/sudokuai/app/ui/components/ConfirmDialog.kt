package com.sudokuai.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.sudokuai.app.R

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = androidx.compose.ui.res.stringResource(R.string.common_yes),
    dismissLabel: String = androidx.compose.ui.res.stringResource(R.string.common_no),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(confirmLabel) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        },
    )
}
