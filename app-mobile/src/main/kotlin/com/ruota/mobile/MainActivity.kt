package com.ruota.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ruota.uicommon.screen.RuotaGameApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFFEB3B),
                    onPrimary = Color(0xFF0E240F),
                    surface = Color(0xFF1B5E20),
                ),
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RuotaGameApp()
                }
            }
        }
    }
}
