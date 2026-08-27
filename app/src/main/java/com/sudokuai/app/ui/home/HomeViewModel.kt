package com.sudokuai.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sudokuai.app.data.repository.SaveResult
import com.sudokuai.app.data.repository.SudokuRepository
import com.sudokuai.app.domain.GameMapper
import com.sudokuai.core.generator.SudokuGenerator
import com.sudokuai.core.model.Difficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val repository: SudokuRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkForUnfinishedGame()
    }

    private fun checkForUnfinishedGame() {
        viewModelScope.launch {
            val unfinished = repository.getMostRecentUnfinished()
            if (unfinished != null) {
                _uiState.value = _uiState.value.copy(
                    resumeGameId = unfinished.id,
                    showResumeDialog = true,
                )
            }
        }
    }

    fun onSelectDifficulty(difficulty: Difficulty) {
        _uiState.value = _uiState.value.copy(selectedDifficulty = difficulty)
    }

    fun onResumeConfirmed() {
        val id = _uiState.value.resumeGameId
        _uiState.value = _uiState.value.copy(showResumeDialog = false, navigateToGameId = id)
    }

    fun onResumeDeclined() {
        _uiState.value = _uiState.value.copy(showResumeDialog = false)
    }

    fun onNewSudokuRequested() {
        val difficulty = _uiState.value.selectedDifficulty
        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)
        viewModelScope.launch {
            val generated = withContext(Dispatchers.Default) {
                SudokuGenerator.generatePuzzle(difficulty)
            }
            val gameState = GameMapper.fromGeneratedPuzzle(generated, System.currentTimeMillis())
            when (val result = repository.save(gameState)) {
                is SaveResult.Success -> {
                    _uiState.value = _uiState.value.copy(isGenerating = false, navigateToGameId = result.id)
                }
                SaveResult.LimitReached -> {
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        errorMessage = LIMIT_REACHED_MESSAGE_KEY,
                    )
                }
            }
        }
    }

    fun onEditorRequested() {
        _uiState.value = _uiState.value.copy(navigateToEditor = true)
    }

    fun onNavigationHandled() {
        _uiState.value = _uiState.value.copy(navigateToGameId = null, navigateToEditor = false)
    }

    fun onErrorShown() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    companion object {
        /** Marker consumed by HomeScreen to show the shared "library full" string resource. */
        const val LIMIT_REACHED_MESSAGE_KEY = "LIMIT_REACHED"
    }

    class Factory(private val repository: SudokuRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(repository) as T
    }
}
