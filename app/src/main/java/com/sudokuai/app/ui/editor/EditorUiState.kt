package com.sudokuai.app.ui.editor

import com.sudokuai.core.model.Grid

enum class ValidationResult { NONE, VALID, NO_SOLUTION, MULTIPLE_SOLUTIONS }

data class EditorUiState(
    val puzzle: Grid = Grid(),
    val selectedIndex: Int? = null,
    val validationResult: ValidationResult = ValidationResult.NONE,
    val navigateToGameId: Long? = null,
    val errorMessage: String? = null,
)
