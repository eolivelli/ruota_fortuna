package com.ruota.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ruota.uicommon.screen.RuotaGameApp

class TvMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // The shared game UI is Material3-based (focusable => D-pad friendly). We wrap it
            // in a Material3 dark theme sized to fill the TV screen.
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFFEB3B),
                    onPrimary = Color(0xFF0E240F),
                    surface = Color(0xFF1B5E20),
                ),
            ) {
                RuotaGameApp(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0E240F)),
                )
            }
        }
    }
}
