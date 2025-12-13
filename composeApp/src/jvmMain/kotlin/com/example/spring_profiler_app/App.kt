package com.example.spring_profiler_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ActuatorRepository
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.refreshHealthState
import com.example.spring_profiler_app.data.refreshMetricsState
import com.example.spring_profiler_app.data.refreshState
import com.example.spring_profiler_app.ui.panels.AddServerForm
import com.example.spring_profiler_app.ui.panels.ServerDetailsPanel
import com.example.spring_profiler_app.ui.panels.ServerListPanel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

val Repository = compositionLocalOf<ActuatorRepository> { error("Undefined repository") }

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val servers = remember { mutableStateMapOf<Server, ServerState>() }
            val currentServerKey = remember { mutableStateOf<Server?>(null) }
            val scope = rememberCoroutineScope()
            val ioScope = remember(scope) {
                CoroutineScope(scope.coroutineContext + Dispatchers.IO + SupervisorJob(scope.coroutineContext[Job]))
            }

            val refreshHealthCallback: suspend () -> Unit = {
                currentServerKey.value?.let { server ->
                    servers.refreshHealthState(server, repo)
                }
            }

            val refreshMetricsCallback: suspend () -> Unit = {
                currentServerKey.value?.let { server ->
                    servers.refreshMetricsState(server, repo)
                }
            }

            Row(Modifier.fillMaxHeight()) {
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    ServerListPanel(
                        servers = servers,
                        currentServerKey = currentServerKey.value,
                        onAddServerClick = { currentServerKey.value = null },
                        onServerSelect = { server -> currentServerKey.value = server },
                        onRefreshServer = { server ->
                            ioScope.launch { servers.refreshState(server, repo) }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VerticalDivider(
                    modifier = Modifier.width(1.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val currentServerState = currentServerKey.value?.let { servers[it] }
                    if (currentServerState == null) {
                        AddServerForm(
                            servers = servers,
                            onServerAdded = { newServer ->
                                ioScope.launch {
                                    servers.refreshState(newServer, repo)
                                }
                            },
                            ioScope = ioScope,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ServerDetailsPanel(
                            serverState = currentServerState,
                            refreshHealthCallback = refreshHealthCallback,
                            refreshMetricsCallback = refreshMetricsCallback,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
