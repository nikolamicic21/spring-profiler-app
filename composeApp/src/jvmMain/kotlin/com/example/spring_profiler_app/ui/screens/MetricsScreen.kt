package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ApiUiState
import com.example.spring_profiler_app.data.MetricsResponse
import com.example.spring_profiler_app.data.formatNumberWithoutGrouping
import com.example.spring_profiler_app.ui.components.ApiStateWrapper
import com.example.spring_profiler_app.ui.components.ScrollableContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

@Composable
fun MetricsScreen(
    metricsState: ApiUiState<MetricsResponse>,
    refreshMetricsCallback: suspend () -> Unit
) {
    LaunchedEffect(Unit) {
        while (isActive) {
            refreshMetricsCallback()
            delay(3.seconds)
        }
    }

    ApiStateWrapper(
        state = metricsState,
    ) { data ->
        MetricsContent(data)
    }
}

@Composable
private fun MetricsContent(metricsResponse: MetricsResponse) {
    ScrollableContent {
        metricsResponse.metrics.forEach { metric ->
            Card(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = "Metric name: ${metric.name}")
                }

                if (!metric.unit.isNullOrBlank()) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Base unit: ${metric.unit}")
                    }
                }

                if (metric.measurements.isNotEmpty()) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Measurements:")
                        metric.measurements.forEach { measurement ->
                            Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                                Text(
                                    text = "•",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${measurement.statistic}: ${formatNumberWithoutGrouping(measurement.value)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
