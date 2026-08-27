package com.sudokuai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sudokuai.app.ui.theme.CellError
import com.sudokuai.app.ui.theme.CellPeerHighlight
import com.sudokuai.app.ui.theme.CellSameNumber
import com.sudokuai.app.ui.theme.CellSelected
import com.sudokuai.core.model.Candidates
import com.sudokuai.core.model.Grid

/**
 * Renders a 9x9 Sudoku board. All highlighting is purely presentational and driven by the flags
 * passed in — this composable has no knowledge of settings/DataStore, only booleans.
 *
 * @param originalPuzzle the given (fixed) clues — cells non-zero here are rendered bold/locked.
 * @param currentState the player's current board (givens + entered digits).
 * @param solution used only to decide whether an entered digit is wrong, when [highlightMistakes]
 *   is true. Never displayed directly.
 * @param selectedIndex currently selected cell, or null.
 */
@Composable
fun SudokuBoard(
    originalPuzzle: Grid,
    currentState: Grid,
    candidates: Candidates,
    solution: Grid?,
    selectedIndex: Int?,
    highlightMistakes: Boolean,
    highlightSameNumber: Boolean,
    highlightRowCol: Boolean,
    onCellClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedRow = selectedIndex?.let { Grid.row(it) }
    val selectedCol = selectedIndex?.let { Grid.col(it) }
    val selectedValue = selectedIndex?.let { currentState.get(it) }?.takeIf { it != 0 }
    val boxLineColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, MaterialTheme.colorScheme.outline)
            .drawBehind { drawBoxLines(size.width, size.height, boxLineColor) },
    ) {
        for (row in 0 until 9) {
            Row(modifier = Modifier.weight(1f)) {
                for (col in 0 until 9) {
                    val index = Grid.index(row, col)
                    val value = currentState.get(index)
                    val isGiven = originalPuzzle.get(index) != 0
                    val isSelected = index == selectedIndex
                    val isPeer = !isSelected && highlightRowCol &&
                        selectedRow != null && selectedCol != null &&
                        (row == selectedRow || col == selectedCol)
                    val isSameNumber = !isSelected && highlightSameNumber &&
                        selectedValue != null && value == selectedValue
                    val isMistake = highlightMistakes && value != 0 && solution != null &&
                        solution.get(index) != value

                    val backgroundColor = when {
                        isSelected -> CellSelected
                        isMistake -> CellError
                        isSameNumber -> CellSameNumber
                        isPeer -> CellPeerHighlight
                        else -> Color.Transparent
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(backgroundColor)
                            .clickable { onCellClick(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (value != 0) {
                            Text(
                                text = value.toString(),
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = if (isGiven) {
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                } else {
                                    androidx.compose.ui.text.font.FontWeight.Normal
                                },
                                color = if (isMistake) {
                                    MaterialTheme.colorScheme.error
                                } else if (isGiven) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                            )
                        } else if (!candidates.isEmpty(index)) {
                            CandidateGrid(candidates.get(index))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateGrid(mask: Int) {
    Column {
        for (r in 0 until 3) {
            Row {
                for (c in 0 until 3) {
                    val digit = r * 3 + c + 1
                    val present = (mask and (1 shl (digit - 1))) != 0
                    Box(
                        modifier = Modifier.aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (present) {
                            Text(
                                text = digit.toString(),
                                fontSize = 8.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Draws thicker lines every 3 cells to mark 3x3 box boundaries. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBoxLines(width: Float, height: Float, color: Color) {
    val cell = width / 9f
    val strokeWidth = 2.5.dp.toPx()
    for (i in 1..2) {
        val x = cell * 3 * i
        drawLine(color, Offset(x, 0f), Offset(x, height), strokeWidth)
        val y = cell * 3 * i
        drawLine(color, Offset(0f, y), Offset(width, y), strokeWidth)
    }
}
