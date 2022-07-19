package io.chthonic.storeminal.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.chthonic.storeminal.presentation.terminal.TerminalScreen

@Composable
fun AppContainerNavHost(
    appContainerState: AppContainerState,
    padding: PaddingValues
) = NavHost(
    navController = appContainerState.navController,
    startDestination = Destination.Terminal.route,
    modifier = androidx.compose.ui.Modifier.padding(padding)
) {
    composable(Destination.Terminal.route) {
        TerminalScreen()
    }
}

sealed class Destination(val route: String) {
    object Terminal : Destination("terminal")
}