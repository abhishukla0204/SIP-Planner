package com.abhinav.sipplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.abhinav.sipplanner.ui.navigation.SipNavHost
import com.abhinav.sipplanner.ui.theme.SipPlannerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SipPlannerTheme {
                SipNavHost()
            }
        }
    }
}
