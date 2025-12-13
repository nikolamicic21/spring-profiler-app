package com.example.spring_profiler_app.ui.panels

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.spring_profiler_app.data.ActuatorEndpoints
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.ui.screens.BeansScreen
import com.example.spring_profiler_app.ui.screens.ConfigPropsScreen
import com.example.spring_profiler_app.ui.screens.HealthScreen
import com.example.spring_profiler_app.ui.screens.MetricsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailsPanel(
    serverState: ServerState,
    refreshHealthCallback: suspend () -> Unit,
    refreshMetricsCallback: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedEndpoint = remember { mutableStateOf(ActuatorEndpoints.BEANS) }

    Scaffold(
        modifier = modifier,
        topBar = {
            EndpointTabs(
                selectedEndpoint = selectedEndpoint,
                onEndpointSelect = { endpoint -> selectedEndpoint.value = endpoint }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedEndpoint.value) {
                ActuatorEndpoints.BEANS -> BeansScreen(serverState.beans)
                ActuatorEndpoints.HEALTH -> HealthScreen(serverState.health, refreshHealthCallback)
                ActuatorEndpoints.CONFIG_PROPS -> ConfigPropsScreen(serverState.configProps)
                ActuatorEndpoints.METRICS -> MetricsScreen(serverState.metrics, refreshMetricsCallback)
            }
        }
    }
}

@Composable
private fun EndpointTabs(
    selectedEndpoint: MutableState<ActuatorEndpoints>,
    onEndpointSelect: (ActuatorEndpoints) -> Unit
) {
    TabRow(
        selectedTabIndex = ActuatorEndpoints.entries.indexOf(selectedEndpoint.value),
        modifier = Modifier.fillMaxWidth()
    ) {
        ActuatorEndpoints.entries.forEach { endpoint ->
            Tab(
                selected = endpoint == selectedEndpoint.value,
                onClick = { onEndpointSelect(endpoint) },
                text = { Text(endpoint.title) }
            )
        }
    }
}
