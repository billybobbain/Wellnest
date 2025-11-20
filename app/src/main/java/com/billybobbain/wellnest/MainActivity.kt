package com.billybobbain.wellnest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.billybobbain.wellnest.ui.theme.WellnestTheme

class MainActivity : ComponentActivity() {
    private val viewModel: WellnestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by viewModel.settings.collectAsState()
            val selectedTheme = settings?.selectedTheme ?: "Teal"

            WellnestTheme(selectedTheme = selectedTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WellnestApp(viewModel = viewModel)
                }
            }
        }
    }
}
