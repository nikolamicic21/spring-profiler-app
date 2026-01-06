package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spring_profiler_app.data.Measurement
import com.example.spring_profiler_app.data.Metric
import com.example.spring_profiler_app.data.MetricsResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.data.formatNumberWithoutGrouping
import com.example.spring_profiler_app.ui.components.AutoRefresh
import com.example.spring_profiler_app.ui.components.UIStateWrapper
import com.example.spring_profiler_app.ui.components.UnitBadge
import kotlin.time.Duration.Companion.seconds

@Composable
fun MetricsScreen(
    metricsState: UIState<MetricsResponse>,
    refreshMetricsCallback: suspend () -> Unit
) {
    val autoRefreshInterval = 3.seconds
    AutoRefresh(interval = autoRefreshInterval, onRefresh = refreshMetricsCallback)

    UIStateWrapper(
        state = metricsState,
        loadingMessage = "Loading metrics...",
        autoRefreshInterval = autoRefreshInterval
    ) { data ->
        MetricsContent(data)
    }
}

@Composable
private fun MetricsContent(metricsResponse: MetricsResponse) {
    val groupedMetrics = metricsResponse.metrics.groupBy { it.name.substringBefore(".") }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp,
    ) {
        groupedMetrics.forEach { (prefix, metrics) ->
            item {
                MetricCategoryCard(prefix, metrics)
            }
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
            Text(
                text = metric.name.substringAfter("."),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

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
