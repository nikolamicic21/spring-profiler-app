package com.example.spring_profiler_app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration

@Composable
fun AutoRefresh(
    interval: Duration,
    onRefresh: suspend () -> Unit
) {
    LaunchedEffect(Unit) {
        while (isActive) {
            onRefresh()
            delay(interval)
        }
    }
}
