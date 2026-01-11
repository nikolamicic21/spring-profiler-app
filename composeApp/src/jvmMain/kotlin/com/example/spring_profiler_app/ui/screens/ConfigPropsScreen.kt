package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spring_profiler_app.data.AggregatedConfigPropsResponse
import com.example.spring_profiler_app.data.ContextEndpointItem
import com.example.spring_profiler_app.data.ContextFilterOption
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.data.buildContextFilterOptions
import com.example.spring_profiler_app.data.flattenConfigPropsObject
import com.example.spring_profiler_app.data.matches
import com.example.spring_profiler_app.ui.components.EndpointContextBadges
import com.example.spring_profiler_app.ui.components.FilterBarContainer
import com.example.spring_profiler_app.ui.components.FilterChipGroup
import com.example.spring_profiler_app.ui.components.FilterableScreenLayout
import com.example.spring_profiler_app.ui.components.SearchBar
import com.example.spring_profiler_app.ui.components.UIStateWrapper

@Composable
fun ConfigPropsScreen(configPropsState: UIState<AggregatedConfigPropsResponse>) {
    UIStateWrapper(
        state = configPropsState,
        loadingMessage = "Loading configuration properties..."
    ) { data ->
        ConfigPropsContent(data)
    }
}

@Composable
private fun ConfigPropsContent(configPropsResponse: AggregatedConfigPropsResponse) {
    val allConfigProps = remember(configPropsResponse) {
        configPropsResponse.endpoints.flatMap { endpointConfigProps ->
            endpointConfigProps.contexts.flatMap { (contextName, context) ->
                context.beans.map { (_, beanProperties) ->
                    val flatProps = mutableMapOf<String, String>()
                    flattenConfigPropsObject(beanProperties.properties, "", flatProps)
                    ConfigPropsItem(
                        prefix = beanProperties.prefix,
                        props = flatProps,
                        context = contextName,
                        endpoint = endpointConfigProps.endpoint
                    )
                }
            }
        }
    }

    val contextFilterOptions = remember(allConfigProps) {
        allConfigProps.buildContextFilterOptions()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedContextOption by remember { mutableStateOf<ContextFilterOption?>(null) }

    val filteredProps = remember(searchQuery, selectedContextOption, allConfigProps) {
        val contextFilter = selectedContextOption
        allConfigProps.filter {
            val matchesSearch = it.prefix.contains(searchQuery, ignoreCase = true)
            val matchesContext = contextFilter == null || contextFilter.matches(it)
            matchesSearch && matchesContext
        }.filter { it.props.isNotEmpty() }
    }

    FilterableScreenLayout(
        isEmpty = filteredProps.isEmpty(),
        emptyStateMessage = "No configuration properties match your filters.",
        filterBar = {
            ConfigPropsFilterBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                contextOptions = contextFilterOptions,
                selectedContextOption = selectedContextOption,
                onContextSelect = { selectedContextOption = it }
            )
        }
    ) { filterBarHeight ->
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(minSize = 450.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = filterBarHeight + 16.dp,
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalItemSpacing = 16.dp
        ) {
            items(filteredProps) { config ->
                ConfigGroupCard(config)
            }
        }
    }
}

@Composable
private fun ConfigPropsFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    contextOptions: List<ContextFilterOption>,
    selectedContextOption: ContextFilterOption?,
    onContextSelect: (ContextFilterOption?) -> Unit
) {
    FilterBarContainer {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchChange,
            placeholder = "Search prefixes (e.g., server, datasource)..."
        )

        FilterChipGroup(
            label = "Context",
            options = contextOptions,
            selectedOption = selectedContextOption,
            onOptionSelect = onContextSelect,
            optionLabel = { it.displayLabel },
            collapsible = true,
        )
    }
}

@Composable
fun ConfigGroupCard(config: ConfigPropsItem) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            EndpointContextBadges(
                endpoint = config.endpoint,
                context = config.context
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = config.prefix,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)

            SelectionContainer {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    config.props.forEach { (key, value) ->
                        PropertyRow(key, value)
                    }
                }
            }
        }
    }
}

@Composable
fun PropertyRow(keyExtension: String, propertyValue: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = keyExtension,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(8.dp)
        ) {
            Text(
                text = propertyValue,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class ConfigPropsItem(
    val prefix: String,
    val props: Map<String, String>,
    override val context: String,
    override val endpoint: String
) : ContextEndpointItem
