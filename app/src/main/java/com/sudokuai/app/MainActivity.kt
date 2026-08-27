package com.sudokuai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.sudokuai.app.data.datastore.AppTheme
import com.sudokuai.app.di.ServiceLocator
import com.sudokuai.app.navigation.SudokuAiNavHost
import com.sudokuai.app.ui.theme.SudokuAiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsDataStore = ServiceLocator.provideSettingsDataStore(applicationContext)

        setContent {
            val theme by settingsDataStore.theme.collectAsStateWithLifecycle(initialValue = AppTheme.SYSTEM)

            SudokuAiTheme(appTheme = theme) {
                val navController = rememberNavController()
                SudokuAiNavHost(navController = navController)
            }
        }
    }
}
