package com.example.spring_profiler_app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.spring_profiler_app.repo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

class ThreadSafeState<T>(initialValue: T) {
    private var _state by mutableStateOf(initialValue)
    val state: T get() = _state

    private val mutex = Mutex()

    suspend fun modify(block: (T) -> T) {
        mutex.withLock {
            _state = block(_state)
        }
    }
}

val RecoveryScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

fun getCoroutineExceptionHandler(
    server: Server,
    state: SnapshotStateMap<Server, ThreadSafeState<ServerState>>,
    actuatorEndpoints: ActuatorEndpoints
) =
    CoroutineExceptionHandler { _, exception ->
        RecoveryScope.launch {
            state[server]?.modify { state ->
                when (actuatorEndpoints) {
                    ActuatorEndpoints.BEANS -> {
                        state.copy(
                            beans = ApiUiState.Error(
                                getFriendlyMessage(
                                    actuatorEndpoints, exception
                                )
                            )
                        )
                    }

                    ActuatorEndpoints.HEALTH -> {
                        state.copy(
                            health = ApiUiState.Error(
                                getFriendlyMessage(
                                    actuatorEndpoints, exception
                                )
                            )
                        )
                    }

                    ActuatorEndpoints.CONFIG_PROPS -> {
                        state.copy(
                            configProps = ApiUiState.Error(
                                getFriendlyMessage(
                                    actuatorEndpoints, exception
                                )
                            )
                        )
                    }

                    ActuatorEndpoints.METRICS -> {
                        state.copy(
                            metrics = ApiUiState.Error(
                                getFriendlyMessage(
                                    actuatorEndpoints, exception
                                )
                            )
                        )
                    }
                }
            }
        }
    }

fun SnapshotStateMap<Server, ThreadSafeState<ServerState>>.refreshState(server: Server) {
    val state = this
    state.refreshBeansState(server)
    state.refreshHealthState(server)
    state.refreshConfigPropsState(server)
    state.refreshMetricsState(server)
}

fun SnapshotStateMap<Server, ThreadSafeState<ServerState>>.refreshBeansState(server: Server) {
    val state = this
    RecoveryScope.launch(getCoroutineExceptionHandler(server, state, ActuatorEndpoints.BEANS)) {
        val beansResponse = repo.getBeans(server)
        state[server]?.modify {
            it.copy(
                beans = ApiUiState.Success(beansResponse)
            )
        }
    }
}

fun SnapshotStateMap<Server, ThreadSafeState<ServerState>>.refreshHealthState(server: Server) {
    val state = this
    RecoveryScope.launch(getCoroutineExceptionHandler(server, state, ActuatorEndpoints.HEALTH)) {
        val healthResponse = repo.getHealth(server)
        state[server]?.modify {
            it.copy(
                health = ApiUiState.Success(healthResponse)
            )
        }
    }
}

fun SnapshotStateMap<Server, ThreadSafeState<ServerState>>.refreshConfigPropsState(server: Server) {
    val state = this
    RecoveryScope.launch(getCoroutineExceptionHandler(server, state, ActuatorEndpoints.CONFIG_PROPS)) {
        val configPropsResponse = repo.getConfigProps(server)
        state[server]?.modify {
            it.copy(
                configProps = ApiUiState.Success(configPropsResponse)
            )
        }
    }
}

fun SnapshotStateMap<Server, ThreadSafeState<ServerState>>.refreshMetricsState(server: Server) {
    val state = this
    RecoveryScope.launch(getCoroutineExceptionHandler(server, state, ActuatorEndpoints.METRICS)) {
        val metricsResponse = repo.getMetrics(server)
        state[server]?.modify {
            it.copy(
                metrics = ApiUiState.Success(metricsResponse)
            )
        }
    }
}
