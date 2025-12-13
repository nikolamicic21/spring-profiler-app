package com.example.spring_profiler_app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ActuatorEndpoints
import com.example.spring_profiler_app.data.ActuatorRepository
import com.example.spring_profiler_app.data.ApiUiState
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.data.ConfigPropsResponse
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.MetricsResponse
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.flattenConfigPropsObject
import com.example.spring_profiler_app.data.formatNumberWithoutGrouping
import com.example.spring_profiler_app.data.refreshHealthState
import com.example.spring_profiler_app.data.refreshMetricsState
import com.example.spring_profiler_app.data.refreshState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

val Repository = compositionLocalOf<ActuatorRepository> { error("Undefined repository") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        val servers = remember { mutableStateMapOf<Server, ServerState>() }
        val currentServerKey = remember { mutableStateOf<Server?>(null) }
        val scope = rememberCoroutineScope()
        val ioScope = remember(scope) {
            CoroutineScope(scope.coroutineContext + Dispatchers.IO + SupervisorJob(scope.coroutineContext[Job]))
        }

        val refreshHealthCallback: suspend () -> Unit = {
            currentServerKey.value?.let { server ->
                servers.refreshHealthState(server)
            }
        }

        val refreshMetricsCallback: suspend () -> Unit = {
            currentServerKey.value?.let { server ->
                servers.refreshMetricsState(server)
            }
        }

        Box {
            Row(Modifier.fillMaxHeight()) {
                Box(modifier = Modifier.fillMaxWidth(0.3f), contentAlignment = Alignment.Center) {
                    Column {
                        Scaffold(topBar = {
                            TopAppBar(
                                title = {
                                    Box(
                                        Modifier.padding(10.dp).fillMaxWidth(), contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "Servers")
                                    }
                                },
                            )
                        }, content = { paddingValues ->
                            Box(Modifier.padding(paddingValues).fillMaxHeight()) {
                                val scroll = rememberScrollState()
                                Column(Modifier.verticalScroll(scroll).fillMaxHeight()) {
                                    Box(
                                        Modifier.padding(10.dp).fillMaxWidth(), contentAlignment = Alignment.Center
                                    ) {
                                        Button(onClick = {
                                            currentServerKey.value = null
                                        }) {
                                            Text("Add a new server")
                                        }
                                    }
                                    for (server in servers.keys.toList()) {
                                        val color =
                                            if (currentServerKey.value == server) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        Box(contentAlignment = Alignment.CenterStart) {
                                            Row(
                                                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                            ) {
                                                Box(modifier = Modifier.padding(2.dp).fillMaxWidth(0.7f)) {
                                                    Card(
                                                        colors = CardDefaults.cardColors(containerColor = color),
                                                        modifier = Modifier.clickable {
                                                            currentServerKey.value = server
                                                        }) {
                                                        Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                                                            Text(text = "${server.host}:${server.port}")
                                                        }
                                                    }
                                                }
                                                Box(
                                                    modifier = Modifier.padding(2.dp).fillMaxWidth(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    IconButton(onClick = {
                                                        ioScope.launch {
                                                            servers.refreshState(server)
                                                        }
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Refresh,
                                                            contentDescription = "Refresh data",
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                VerticalScrollbar(
                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                    adapter = rememberScrollbarAdapter(scrollState = scroll)
                                )
                            }
                        })
                    }
                }
                Box {
                    Row {
                        Column {
                            val currentServerState = currentServerKey.value?.let { servers[it] }
                            when (currentServerState) {
                                null -> Box {
                                    var host by rememberSaveable { mutableStateOf("") }
                                    var port by rememberSaveable { mutableStateOf("") }

                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp) // Spacing between elements
                                    ) {
                                        OutlinedTextField(
                                            value = host,
                                            onValueChange = { host = it },
                                            label = { Text("Server host") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        OutlinedTextField(
                                            value = port,
                                            onValueChange = { port = it },
                                            label = { Text("Server port") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                val newServer = Server(host, port.toInt())
                                                if (!servers.keys.contains(newServer)) {
                                                    servers[newServer] = ServerState(
                                                        newServer,
                                                        ApiUiState.Loading,
                                                        ApiUiState.Loading,
                                                        ApiUiState.Loading,
                                                        ApiUiState.Loading
                                                    )
                                                    ioScope.launch {
                                                        servers.refreshState(newServer)
                                                    }
                                                }
                                            },
                                            enabled = host.isNotBlank() && port.isNotBlank(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Connect")
                                        }
                                    }
                                }

                                else -> {
                                    val actuatorEndpoints = remember { mutableStateOf(ActuatorEndpoints.BEANS) }
                                    Scaffold(
                                        topBar = {
                                            // 2. TabRow component for navigation
                                            TabRow(
                                                selectedTabIndex = ActuatorEndpoints.entries.indexOf(
                                                    actuatorEndpoints.value
                                                ), modifier = Modifier.fillMaxWidth()
                                            ) {
                                                ActuatorEndpoints.entries.forEach { screen ->
                                                    Tab(
                                                        selected = screen == actuatorEndpoints.value,
                                                        onClick = {
                                                            actuatorEndpoints.value = screen
                                                        },
                                                        text = { Text(screen.title) },
                                                    )
                                                }
                                            }
                                        }) { paddingValues ->
                                        // 3. The content area, which uses the state to decide what to show
                                        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                                            ContentViewSwitcher(
                                                actuatorEndpoints.value,
                                                currentServerState.beans,
                                                currentServerState.health,
                                                refreshHealthCallback,
                                                currentServerState.configProps,
                                                currentServerState.metrics,
                                                refreshMetricsCallback,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContentViewSwitcher(
    screen: ActuatorEndpoints,
    beansState: ApiUiState<BeansResponse>,
    healthState: ApiUiState<HealthResponse>,
    refreshHealthCallback: suspend () -> Unit,
    configPropsState: ApiUiState<ConfigPropsResponse>,
    metricsState: ApiUiState<MetricsResponse>,
    refreshMetricsCallback: suspend () -> Unit,
) {
    when (screen) {
        ActuatorEndpoints.BEANS -> BeansScreen(beansState)
        ActuatorEndpoints.HEALTH -> HealthScreen(healthState, refreshHealthCallback)
        ActuatorEndpoints.CONFIG_PROPS -> ConfigPropsScreen(configPropsState)
        ActuatorEndpoints.METRICS -> MetricsScreen(metricsState, refreshMetricsCallback)
    }
}

@Composable
fun BeansScreen(beansState: ApiUiState<BeansResponse>) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    Box(Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (beansState) {
                is ApiUiState.Success -> {
                    val beans = beansState.data.contexts.values.flatMap { beans ->
                        beans.beans.map { bean ->
                            Pair(
                                bean.key, bean.value
                            )
                        }
                    }
                    var searchQuery by remember { mutableStateOf("") }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Active contexts: ${beansState.data.contexts.keys.joinToString(", ")}")
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search for Bean by name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().onKeyEvent { event ->
                                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                                        if (!searchQuery.isBlank()) {
                                            val targetIndex = beans.map { it.first }
                                                .indexOfFirst { it.contains(searchQuery, ignoreCase = true) }
                                            if (targetIndex != -1) {
                                                scope.launch {
                                                    lazyListState.animateScrollToItem(index = targetIndex)
                                                }
                                            }
                                        } else {
                                            scope.launch {
                                                lazyListState.animateScrollToItem(index = 0)
                                            }
                                        }

                                        true
                                    } else {
                                        false
                                    }
                                })
                            Spacer(modifier = Modifier.width(8.dp))
                            LazyColumn(
                                state = lazyListState, // Pass the state here
                                modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(beans) { bean ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                        ) {
                                            Text(text = "Bean name: ${bean.first}")
                                        }
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                        ) {
                                            Text(text = "Scope: ${bean.second.scope}")
                                        }
                                        if (bean.second.dependencies.isNotEmpty()) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                            ) {
                                                Text(
                                                    text = "Dependencies:"
                                                )
                                                for (dependency in bean.second.dependencies) {
                                                    Row(
                                                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = "•", // Standard bullet symbol
                                                            fontWeight = FontWeight.Bold,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            modifier = Modifier.clickable {
                                                                scope.launch {
                                                                    val targetIndex =
                                                                        beans.map { it.first }.indexOf(dependency)
                                                                    if (targetIndex != -1) {
                                                                        lazyListState.animateScrollToItem(index = targetIndex)
                                                                    }
                                                                }
                                                            },
                                                            text = dependency,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }

                is ApiUiState.Error -> {
                    Text(text = beansState.message)
                }

                is ApiUiState.Loading -> {
                    Text(text = "Loading...")
                }
            }
        }
    }
}


@Composable
fun HealthScreen(healthState: ApiUiState<HealthResponse>, refreshHealthCallback: suspend () -> Unit) {
    LaunchedEffect(Unit) {
        while (isActive) {
            refreshHealthCallback()
            delay(5.seconds)
        }
    }

    Box(Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (healthState) {
                is ApiUiState.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                ) {
                                    Text(text = "health status: ${healthState.data.status}")
                                }
                            }
                            if (healthState.data.components !== null) {
                                HorizontalDivider()
                                for (component in healthState.data.components) {
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                        ) {
                                            Text(text = "${component.key} component status: ${component.value.status}")
                                        }
                                    }
                                }
                            }

                        }

                    }
                }

                is ApiUiState.Error -> {
                    Text(text = healthState.message)
                }

                is ApiUiState.Loading -> {
                    Text(text = "Loading...")
                }
            }
        }
    }
}

@Composable
fun ConfigPropsScreen(configPropsState: ApiUiState<ConfigPropsResponse>) {
    Box(Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (configPropsState) {
                is ApiUiState.Success -> {
                    val flatProperties = mutableMapOf<String, String>()
                    for ((_, context) in configPropsState.data.contexts) {
                        for ((_, bean) in context.beans) {
                            val beanPrefix = bean.prefix
                            val beanProperties = bean.properties

                            val flatBeanProperties = mutableMapOf<String, String>()
                            flattenConfigPropsObject(beanProperties, "", flatBeanProperties)

                            flatBeanProperties.forEach { (propertyKey, value) ->
                                val fullKey = "$beanPrefix.$propertyKey"
                                flatProperties[fullKey] = value
                            }
                        }
                    }
                    Box {
                        val scroll = rememberScrollState()
                        Column(Modifier.verticalScroll(scroll).fillMaxHeight()) {
                            for (property in flatProperties) {
                                Card(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                    ) {
                                        Text(text = "${property.key}: ${property.value}")
                                    }
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState = scroll)
                        )
                    }
                }

                is ApiUiState.Error -> {
                    Box(Modifier.padding(16.dp)) {
                        Text(text = configPropsState.message)
                    }
                }

                is ApiUiState.Loading -> {
                    Box(Modifier.padding(16.dp)) {
                        Text(text = "Loading...")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricsScreen(metricsState: ApiUiState<MetricsResponse>, refreshMetricsCallback: suspend () -> Unit) {
    LaunchedEffect(Unit) {
        while (isActive) {
            refreshMetricsCallback()
            delay(3.seconds)
        }
    }


    Box(Modifier.fillMaxHeight()) {
        Column(modifier = Modifier.fillMaxSize()) {
            when (metricsState) {
                is ApiUiState.Success -> {
                    Box {
                        val scroll = rememberScrollState()
                        Column(Modifier.verticalScroll(scroll).fillMaxHeight()) {
                            for (metric in metricsState.data.metrics) {
                                Card(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                    ) {
                                        Text(text = "Metric name: ${metric.name}")
                                    }
                                    if (!metric.unit.isNullOrBlank()) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                        ) {
                                            Text(text = "Base unit: ${metric.unit}")
                                        }
                                    }
                                    if (metric.measurements.isNotEmpty()) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                        ) {
                                            Text(
                                                text = "Measurements:"
                                            )
                                            for (measurement in metric.measurements) {
                                                Row(
                                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                                ) {
                                                    Text(
                                                        text = "•",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${measurement.statistic}: ${
                                                            formatNumberWithoutGrouping(
                                                                measurement.value
                                                            )
                                                        }",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        VerticalScrollbar(
                            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                            adapter = rememberScrollbarAdapter(scrollState = scroll)
                        )
                    }
                }

                is ApiUiState.Error -> {
                    Box(Modifier.padding(16.dp)) {
                        Text(text = metricsState.message)
                    }
                }

                is ApiUiState.Loading -> {
                    Box(Modifier.padding(16.dp)) {
                        Text(text = "Loading...")
                    }
                }
            }
        }
    }
}