package com.simonfx.app.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.simonfx.app.BuildConfig
import com.simonfx.app.ui.home.SimonHomeScreen
import com.simonfx.app.ui.screens.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    contentPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(contentPadding)
    ) {
        composable("usd_btc") {
            SimonHomeScreen(
                wsBase = BuildConfig.WS_BASE,
                httpBase = BuildConfig.HTTP_BASE,
                targetSymbols = "BTCUSDT"
            )
        }
        composable("usd_jpy") {
            SimonHomeScreen(
                wsBase = BuildConfig.WS_BASE,
                httpBase = BuildConfig.HTTP_BASE,
                targetSymbols = "USDJPY"
            )
        }
        composable("xau_usd") {
            SimonHomeScreen(
                wsBase = BuildConfig.WS_BASE,
                httpBase = BuildConfig.HTTP_BASE,
                targetSymbols = "XAUUSD"
            )
        }
        composable("settings") {
            SettingsScreen()
        }
    }
}
