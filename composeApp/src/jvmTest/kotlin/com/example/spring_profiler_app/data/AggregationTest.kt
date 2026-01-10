package com.example.spring_profiler_app.data

import io.ktor.http.Url
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AggregationTest {

    private val server1 = Server(Url("http://localhost:8080/actuator"))
    private val server2 = Server(Url("http://localhost:8081/actuator"))
    private val server3 = Server(Url("http://localhost:8082/actuator"))

    @Test
    fun `getAggregatedBeans should return Success when all endpoints succeed`() {
        // Given
        val beans1 = BeansResponse(
            contexts = mapOf(
                "application" to Beans(
                    beans = mapOf("bean1" to Bean(dependencies = emptyList(), scope = "singleton"))
                )
            )
        )
        val beans2 = BeansResponse(
            contexts = mapOf(
                "application" to Beans(
                    beans = mapOf("bean2" to Bean(dependencies = emptyList(), scope = "prototype"))
                )
            )
        )

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Success(beans1),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Success(beans2),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedBeans()

        // Then
        assertIs<UIState.Success<AggregatedBeansResponse>>(result)
        val data = result.data
        assertEquals(2, data.endpoints.size)
        assertEquals(server1.url.toString(), data.endpoints[0].endpoint)
        assertEquals(server2.url.toString(), data.endpoints[1].endpoint)
        assertEquals(beans1.contexts, data.endpoints[0].contexts)
        assertEquals(beans2.contexts, data.endpoints[1].contexts)
    }

    @Test
    fun `getAggregatedBeans should return PartialSuccess when some endpoints fail`() {
        // Given
        val beans1 = BeansResponse(
            contexts = mapOf(
                "application" to Beans(
                    beans = mapOf("bean1" to Bean(dependencies = emptyList(), scope = "singleton"))
                )
            )
        )

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Success(beans1),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Error("Connection failed"),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedBeans()

        // Then
        assertIs<UIState.PartialSuccess<AggregatedBeansResponse>>(result)
        val data = result.data
        assertEquals(1, data.endpoints.size)
        assertEquals(server1.url.toString(), data.endpoints[0].endpoint)

        assertEquals(1, result.warnings.size)
        assertTrue(result.warnings[0].contains("localhost:8081"))
        assertTrue(result.warnings[0].contains("Connection failed"))
    }

    @Test
    fun `getAggregatedBeans should return PartialSuccess when some endpoints are still loading`() {
        // Given
        val beans1 = BeansResponse(
            contexts = mapOf(
                "application" to Beans(
                    beans = mapOf("bean1" to Bean(dependencies = emptyList(), scope = "singleton"))
                )
            )
        )

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Success(beans1),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedBeans()

        // Then
        assertIs<UIState.PartialSuccess<AggregatedBeansResponse>>(result)
        val data = result.data
        assertEquals(1, data.endpoints.size)

        assertEquals(1, result.warnings.size)
        assertTrue(result.warnings[0].contains("localhost:8081"))
        assertTrue(result.warnings[0].contains("Still loading"))
    }

    @Test
    fun `getAggregatedBeans should return Error when all endpoints fail`() {
        // Given
        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Error("Connection timeout"),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Error("404 Not Found"),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedBeans()

        // Then
        assertIs<UIState.Error>(result)
        assertTrue(result.message.contains("All endpoints failed"))
        assertTrue(result.message.contains("localhost:8080"))
        assertTrue(result.message.contains("Connection timeout"))
        assertTrue(result.message.contains("localhost:8081"))
        assertTrue(result.message.contains("404 Not Found"))
    }

    @Test
    fun `getAggregatedBeans should return Loading when all endpoints are loading`() {
        // Given
        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedBeans()

        // Then
        assertIs<UIState.Loading>(result)
    }

    @Test
    fun `getAggregatedHealth should calculate overall status as DOWN when any endpoint is DOWN`() {
        // Given
        val health1 = HealthResponse(status = "UP", components = emptyMap())
        val health2 = HealthResponse(status = "DOWN", components = emptyMap())

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Loading,
            health = UIState.Success(health1),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Loading,
            health = UIState.Success(health2),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedHealth()

        // Then
        assertIs<UIState.Success<AggregatedHealthResponse>>(result)
        assertEquals("DOWN", result.data.status)
        assertEquals(2, result.data.endpoints.size)
    }

    @Test
    fun `getAggregatedHealth should calculate overall status as UP when all endpoints are UP`() {
        // Given
        val health1 = HealthResponse(status = "UP", components = emptyMap())
        val health2 = HealthResponse(status = "UP", components = emptyMap())

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Loading,
            health = UIState.Success(health1),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Loading,
            health = UIState.Success(health2),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedHealth()

        // Then
        assertIs<UIState.Success<AggregatedHealthResponse>>(result)
        assertEquals("UP", result.data.status)
        assertEquals(2, result.data.endpoints.size)
    }

    @Test
    fun `getAggregatedHealth should extract component statuses correctly`() {
        // Given
        val health1 = HealthResponse(
            status = "UP",
            components = mapOf(
                "db" to HealthResponse.Component(status = "UP"),
                "diskSpace" to HealthResponse.Component(status = "UP")
            )
        )

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Loading,
            health = UIState.Success(health1),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1)
        )

        // When
        val result = groupState.getAggregatedHealth()

        // Then
        assertIs<UIState.Success<AggregatedHealthResponse>>(result)
        val endpoint = result.data.endpoints[0]
        assertEquals(2, endpoint.components.size)
        assertEquals("UP", endpoint.components["db"])
        assertEquals("UP", endpoint.components["diskSpace"])
    }

    @Test
    fun `getAggregatedConfigProps should aggregate config properties from multiple endpoints`() {
        // Given
        val configProps1 = ConfigPropsResponse(
            contexts = mapOf(
                "application" to Context(
                    beans = mapOf(
                        "dataSource" to BeanProperties(
                            prefix = "spring.datasource",
                            properties = JsonObject(mapOf("url" to JsonPrimitive("jdbc:h2:mem:test")))
                        )
                    )
                )
            )
        )
        val configProps2 = ConfigPropsResponse(
            contexts = mapOf(
                "application" to Context(
                    beans = mapOf(
                        "server" to BeanProperties(
                            prefix = "server",
                            properties = JsonObject(mapOf("port" to JsonPrimitive(8080)))
                        )
                    )
                )
            )
        )

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Success(configProps1),
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Success(configProps2),
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedConfigProps()

        // Then
        assertIs<UIState.Success<AggregatedConfigPropsResponse>>(result)
        val data = result.data
        assertEquals(2, data.endpoints.size)
        assertEquals(server1.url.toString(), data.endpoints[0].endpoint)
        assertEquals(server2.url.toString(), data.endpoints[1].endpoint)
        assertEquals(configProps1.contexts, data.endpoints[0].contexts)
        assertEquals(configProps2.contexts, data.endpoints[1].contexts)
    }

    @Test
    fun `getAggregatedMetrics should aggregate metrics from multiple endpoints`() {
        // Given
        val metrics1 = MetricsResponse(
            metrics = listOf(
                Metric(
                    name = "jvm.memory.used",
                    measurements = listOf(Measurement(statistic = "VALUE", value = 1024.0)),
                    unit = "bytes"
                )
            )
        )
        val metrics2 = MetricsResponse(
            metrics = listOf(
                Metric(
                    name = "http.server.requests",
                    measurements = listOf(Measurement(statistic = "COUNT", value = 100.0)),
                    unit = null
                )
            )
        )

        val serverState1 = ServerState(
            server = server1,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Success(metrics1)
        )
        val serverState2 = ServerState(
            server = server2,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Success(metrics2)
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        val result = groupState.getAggregatedMetrics()

        // Then
        assertIs<UIState.Success<AggregatedMetricsResponse>>(result)
        val data = result.data
        assertEquals(2, data.endpoints.size)
        assertEquals(server1.url.toString(), data.endpoints[0].endpoint)
        assertEquals(server2.url.toString(), data.endpoints[1].endpoint)
        assertEquals(metrics1.metrics, data.endpoints[0].metrics)
        assertEquals(metrics2.metrics, data.endpoints[1].metrics)
    }

    @Test
    fun `aggregation should sort endpoints by name`() {
        // Given
        val beans1 = BeansResponse(contexts = emptyMap())
        val beans2 = BeansResponse(contexts = emptyMap())
        val beans3 = BeansResponse(contexts = emptyMap())

        val serverState1 = ServerState(
            server = server3,
            beans = UIState.Success(beans1),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState2 = ServerState(
            server = server1,
            beans = UIState.Success(beans2),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val serverState3 = ServerState(
            server = server2,
            beans = UIState.Success(beans3),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        val group = ServerGroup(name = "test-group", endpoints = listOf(server3, server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server3 to serverState1, server1 to serverState2, server2 to serverState3)
        )

        // When
        val result = groupState.getAggregatedBeans()

        // Then
        assertIs<UIState.Success<AggregatedBeansResponse>>(result)
        val endpoints = result.data.endpoints
        assertEquals(server1.url.toString(), endpoints[0].endpoint)
        assertEquals(server2.url.toString(), endpoints[1].endpoint)
        assertEquals(server3.url.toString(), endpoints[2].endpoint)
    }
}
