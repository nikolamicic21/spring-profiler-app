package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
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
import com.example.spring_profiler_app.data.AggregatedBeansResponse
import com.example.spring_profiler_app.data.Bean
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.ui.components.AppTooltip
import com.example.spring_profiler_app.ui.components.EndpointContextBadges
import com.example.spring_profiler_app.ui.components.FilterBarContainer
import com.example.spring_profiler_app.ui.components.FilterChipGroup
import com.example.spring_profiler_app.ui.components.FilterableScreenLayout
import com.example.spring_profiler_app.ui.components.ScopeBadge
import com.example.spring_profiler_app.ui.components.SearchBar
import com.example.spring_profiler_app.ui.components.UIStateWrapper

data class BeanItem(
    val name: String,
    val bean: Bean,
    val context: String,
    val endpoint: String
)

@Composable
fun BeansScreen(beansState: UIState<AggregatedBeansResponse>) {
    UIStateWrapper(
        state = beansState,
        loadingMessage = "Loading beans..."
    ) { data ->
        BeansContent(data)
    }
}

@Composable
private fun BeansContent(beansResponse: AggregatedBeansResponse) {
    val allBeans = remember(beansResponse) {
        beansResponse.endpoints.flatMap { endpointBeans ->
            endpointBeans.contexts.flatMap { (contextName, beans) ->
                beans.beans.map { (beanName, bean) ->
                    BeanItem(
                        name = beanName,
                        bean = bean,
                        context = contextName,
                        endpoint = endpointBeans.endpoint
                    )
                }
            }
        }
    }

    val allContexts = remember(allBeans) {
        allBeans.map { it.context }.distinct().sorted()
    }

    val allScopes = remember(allBeans) {
        allBeans.map { it.bean.scope }.distinct().sorted()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedContext by remember { mutableStateOf<String?>(null) }
    var selectedScope by remember { mutableStateOf<String?>(null) }

    val filteredBeans = remember(searchQuery, selectedContext, selectedScope, allBeans) {
        allBeans.filter { beanItem ->
            val matchesSearch = beanItem.name.contains(searchQuery, ignoreCase = true)
            val matchesContext = selectedContext == null || beanItem.context == selectedContext
            val matchesScope = selectedScope == null || beanItem.bean.scope == selectedScope
            matchesSearch && matchesContext && matchesScope
        }
    }

    FilterableScreenLayout(
        isEmpty = filteredBeans.isEmpty(),
        emptyStateMessage = "No beans match your filters.",
        filterBar = {
            BeansFilterBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                contexts = allContexts,
                selectedContext = selectedContext,
                onContextSelect = { selectedContext = it },
                scopes = allScopes,
                selectedScope = selectedScope,
                onScopeSelect = { selectedScope = it }
            )
        }
    ) { filterBarHeight ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 450.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = filterBarHeight + 16.dp,
                start = 16.dp, end = 16.dp, bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp
        ) {
            items(filteredBeans) { beanItem ->
                BeanCard(
                    beanItem = beanItem,
                    onDependencyClick = { depName ->
                        searchQuery = depName
                        selectedScope = null
                        selectedContext = beanItem.context
                    }
                )
            }
        }
    }
}

@Composable
private fun BeansFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    contexts: List<String>,
    selectedContext: String?,
    onContextSelect: (String?) -> Unit,
    scopes: List<String>,
    selectedScope: String?,
    onScopeSelect: (String?) -> Unit
) {
    FilterBarContainer {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchChange,
            placeholder = "Search beans by name..."
        )

        FilterChipGroup(
            label = "Context",
            options = contexts,
            selectedOption = selectedContext,
            onOptionSelect = onContextSelect,
            modifier = Modifier.fillMaxWidth(),
            collapsible = true,
        )

        FilterChipGroup(
            label = "Scope",
            options = scopes,
            selectedOption = selectedScope,
            onOptionSelect = onScopeSelect,
            modifier = Modifier.fillMaxWidth(),
            collapsible = true,
        )
    }
}

@Composable
fun BeanCard(beanItem: BeanItem, onDependencyClick: (String) -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EndpointContextBadges(
                endpoint = beanItem.endpoint,
                context = beanItem.context
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                AppTooltip(text = beanItem.name) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = beanItem.name.substringAfterLast("."),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                        Text(
                            text = beanItem.name.substringBeforeLast(".", ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScopeBadge(scope = beanItem.bean.scope)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DEPENDENCIES (${beanItem.bean.dependencies.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )

            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (beanItem.bean.dependencies.isEmpty()) {
                    Text(
                        "No dependencies",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                } else {
                    beanItem.bean.dependencies.forEach { dep ->
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
