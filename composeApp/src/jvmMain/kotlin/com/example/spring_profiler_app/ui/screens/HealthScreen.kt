package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.ui.components.ApiStateWrapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

@Composable
fun HealthScreen(
    healthState: UIState<HealthResponse>,
    refreshHealthCallback: suspend () -> Unit
) {
    LaunchedEffect(Unit) {
        while (isActive) {
            refreshHealthCallback()
            delay(5.seconds)
        }
    }

    ApiStateWrapper(
        state = healthState,
    ) { data ->
        HealthContent(data)
    }
}

@Composable
private fun HealthContent(healthResponse: HealthResponse) {
    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(10.dp)) {
                Text(text = "health status: ${healthResponse.status}")
            }
        }

        if (healthResponse.components != null) {
            HorizontalDivider()
            healthResponse.components.forEach { component ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp)) {
                        Text(text = "${component.key} component status: ${component.value.status}")
                    }
                }
            }
        }
    }
}
