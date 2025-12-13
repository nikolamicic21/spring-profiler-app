package com.example.spring_profiler_app.data

import com.example.spring_profiler_app.client.safeRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.url
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface ActuatorRepository {
    suspend fun getBeans(server: Server): BeansResponse
    suspend fun getHealth(server: Server): HealthResponse
    suspend fun getConfigProps(server: Server): ConfigPropsResponse
    suspend fun getMetrics(server: Server): MetricsResponse
}

class ActuatorRepositoryImpl(val client: HttpClient) : ActuatorRepository {

    override suspend fun getBeans(
        server: Server
    ): BeansResponse =
        client.safeRequest { url("${server.url}/beans") }

    override suspend fun getHealth(server: Server): HealthResponse =
        client.safeRequest { url("${server.url}/health") }

    override suspend fun getConfigProps(server: Server): ConfigPropsResponse =
        client.safeRequest { url("${server.url}/configprops") }

    override suspend fun getMetrics(server: Server): MetricsResponse = coroutineScope {
        val metricNames = client.safeRequest<MetricNamesResult> { url("${server.url}/metrics") }
        val metrics = metricNames.names.map {
            async {
                client.safeRequest<MetricsResult> { url("${server.url}/metrics/${it}") }
            }
        }.awaitAll()

        return@coroutineScope MetricsResponse(
            metrics = metrics.map { metric ->
                Metric(
                    name = metric.name,
                    unit = metric.baseUnit,
                    measurements = metric.measurements.map { metric ->
                        Measurement(
                            statistic = metric.statistic,
                            value = metric.value,
                        )
                    }
                )
            }
        )
    }
}