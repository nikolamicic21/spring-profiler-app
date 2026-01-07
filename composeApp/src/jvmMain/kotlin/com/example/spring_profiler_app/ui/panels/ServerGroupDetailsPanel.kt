package com.example.spring_profiler_app.ui.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ActuatorEndpoints
import com.example.spring_profiler_app.data.ServerGroupState
import com.example.spring_profiler_app.data.getAggregatedBeans
import com.example.spring_profiler_app.data.getAggregatedConfigProps
import com.example.spring_profiler_app.data.getAggregatedHealth
import com.example.spring_profiler_app.data.getAggregatedMetrics
import com.example.spring_profiler_app.ui.screens.AggregatedHealthScreen
import com.example.spring_profiler_app.ui.screens.AggregatedMetricsScreen
import com.example.spring_profiler_app.ui.screens.BeansScreen
import com.example.spring_profiler_app.ui.screens.ConfigPropsScreen

@Composable
fun ServerGroupDetailsPanel(
    groupState: ServerGroupState,
    refreshHealthCallback: suspend () -> Unit,
    refreshMetricsCallback: suspend () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = ActuatorEndpoints.entries

    Column(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = groupState.group.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${groupState.group.endpoints.size} endpoint${if (groupState.group.endpoints.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        HorizontalDivider()

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, endpoint ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(endpoint.title) }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (tabs[selectedTab]) {
                ActuatorEndpoints.BEANS -> {
                    val aggregatedBeans = remember(groupState.endpointStates) {
                        groupState.getAggregatedBeans()
                    }
                    BeansScreen(beansState = aggregatedBeans)
                }

                ActuatorEndpoints.HEALTH -> {
                    val aggregatedHealth = remember(groupState.endpointStates) {
                        groupState.getAggregatedHealth()
                    }
                    AggregatedHealthScreen(
                        healthState = aggregatedHealth,
                        refreshHealthCallback = refreshHealthCallback
                    )
                }

                ActuatorEndpoints.CONFIG_PROPS -> {
                    val aggregatedConfigProps = remember(groupState.endpointStates) {
                        groupState.getAggregatedConfigProps()
                    }
                    ConfigPropsScreen(configPropsState = aggregatedConfigProps)
                }

                ActuatorEndpoints.METRICS -> {
                    val aggregatedMetrics = remember(groupState.endpointStates) {
                        groupState.getAggregatedMetrics()
                    }
                    AggregatedMetricsScreen(
                        metricsState = aggregatedMetrics,
                        refreshMetricsCallback = refreshMetricsCallback
                    )
                }
            }
        }
    }
}
