package com.example.spring_profiler_app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.UIState
import kotlinx.coroutines.delay
import kotlin.time.Duration

@Composable
fun <T> UIStateWrapper(
    state: UIState<T>,
    loadingMessage: String = "Loading...",
    loadingDebounceMs: Long = 50L,
    autoRefreshInterval: Duration? = null,
    content: @Composable (T) -> Unit,
) {
    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(state, showLoading) {
        if (state is UIState.Loading) {
            delay(loadingDebounceMs)
            showLoading = true
        } else {
            showLoading = false
        }
    }

    Box {
        when (state) {
            is UIState.Loading -> {
                AnimatedVisibility(
                    visible = showLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LoadingIndicator(message = loadingMessage)
                }
            }

            is UIState.Error -> {
                ErrorMessage(
                    message = state.message,
                    autoRefreshInterval = autoRefreshInterval
                )
            }

            is UIState.Success -> {
                content(state.data)
            }
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    autoRefreshInterval: Duration? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (autoRefreshInterval != null) {
                Spacer(modifier = Modifier.height(8.dp))

                CompactLoadingIndicator()

                Text(
                    text = "Retrying in ${autoRefreshInterval.inWholeSeconds}s...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
