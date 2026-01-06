package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spring_profiler_app.data.ConfigPropsResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.data.flattenConfigPropsObject
import com.example.spring_profiler_app.ui.components.EmptyState
import com.example.spring_profiler_app.ui.components.SearchBar
import com.example.spring_profiler_app.ui.components.UIStateWrapper

@Composable
fun ConfigPropsScreen(configPropsState: UIState<ConfigPropsResponse>) {
    UIStateWrapper(
        state = configPropsState,
        loadingMessage = "Loading configuration properties..."
    ) { data ->
        ConfigPropsContent(data)
    }
}

data class ConfigProps(val prefix: String, val props: Map<String, String>)

@Composable
private fun ConfigPropsContent(configPropsResponse: ConfigPropsResponse) {
    val allConfigProps = mutableListOf<ConfigProps>()
    for ((_, context) in configPropsResponse.contexts) {
        for ((_, bean) in context.beans) {
            val beanPrefix = bean.prefix
            val beanProperties = bean.properties

            val flatBeanProperties = mutableMapOf<String, String>()
            flattenConfigPropsObject(beanProperties, "", flatBeanProperties)

            allConfigProps.add(ConfigProps(beanPrefix, flatBeanProperties))
        }
    }
    var searchQuery by remember { mutableStateOf("") }
    val filteredProps = remember(searchQuery, allConfigProps) {
        allConfigProps.filter {
            it.prefix.contains(searchQuery, ignoreCase = true)
        }
    }

    val searchBarHeight = 90.dp
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
        if (filteredProps.isEmpty()) {
            EmptyState(message = "No configuration prefixes match your search.")
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(minSize = 450.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = searchBarHeight + 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp
            ) {
                items(filteredProps.filter { it.props.isNotEmpty() }) { config ->
                    ConfigGroupCard(config)
                }
            }
        }

        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            placeholder = "Search prefixes (e.g., server, datasource)...",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun ConfigGroupCard(config: ConfigProps) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
