package io.chthonic.storeminal.presentation

import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.chthonic.rickmortychars.presentation.ktx.collectAsStateLifecycleAware
import io.chthonic.storeminal.R

@Preview
@Composable
fun AppContainer() {
    val appContainerState = rememberAppContainerState()
    val coroutineScope = rememberCoroutineScope()
    val appTitle = stringResource(R.string.app_name)
    LaunchedEffect(appTitle) {
        appContainerState.updateAppBarTitle(appTitle)
    }
    Scaffold(
        scaffoldState = appContainerState.scaffoldState,
        topBar = {
            // your top bar
            val appBarTitle = appContainerState.showAppBarTitle.collectAsStateLifecycleAware(
                initial = null,
                scope = coroutineScope
            )
            TopAppBar(title = { Text(appBarTitle.value ?: "") })
        },
        floatingActionButton = {
            // your floating action button
        },
        drawerContent = null,
        content = { padding ->
            // your page content
            AppContainerNavHost(
                appContainerState = appContainerState,
                padding = padding
            )
        },
        bottomBar = {
            // your bottom bar composable
        }
    )
}