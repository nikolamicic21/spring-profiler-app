package com.example.spring_profiler_app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
inline fun <T> rememberDebouncedCallback(
    debounceInterval: Long = 500L,
    crossinline onEventHandler: (T) -> Unit
): (T) -> Unit {
    var lastHandleTime by remember { mutableStateOf(0L) }

    return remember(debounceInterval) {
        { param: T ->
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastHandleTime >= debounceInterval) {
                lastHandleTime = currentTime
                onEventHandler(param)
            }
        }
    }
}
