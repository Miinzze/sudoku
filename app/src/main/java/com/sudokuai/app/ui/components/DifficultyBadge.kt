package com.sudokuai.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sudokuai.core.model.Difficulty

fun difficultyColor(difficulty: Difficulty): Color = when (difficulty) {
    Difficulty.LEICHT -> Color(0xFF2E7D32)
    Difficulty.MITTEL -> Color(0xFF00838F)
    Difficulty.SCHWER -> Color(0xFFEF6C00)
    Difficulty.EXPERTE -> Color(0xFFC62828)
    Difficulty.MONSTER -> Color(0xFF4A148C)
}

@Composable
fun DifficultyBadge(difficulty: Difficulty, modifier: Modifier = Modifier) {
    Text(
        text = difficulty.displayNameDe,
        color = Color.White,
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(difficultyColor(difficulty))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}
