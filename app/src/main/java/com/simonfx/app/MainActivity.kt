package com.simonfx.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.LocalImageLoader
import coil.decode.SvgDecoder
import com.simonfx.app.ui.AppNavHost
import com.simonfx.app.ui.BottomNavBar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val imageLoader = ImageLoader.Builder(this).components { add(SvgDecoder.Factory()) }.build()

        setContent {
            val navController = rememberNavController()
            CompositionLocalProvider(LocalImageLoader provides imageLoader) {
                Scaffold(
                    bottomBar = {
                        Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavHost(
                        navController = navController,
                        startDestination = "usd_btc",
                        contentPadding = innerPadding
                    )
                }
            }
        }
    }
}
