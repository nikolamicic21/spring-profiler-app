package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spring_profiler_app.data.Bean
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.ui.components.AppTooltip
import com.example.spring_profiler_app.ui.components.EmptyState
import com.example.spring_profiler_app.ui.components.ScopeBadge
import com.example.spring_profiler_app.ui.components.SearchBar
import com.example.spring_profiler_app.ui.components.UIStateWrapper

@Composable
fun BeansScreen(beansState: UIState<BeansResponse>) {
    UIStateWrapper(
        state = beansState,
        loadingMessage = "Loading beans..."
    ) { data ->
        BeansContent(data)
    }
}

@Composable
private fun BeansContent(beansResponse: BeansResponse) {
    val beans = beansResponse.contexts.values.flatMap { beans ->
        beans.beans.map { bean -> Pair(bean.key, bean.value) }
    }

    var searchQuery by remember { mutableStateOf("") }
    val searchBarHeight = 90.dp

    val filteredBeans = remember(searchQuery, beans) {
        beans.filter { (name, _) ->
            name.contains(searchQuery, ignoreCase = true)
        }.toList()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (filteredBeans.isEmpty()) {
            EmptyState(message = "No bean names match your search.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 450.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = searchBarHeight + 16.dp,
                    start = 16.dp, end = 16.dp, bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredBeans) { (name, bean) ->
                    BeanCard(
                        name = name,
                        bean = bean,
                        onDependencyClick = { depName -> searchQuery = depName }
                    )
                }
            }
        }

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            placeholder = "Search beans by name...",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun BeanCard(name: String, bean: Bean, onDependencyClick: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AppTooltip(text = name) {
                Column {
                    Text(
                        text = name.substringAfterLast("."),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(
                        text = name.substringBeforeLast(".", ""),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ScopeBadge(scope = bean.scope)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DEPENDENCIES (${bean.dependencies.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (bean.dependencies.isEmpty()) {
                    Text(
                        "No dependencies",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    bean.dependencies.forEach { dep ->
                        DependencyTag(dep, onDependencyClick)
                    }
                }
            }
        }
    }
}

@Composable
fun DependencyTag(depName: String, onClick: (String) -> Unit) {
    AppTooltip(text = depName) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick(depName) },
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = depName,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}
