package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ApiUiState

@Composable
fun <T> ApiStateWrapper(
    state: ApiUiState<T>,
    content: @Composable (T) -> Unit,
) {
    Box {
        when (state) {
            is ApiUiState.Loading -> {
                Box(Modifier.padding(16.dp)) {
                    Text(text = "Loading...")
                }
            }

            is ApiUiState.Error -> {
                Box(Modifier.padding(16.dp)) {
                    Text(text = state.message)
                }
            }

            is ApiUiState.Success -> {
                content(state.data)
            }
        }
    }
}
