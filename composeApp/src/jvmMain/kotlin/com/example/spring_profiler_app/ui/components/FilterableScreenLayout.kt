package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FilterableScreenLayout(
    isEmpty: Boolean,
    emptyStateMessage: String,
    filterBar: @Composable () -> Unit,
    content: @Composable (filterBarHeight: Dp) -> Unit
) {
    var filterBarHeightPx by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (isEmpty) {
            EmptyState(
                message = emptyStateMessage,
                topPadding = filterBarHeightPx.dp
            )
        } else {
            content(filterBarHeightPx.dp)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { size -> filterBarHeightPx = size.height }
        ) {
            filterBar()
        }
    }
}

@Composable
fun FilterBarContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}
