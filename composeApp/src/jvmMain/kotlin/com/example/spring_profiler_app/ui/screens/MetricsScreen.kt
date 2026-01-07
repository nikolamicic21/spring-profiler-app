package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spring_profiler_app.data.AggregatedMetricsResponse
import com.example.spring_profiler_app.data.Measurement
import com.example.spring_profiler_app.data.Metric
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.data.formatNumberWithoutGrouping
import com.example.spring_profiler_app.ui.components.AutoRefresh
import com.example.spring_profiler_app.ui.components.FilterChipGroup
import com.example.spring_profiler_app.ui.components.UIStateWrapper
import com.example.spring_profiler_app.ui.components.UnitBadge
import kotlin.time.Duration.Companion.seconds

@Composable
fun AggregatedMetricsScreen(
    metricsState: UIState<AggregatedMetricsResponse>,
    refreshMetricsCallback: suspend () -> Unit
) {
    val autoRefreshInterval = 3.seconds
    AutoRefresh(interval = autoRefreshInterval, onRefresh = refreshMetricsCallback)

    UIStateWrapper(
        state = metricsState,
        loadingMessage = "Loading metrics...",
        autoRefreshInterval = autoRefreshInterval
    ) { data ->
        AggregatedMetricsContent(data)
    }
}

@Composable
private fun AggregatedMetricsContent(metricsResponse: AggregatedMetricsResponse) {
    val endpointMetrics = metricsResponse.endpoints

    val allEndpoints = remember(endpointMetrics) {
        endpointMetrics.map { it.endpoint }
    }

    var selectedEndpoint by remember { mutableStateOf<String?>(null) }

    val filteredEndpoints = remember(selectedEndpoint, endpointMetrics) {
        if (selectedEndpoint == null) {
            endpointMetrics
        } else {
            endpointMetrics.filter { it.endpoint == selectedEndpoint }
        }
    }

    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    var filterBarHeightPx by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = filterBarHeightPx.dp + 16.dp,
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp
        ) {
            filteredEndpoints.forEach { endpointMetric ->
                val isExpanded = expandedStates[endpointMetric.endpoint] ?: true

                item(span = StaggeredGridItemSpan.FullLine) {
                    EndpointHeader(
                        endpointMetric = endpointMetric,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            expandedStates[endpointMetric.endpoint] = !isExpanded
                        }
                    )
                }

                if (isExpanded && endpointMetric.metrics.isNotEmpty()) {
                    val groupedMetrics = endpointMetric.metrics.groupBy { it.name.substringBefore(".") }

                    groupedMetrics.forEach { (prefix, metricsList) ->
                        item {
                            MetricCategoryCard(prefix, metricsList)
                        }
                    }
                }
            }
        }

        if (allEndpoints.isNotEmpty()) {
            MetricsFilterBar(
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
private fun MetricsFilterBar(
    endpoints: List<String>,
    selectedEndpoint: String?,
    onEndpointSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
private fun EndpointHeader(
    endpointMetric: AggregatedMetricsResponse.EndpointMetrics,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpand)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = endpointMetric.endpoint,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Badge {
            Text("${endpointMetric.metrics.size} metrics")
        }
    }
}

@Composable
fun MetricCategoryCard(prefix: String, metrics: List<Metric>) {
    ElevatedCard(
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = prefix.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    metrics.forEach { metric ->
                        MetricBlock(metric)
                    }
                }
            }
        }
    }
}

@Composable
fun MetricBlock(metric: Metric) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = metric.name.substringAfter("."),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!metric.unit.isNullOrBlank()) {
                UnitBadge(unit = metric.unit)
            }
        }

        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            metric.measurements.forEach { measurement ->
                MeasurementRow(measurement)
            }
        }
    }
}

@Composable
fun MeasurementRow(measurement: Measurement) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = measurement.statistic,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        Text(
            text = formatNumberWithoutGrouping(measurement.value),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            ),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
