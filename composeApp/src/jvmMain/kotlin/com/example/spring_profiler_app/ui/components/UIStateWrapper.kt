package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.UIState

@Composable
fun <T> UIStateWrapper(
    state: UIState<T>,
    content: @Composable (T) -> Unit,
) {
    Box {
        when (state) {
            is UIState.Loading -> {
                Box(Modifier.padding(16.dp)) {
                    Text(text = "Loading...")
                }
            }

            is UIState.Error -> {
                Box(Modifier.padding(16.dp)) {
                    Text(text = state.message)
                }
            }

            is UIState.Success -> {
                content(state.data)
            }
        }
    }
}
