package com.sudokuai.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Formats a duration in seconds as HH:MM:SS (always includes hours, even if 00). */
fun formatElapsed(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

@Composable
fun TimerText(elapsedSeconds: Long, modifier: Modifier = Modifier) {
    Text(
        text = formatElapsed(elapsedSeconds),
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier,
    )
}
