package com.example.spring_profiler_app.data

import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private suspend fun <T> SnapshotStateMap<Server, ServerState>.refreshEndpoint(
    server: Server,
    endpoint: ActuatorEndpoints,
    fetchData: suspend () -> T,
    updateState: ServerState.(UIState<T>) -> ServerState,
    showLoadingOnRefresh: Boolean = false
) {
    val stateMap = this

    if (showLoadingOnRefresh) {
        withContext(Dispatchers.Main.immediate) {
            val currentState = stateMap[server] ?: return@withContext
            val currentEndpointState = when (endpoint) {
                ActuatorEndpoints.BEANS -> currentState.beans
                ActuatorEndpoints.HEALTH -> currentState.health
                ActuatorEndpoints.CONFIG_PROPS -> currentState.configProps
                ActuatorEndpoints.METRICS -> currentState.metrics
            }
            if (currentEndpointState !is UIState.Success) {
                stateMap[server] = currentState.updateState(UIState.Loading)
            }
        }
    }

    try {
        val response = fetchData()

        withContext(Dispatchers.Main.immediate) {
            val currentState = stateMap[server] ?: return@withContext
            stateMap[server] = currentState.updateState(UIState.Success(response))
        }
    } catch (exception: Exception) {
        withContext(Dispatchers.Main.immediate) {
            val currentState = stateMap[server] ?: return@withContext
            stateMap[server] = currentState.updateState(UIState.Error(getFriendlyMessage(endpoint, exception)))
        }
    }
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshState(
    server: Server,
    repository: ActuatorRepository
) {
    coroutineScope {
        launch { refreshBeansState(server, repository) }
        launch { refreshHealthState(server, repository) }
        launch { refreshConfigPropsState(server, repository) }
        launch { refreshMetricsState(server, repository) }
    }
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshBeansState(
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.BEANS,
        fetchData = { repository.getBeans(server) },
        updateState = { copy(beans = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshHealthState(
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.HEALTH,
        fetchData = { repository.getHealth(server) },
        updateState = { copy(health = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshConfigPropsState(
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.CONFIG_PROPS,
        fetchData = { repository.getConfigProps(server) },
        updateState = { copy(configProps = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshMetricsState(
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.METRICS,
        fetchData = { repository.getMetrics(server) },
        updateState = { copy(metrics = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupState(
    group: ServerGroup,
    repository: ActuatorRepository
) {
    val groupState = this[group] ?: return

    coroutineScope {
        groupState.group.endpoints.forEach { server ->
            launch {
                refreshGroupEndpointState(group, server, repository)
            }
        }
    }
}

private suspend fun SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupEndpointState(
    group: ServerGroup,
    server: Server,
    repository: ActuatorRepository
) {
    coroutineScope {
        launch { refreshGroupBeansState(group, server, repository) }
        launch { refreshGroupHealthState(group, server, repository) }
        launch { refreshGroupConfigPropsState(group, server, repository) }
        launch { refreshGroupMetricsState(group, server, repository) }
    }
}

suspend fun SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupBeansState(
    group: ServerGroup,
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshGroupEndpoint(
        group = group,
        server = server,
        endpoint = ActuatorEndpoints.BEANS,
        fetchData = { repository.getBeans(server) },
        updateState = { copy(beans = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupHealthState(
    group: ServerGroup,
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshGroupEndpoint(
        group = group,
        server = server,
        endpoint = ActuatorEndpoints.HEALTH,
        fetchData = { repository.getHealth(server) },
        updateState = { copy(health = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupConfigPropsState(
    group: ServerGroup,
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshGroupEndpoint(
        group = group,
        server = server,
        endpoint = ActuatorEndpoints.CONFIG_PROPS,
        fetchData = { repository.getConfigProps(server) },
        updateState = { copy(configProps = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

suspend fun SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupMetricsState(
    group: ServerGroup,
    server: Server,
    repository: ActuatorRepository,
    showLoadingOnRefresh: Boolean = true
) {
    refreshGroupEndpoint(
        group = group,
        server = server,
        endpoint = ActuatorEndpoints.METRICS,
        fetchData = { repository.getMetrics(server) },
        updateState = { copy(metrics = it) },
        showLoadingOnRefresh = showLoadingOnRefresh
    )
}

private suspend fun <T> SnapshotStateMap<ServerGroup, ServerGroupState>.refreshGroupEndpoint(
    group: ServerGroup,
    server: Server,
    endpoint: ActuatorEndpoints,
    fetchData: suspend () -> T,
    updateState: ServerState.(UIState<T>) -> ServerState,
    showLoadingOnRefresh: Boolean = false
) {
    val stateMap = this

    if (showLoadingOnRefresh) {
        withContext(Dispatchers.Main.immediate) {
            val groupState = stateMap[group] ?: return@withContext
            val serverState = groupState.endpointStates[server] ?: return@withContext
            val currentEndpointState = when (endpoint) {
                ActuatorEndpoints.BEANS -> serverState.beans
                ActuatorEndpoints.HEALTH -> serverState.health
                ActuatorEndpoints.CONFIG_PROPS -> serverState.configProps
                ActuatorEndpoints.METRICS -> serverState.metrics
            }
            if (currentEndpointState !is UIState.Success) {
                val updatedEndpointStates = groupState.endpointStates.toMutableMap()
                updatedEndpointStates[server] = serverState.updateState(UIState.Loading)
                stateMap[group] = groupState.copy(endpointStates = updatedEndpointStates)
            }
        }
    }

    try {
        val response = fetchData()

        withContext(Dispatchers.Main.immediate) {
            val groupState = stateMap[group] ?: return@withContext
            val serverState = groupState.endpointStates[server] ?: return@withContext
            val updatedEndpointStates = groupState.endpointStates.toMutableMap()
            updatedEndpointStates[server] = serverState.updateState(UIState.Success(response))
            stateMap[group] = groupState.copy(endpointStates = updatedEndpointStates)
        }
    } catch (exception: Exception) {
        withContext(Dispatchers.Main.immediate) {
            val groupState = stateMap[group] ?: return@withContext
            val serverState = groupState.endpointStates[server] ?: return@withContext
            val updatedEndpointStates = groupState.endpointStates.toMutableMap()
            updatedEndpointStates[server] =
                serverState.updateState(UIState.Error(getFriendlyMessage(endpoint, exception)))
            stateMap[group] = groupState.copy(endpointStates = updatedEndpointStates)
        }
    }
}
