package com.sudokuai.app.ui.home

import com.sudokuai.core.model.Difficulty

data class HomeUiState(
    val isGenerating: Boolean = false,
    val resumeGameId: Long? = null,
    val showResumeDialog: Boolean = false,
    val navigateToGameId: Long? = null,
    val navigateToEditor: Boolean = false,
    val errorMessage: String? = null,
    val selectedDifficulty: Difficulty = Difficulty.MITTEL,
)
