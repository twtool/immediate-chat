package icu.twtool.chat

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import icu.twtool.chat.navigation.window.calculateWindowSizeClass
import icu.twtool.chat.theme.ICTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("MainActivity", "onCreate")
        super.onCreate(savedInstanceState)

        setContent {
            val windowSize = calculateWindowSizeClass(this)
            ICTheme {
                enableEdgeToEdge()
                App(windowSize)
            }
        }
    }
}