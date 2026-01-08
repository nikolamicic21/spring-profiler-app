package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Badge
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spring_profiler_app.data.AggregatedConfigPropsResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.data.flattenConfigPropsObject
import com.example.spring_profiler_app.ui.components.EmptyState
import com.example.spring_profiler_app.ui.components.FilterChipGroup
import com.example.spring_profiler_app.ui.components.SearchBar
import com.example.spring_profiler_app.ui.components.UIStateWrapper

data class ConfigPropsItem(
    val prefix: String,
    val props: Map<String, String>,
    val context: String,
    val endpoint: String
)

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

    val allEndpoints = remember(configPropsResponse) {
        configPropsResponse.endpoints.map { it.endpoint }
    }

    val allContexts = remember(allConfigProps) {
        allConfigProps.map { it.context }.distinct().sorted()
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedEndpoint by remember { mutableStateOf<String?>(null) }
    var selectedContext by remember { mutableStateOf<String?>(null) }

    val filteredProps = remember(searchQuery, selectedEndpoint, selectedContext, allConfigProps) {
        allConfigProps.filter {
            val matchesSearch = it.prefix.contains(searchQuery, ignoreCase = true)
            val matchesEndpoint = selectedEndpoint == null || it.endpoint == selectedEndpoint
            val matchesContext = selectedContext == null || it.context == selectedContext
            matchesSearch && matchesEndpoint && matchesContext
        }.filter { it.props.isNotEmpty() }
    }

    var filterBarHeightPx by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (filteredProps.isEmpty()) {
            EmptyState(
                message = "No configuration properties match your filters.",
                topPadding = filterBarHeightPx.dp
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 450.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = filterBarHeightPx.dp + 16.dp,
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

        ConfigPropsFilterBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            endpoints = allEndpoints,
            selectedEndpoint = selectedEndpoint,
            onEndpointSelect = { selectedEndpoint = it },
            contexts = allContexts,
            selectedContext = selectedContext,
            onContextSelect = { selectedContext = it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .onSizeChanged { size -> filterBarHeightPx = size.height }
        )
    }
}

@Composable
fun ConfigPropsFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    endpoints: List<String>,
    selectedEndpoint: String?,
    onEndpointSelect: (String?) -> Unit,
    contexts: List<String>,
    selectedContext: String?,
    onContextSelect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchChange,
            placeholder = "Search prefixes (e.g., server, datasource)..."
        )

        FilterChipGroup(
            label = "Endpoint",
            options = endpoints,
            selectedOption = selectedEndpoint,
            onOptionSelect = onEndpointSelect,
            collapsible = true,
        )

        FilterChipGroup(
            label = "Context",
            options = contexts,
            selectedOption = selectedContext,
            onOptionSelect = onContextSelect,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = config.endpoint,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = config.context,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

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
