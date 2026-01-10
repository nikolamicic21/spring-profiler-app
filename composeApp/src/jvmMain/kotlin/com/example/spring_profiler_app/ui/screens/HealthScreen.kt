package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.AggregatedHealthResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.ui.components.AutoRefresh
import com.example.spring_profiler_app.ui.components.FilterBarContainer
import com.example.spring_profiler_app.ui.components.FilterChipGroup
import com.example.spring_profiler_app.ui.components.StatusBadge
import com.example.spring_profiler_app.ui.components.UIStateWrapper
import kotlin.time.Duration.Companion.seconds

@Composable
fun AggregatedHealthScreen(
    healthState: UIState<AggregatedHealthResponse>,
    refreshHealthCallback: suspend () -> Unit
) {
    val autoRefreshInterval = 5.seconds
    AutoRefresh(interval = autoRefreshInterval, onRefresh = refreshHealthCallback)

    UIStateWrapper(
        state = healthState,
        loadingMessage = "Loading health status...",
        autoRefreshInterval = autoRefreshInterval
    ) { data ->
        AggregatedHealthContent(data)
    }
}

@Composable
private fun AggregatedHealthContent(healthResponse: AggregatedHealthResponse) {
    val endpointGroups = healthResponse.endpoints

    val allEndpoints = remember(endpointGroups) {
        endpointGroups.map { it.endpoint }
    }

    var selectedEndpoint by remember { mutableStateOf<String?>(null) }

    val filteredGroups = remember(selectedEndpoint, endpointGroups) {
        if (selectedEndpoint == null) {
            endpointGroups
        } else {
            endpointGroups.filter { it.endpoint == selectedEndpoint }
        }
    }

    var filterBarHeightPx by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = filterBarHeightPx.dp + 16.dp,
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalItemSpacing = 12.dp,
        ) {
            item(span = StaggeredGridItemSpan.FullLine) {
                GlobalHealthHero(healthResponse.status)
            }

            if (filteredGroups.isNotEmpty()) {
                filteredGroups.forEach { endpointHealth ->
                    item(span = StaggeredGridItemSpan.FullLine) {
                        EndpointHealthCard(endpointHealth)
                    }
                }
            }
        }

        if (allEndpoints.isNotEmpty()) {
            HealthFilterBar(
                endpoints = allEndpoints,
                selectedEndpoint = selectedEndpoint,
                onEndpointSelect = { selectedEndpoint = it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged { size -> filterBarHeightPx = size.height }
            )
        }
    }
}

@Composable
private fun HealthFilterBar(
    endpoints: List<String>,
    selectedEndpoint: String?,
    onEndpointSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterBarContainer(modifier = modifier) {
        FilterChipGroup(
            label = "Endpoint",
            options = endpoints,
            selectedOption = selectedEndpoint,
            onOptionSelect = onEndpointSelect,
            collapsible = true,
        )
    }
}

@Composable
fun EndpointHealthCard(endpointHealth: AggregatedHealthResponse.EndpointHealth) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = endpointHealth.endpoint,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatusBadge(status = endpointHealth.status)
            }

            if (endpointHealth.components.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "COMPONENTS (${endpointHealth.components.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    endpointHealth.components.forEach { (componentName, status) ->
                        ComponentStatusRow(componentName, status)
                    }
                }
            }
        }
    }
}

@Composable
fun ComponentStatusRow(name: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        StatusBadge(status = status, dotSize = 6.dp)
    }
}

@Composable
fun GlobalHealthHero(status: String) {
    val isUp = status.equals("UP", ignoreCase = true)
    val color = if (isUp) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIcon(status, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = "System is ${status.uppercase()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = color
                )
                Text(
                    text = if (isUp) "All systems operational" else "Action required: Some components are failing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatusIcon(status: String, modifier: Modifier = Modifier) {
    val (icon, color) = when (status.uppercase()) {
        "UP" -> Icons.Default.CheckCircle to Color(0xFF2E7D32)
        "DOWN" -> Icons.Default.Error to Color(0xFFD32F2F)
        "OUT_OF_SERVICE" -> Icons.Default.Warning to Color(0xFFED6C02)
        else -> Icons.Default.Info to Color.Gray
    }
    Icon(imageVector = icon, contentDescription = status, tint = color, modifier = modifier)
}
