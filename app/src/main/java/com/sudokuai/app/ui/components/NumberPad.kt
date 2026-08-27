package com.sudokuai.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The 1-9 digit entry row plus an erase button. Notes-mode toggle lives separately in the caller. */
@Composable
fun NumberPad(
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    remainingCounts: IntArray? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (digit in 1..9) {
            val remaining = remainingCounts?.getOrNull(digit - 1)
            val exhausted = remaining != null && remaining <= 0
            OutlinedButton(
                onClick = { onNumberClick(digit) },
                enabled = !exhausted,
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp),
            ) {
                Text(digit.toString(), style = MaterialTheme.typography.titleMedium)
            }
        }
        TextButton(onClick = onEraseClick, modifier = Modifier.weight(1f)) {
            Icon(Icons.Filled.Backspace, contentDescription = null)
        }
    }
}
