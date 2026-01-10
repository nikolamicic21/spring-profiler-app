package com.example.spring_profiler_app.data

private fun <T, R> aggregateEndpointData(
    endpointStates: Collection<ServerState>,
    extractData: (ServerState) -> UIState<T>,
    merge: (List<Pair<Server, T>>) -> R
): UIState<R> {
    val states = endpointStates.map { serverState ->
        serverState.server to extractData(serverState)
    }

    val successData = states.mapNotNull { (server, state) ->
        when (state) {
            is UIState.Success -> server to state.data
            else -> null
        }
    }

    val errors = states.filter { it.second is UIState.Error }
    val loading = states.filter { it.second is UIState.Loading }

    return when {
        loading.size == states.size -> UIState.Loading

        successData.isNotEmpty() -> {
            val merged = merge(successData)
            val warnings = buildList {
                if (errors.isNotEmpty()) {
                    errors.forEach { (server, state) ->
                        val errorMsg = (state as UIState.Error).message
                        add("${server.url} - $errorMsg")
                    }
                }
                if (loading.isNotEmpty()) {
                    loading.forEach { (server, _) ->
                        add("${server.url} - Still loading...")
                    }
                }
            }

            if (warnings.isNotEmpty()) {
                UIState.PartialSuccess(data = merged, warnings = warnings)
            } else {
                UIState.Success(merged)
            }
        }

        errors.size == states.size -> {
            val errorMessages = errors.joinToString("\n") { (server, state) ->
                "• ${server.url}: ${(state as UIState.Error).message}"
            }
            UIState.Error("All endpoints failed:\n$errorMessages")
        }

        else -> UIState.Loading
    }
}

fun ServerGroupState.getAggregatedBeans(): UIState<AggregatedBeansResponse> {
    return aggregateEndpointData(
        endpointStates = endpointStates.values,
        extractData = { it.beans },
        merge = { successfulResponses ->
            val endpoints = successfulResponses.map { (server, beansResponse) ->
                AggregatedBeansResponse.EndpointBeans(
                    endpoint = server.url.toString(),
                    contexts = beansResponse.contexts
                )
            }.sortedBy { it.endpoint }

            AggregatedBeansResponse(endpoints = endpoints)
        }
    )
}

fun ServerGroupState.getAggregatedHealth(): UIState<AggregatedHealthResponse> {
    return aggregateEndpointData(
        endpointStates = endpointStates.values,
        extractData = { it.health },
        merge = { successfulResponses ->
            val overallStatus = if (successfulResponses.any { it.second.status != "UP" }) {
                "DOWN"
            } else {
                "UP"
            }

            val endpoints = successfulResponses.map { (server, healthResponse) ->
                val components = healthResponse.components?.mapValues { it.value.status } ?: emptyMap()

                AggregatedHealthResponse.EndpointHealth(
                    endpoint = server.url.toString(),
                    status = healthResponse.status,
                    components = components
                )
            }.sortedBy { it.endpoint }

            AggregatedHealthResponse(
                status = overallStatus,
                endpoints = endpoints
            )
        }
    )
}

fun ServerGroupState.getAggregatedConfigProps(): UIState<AggregatedConfigPropsResponse> {
    return aggregateEndpointData(
        endpointStates = endpointStates.values,
        extractData = { it.configProps },
        merge = { successfulResponses ->
            val endpoints = successfulResponses.map { (server, configPropsResponse) ->
                AggregatedConfigPropsResponse.EndpointConfigProps(
                    endpoint = server.url.toString(),
                    contexts = configPropsResponse.contexts
                )
            }.sortedBy { it.endpoint }

            AggregatedConfigPropsResponse(endpoints = endpoints)
        }
    )
}

fun ServerGroupState.getAggregatedMetrics(): UIState<AggregatedMetricsResponse> {
    return aggregateEndpointData(
        endpointStates = endpointStates.values,
        extractData = { it.metrics },
        merge = { successfulResponses ->
            val endpoints = successfulResponses.map { (server, metricsResponse) ->
                AggregatedMetricsResponse.EndpointMetrics(
                    endpoint = server.url.toString(),
                    metrics = metricsResponse.metrics
                )
            }.sortedBy { it.endpoint }

            AggregatedMetricsResponse(endpoints = endpoints)
        }
    )
}
