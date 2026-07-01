package com.ruota.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MobileRoot()
                }
            }
        }
    }
}

@Composable
fun MobileRoot() {
    // Phase 0 placeholder — replaced by the game navigation graph in Phase 3.
    Text(
        text = "La Ruota Della Fortuna",
        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
    )
}

@Preview(showBackground = true)
@Composable
fun MobileRootPreview() {
    MaterialTheme { MobileRoot() }
}
