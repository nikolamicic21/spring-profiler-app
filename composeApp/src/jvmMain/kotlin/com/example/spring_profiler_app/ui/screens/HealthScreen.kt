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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.ui.components.AutoRefresh
import com.example.spring_profiler_app.ui.components.UIStateWrapper
import kotlin.time.Duration.Companion.seconds

@Composable
fun HealthScreen(
    healthState: UIState<HealthResponse>,
    refreshHealthCallback: suspend () -> Unit
) {
    AutoRefresh(interval = 5.seconds, onRefresh = refreshHealthCallback)

    UIStateWrapper(
        state = healthState,
    ) { data ->
        HealthContent(data)
    }
}

@Composable
private fun HealthContent(healthResponse: HealthResponse) {
    val filteredComponents = healthResponse.components?.toList() ?: emptyList()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 240.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                GlobalHealthHero(healthResponse.status)
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = "System Components",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline
                )
            }

            items(filteredComponents) { (name, component) ->
                ComponentStatusCard(name, component.status)
            }
        }
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
fun ComponentStatusCard(name: String, status: String) {
    val statusColor = getStatusColor(status)

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
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

fun getStatusColor(status: String): Color = when (status.uppercase()) {
    "UP" -> Color(0xFF2E7D32)
    "DOWN" -> Color(0xFFD32F2F)
    "OUT_OF_SERVICE" -> Color(0xFFED6C02)
    else -> Color.Gray
}
