package com.example.spring_profiler_app.data

import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.spring_profiler_app.repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ApiUiState<out T> {
    data object Loading : ApiUiState<Nothing>()
    data class Error(val message: String) : ApiUiState<Nothing>()
    data class Success<T>(val data: T) : ApiUiState<T>()
}

data class ServerState(
    val server: Server,
    val beans: ApiUiState<BeansResponse>,
    val health: ApiUiState<HealthResponse>,
    val configProps: ApiUiState<ConfigPropsResponse>,
    val metrics: ApiUiState<MetricsResponse>,
)

private suspend fun <T> SnapshotStateMap<Server, ServerState>.refreshEndpoint(
    server: Server,
    endpoint: ActuatorEndpoints,
    fetchData: suspend () -> T,
    updateState: ServerState.(ApiUiState<T>) -> ServerState
) {
    val stateMap = this
    try {
        val response = withContext(Dispatchers.IO) {
            fetchData()
        }
        withContext(Dispatchers.Main.immediate) {
            val currentState = stateMap[server] ?: return@withContext
            stateMap[server] = currentState.updateState(ApiUiState.Success(response))
        }
    } catch (exception: Exception) {
        withContext(Dispatchers.Main.immediate) {
            val currentState = stateMap[server] ?: return@withContext
            stateMap[server] = currentState.updateState(ApiUiState.Error(getFriendlyMessage(endpoint, exception)))
        }
    }
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshState(
    server: Server
) {
    coroutineScope {
        launch { refreshBeansState(server) }
        launch { refreshHealthState(server) }
        launch { refreshConfigPropsState(server) }
        launch { refreshMetricsState(server) }
    }
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshBeansState(
    server: Server
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.BEANS,
        fetchData = { repo.getBeans(server) },
        updateState = { copy(beans = it) }
    )
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshHealthState(
    server: Server
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.HEALTH,
        fetchData = { repo.getHealth(server) },
        updateState = { copy(health = it) }
    )
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshConfigPropsState(
    server: Server
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.CONFIG_PROPS,
        fetchData = { repo.getConfigProps(server) },
        updateState = { copy(configProps = it) }
    )
}

suspend fun SnapshotStateMap<Server, ServerState>.refreshMetricsState(
    server: Server
) {
    refreshEndpoint(
        server = server,
        endpoint = ActuatorEndpoints.METRICS,
        fetchData = { repo.getMetrics(server) },
        updateState = { copy(metrics = it) }
    )
}
