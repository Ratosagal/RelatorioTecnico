package com.example.relatriotcnicomitsubishielectric

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.relatriotcnicomitsubishielectric.ui.LoginScreen
import com.example.relatriotcnicomitsubishielectric.ui.RatScreen
import com.example.relatriotcnicomitsubishielectric.ui.theme.RatTheme

class MainActivity : ComponentActivity() {

    private val vm: RatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm.init(this)
        setContent {
            RatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var loggedIn by remember { mutableStateOf(false) }
                    if (loggedIn) {
                        RatScreen(vm = vm)
                    } else {
                        LoginScreen(onLoginSuccess = { loggedIn = true })
                    }
                }
            }
        }
    }
}
