package com.example.bagit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.bagit.auth.ui.LoginScreen
import com.example.bagit.ui.theme.BagItTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BagItTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    LoginScreen()
                }
            }
        }
    }
}