package com.example.spring_profiler_app.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ActuatorRepositoryTest {

    private lateinit var repository: ActuatorRepository
    private lateinit var mockEngine: MockEngine
    private lateinit var testServer: Server

    @BeforeTest
    fun setup() {
        testServer = Server(Url("http://localhost:8080/actuator"))
    }

    @Test
    fun `getBeans should return BeansResponse when request succeeds`() = runTest {
        // Given
        val responseJson = """
        {
            "contexts": {
                "application": {
                    "beans": {
                        "testBean": {
                            "scope": "singleton",
                            "dependencies": ["dep1", "dep2"]
                        }
                    }
                }
            }
        }
        """.trimIndent()

        mockEngine = MockEngine { request ->
            assertEquals("http://localhost:8080/actuator/beans", request.url.toString())
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getBeans(testServer)

        // Then
        assertNotNull(result)
        assertEquals(1, result.contexts.size)
        assertTrue(result.contexts.containsKey("application"))
        assertEquals(1, result.contexts["application"]?.beans?.size)
    }

    @Test
    fun `getBeans should throw ClientRequestException when server returns 404`() = runTest {
        // Given
        mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Not Found"),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When/Then
        assertFailsWith<io.ktor.client.plugins.ClientRequestException> {
            repository.getBeans(testServer)
        }
    }

    @Test
    fun `getBeans should throw ServerResponseException when server returns 500`() = runTest {
        // Given
        mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel("Internal Server Error"),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When/Then
        assertFailsWith<io.ktor.client.plugins.ServerResponseException> {
            repository.getBeans(testServer)
        }
    }

    @Test
    fun `getHealth should return HealthResponse when request succeeds`() = runTest {
        // Given
        val responseJson = """
        {
            "status": "UP",
            "components": {
                "diskSpace": {
                    "status": "UP"
                }
            }
        }
        """.trimIndent()

        mockEngine = MockEngine { request ->
            assertEquals("http://localhost:8080/actuator/health", request.url.toString())
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getHealth(testServer)

        // Then
        assertNotNull(result)
        assertEquals("UP", result.status)
        assertNotNull(result.components)
        assertEquals(1, result.components?.size)
    }

    @Test
    fun `getHealth should handle response without components`() = runTest {
        // Given
        val responseJson = """
        {
            "status": "DOWN"
        }
        """.trimIndent()

        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getHealth(testServer)

        // Then
        assertNotNull(result)
        assertEquals("DOWN", result.status)
        assertNull(result.components)
    }

    @Test
    fun `getConfigProps should return ConfigPropsResponse when request succeeds`() = runTest {
        // Given
        val responseJson = """
        {
            "contexts": {
                "application": {
                    "beans": {
                        "spring.datasource-com.zaxxer.hikari.HikariDataSource": {
                            "prefix": "spring.datasource",
                            "properties": {
                                "url": "jdbc:h2:mem:testdb"
                            }
                        }
                    }
                }
            }
        }
        """.trimIndent()

        mockEngine = MockEngine { request ->
            assertEquals("http://localhost:8080/actuator/configprops", request.url.toString())
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getConfigProps(testServer)

        // Then
        assertNotNull(result)
        assertEquals(1, result.contexts.size)
        assertTrue(result.contexts.containsKey("application"))
    }

    @Test
    fun `getConfigProps should handle empty contexts`() = runTest {
        // Given
        val responseJson = """
        {
            "contexts": {}
        }
        """.trimIndent()

        mockEngine = MockEngine { request ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getConfigProps(testServer)

        // Then
        assertNotNull(result)
        assertEquals(0, result.contexts.size)
    }

    @Test
    fun `getMetrics should fetch metric names and individual metrics concurrently`() = runTest {
        // Given
        var requestCount = 0
        mockEngine = MockEngine { request ->
            requestCount++
            when {
                request.url.toString() == "http://localhost:8080/actuator/metrics" -> {
                    respond(
                        content = ByteReadChannel("""{"names": ["jvm.memory.used", "system.cpu.usage"]}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                request.url.toString().contains("/metrics/jvm.memory.used") -> {
                    respond(
                        content = ByteReadChannel(
                            """
                        {
                            "name": "jvm.memory.used",
                            "baseUnit": "bytes",
                            "measurements": [
                                {"statistic": "VALUE", "value": 123456.0}
                            ]
                        }
                        """.trimIndent()
                        ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                request.url.toString().contains("/metrics/system.cpu.usage") -> {
                    respond(
                        content = ByteReadChannel(
                            """
                        {
                            "name": "system.cpu.usage",
                            "baseUnit": "percent",
                            "measurements": [
                                {"statistic": "VALUE", "value": 0.45}
                            ]
                        }
                        """.trimIndent()
                        ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                else -> error("Unexpected request: ${request.url}")
            }
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getMetrics(testServer)

        // Then
        assertNotNull(result)
        assertEquals(2, result.metrics.size)
        assertEquals(3, requestCount)

        val memoryMetric = result.metrics.find { it.name == "jvm.memory.used" }
        assertNotNull(memoryMetric)
        assertEquals("bytes", memoryMetric.unit)
        assertEquals(1, memoryMetric.measurements.size)
        assertEquals("VALUE", memoryMetric.measurements[0].statistic)
        assertEquals(123456.0, memoryMetric.measurements[0].value)
    }

    @Test
    fun `getMetrics should handle empty metric names list`() = runTest {
        // Given
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""{"names": []}"""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getMetrics(testServer)

        // Then
        assertNotNull(result)
        assertEquals(0, result.metrics.size)
    }

    @Test
    fun `getMetrics should handle metrics with null baseUnit`() = runTest {
        // Given
        mockEngine = MockEngine { request ->
            when {
                request.url.toString() == "http://localhost:8080/actuator/metrics" -> {
                    respond(
                        content = ByteReadChannel("""{"names": ["custom.metric"]}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                request.url.toString().contains("/metrics/custom.metric") -> {
                    respond(
                        content = ByteReadChannel(
                            """
                        {
                            "name": "custom.metric",
                            "measurements": [
                                {"statistic": "COUNT", "value": 42.0}
                            ]
                        }
                        """.trimIndent()
                        ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                else -> error("Unexpected request: ${request.url}")
            }
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getMetrics(testServer)

        // Then
        assertNotNull(result)
        assertEquals(1, result.metrics.size)
        assertNull(result.metrics[0].unit)
    }

    @Test
    fun `getMetrics should handle multiple measurements per metric`() = runTest {
        // Given
        mockEngine = MockEngine { request ->
            when {
                request.url.toString() == "http://localhost:8080/actuator/metrics" -> {
                    respond(
                        content = ByteReadChannel("""{"names": ["http.server.requests"]}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                request.url.toString().contains("/metrics/http.server.requests") -> {
                    respond(
                        content = ByteReadChannel(
                            """
                        {
                            "name": "http.server.requests",
                            "baseUnit": "seconds",
                            "measurements": [
                                {"statistic": "COUNT", "value": 100.0},
                                {"statistic": "TOTAL_TIME", "value": 5.5},
                                {"statistic": "MAX", "value": 0.5}
                            ]
                        }
                        """.trimIndent()
                        ),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                else -> error("Unexpected request: ${request.url}")
            }
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        val result = repository.getMetrics(testServer)

        // Then
        assertNotNull(result)
        assertEquals(1, result.metrics.size)
        assertEquals(3, result.metrics[0].measurements.size)
    }

    @Test
    fun `getMetrics should throw exception when metric names request fails`() = runTest {
        // Given
        mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Service Unavailable"),
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When/Then
        assertFailsWith<io.ktor.client.plugins.ServerResponseException> {
            repository.getMetrics(testServer)
        }
    }

    @Test
    fun `all methods should construct correct URLs with server base URL`() = runTest {
        // Given
        val capturedUrls = mutableListOf<String>()
        mockEngine = MockEngine { request ->
            capturedUrls.add(request.url.toString())
            when {
                request.url.toString().endsWith("/beans") -> {
                    respond(
                        content = ByteReadChannel("""{"contexts": {}}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                request.url.toString().endsWith("/health") -> {
                    respond(
                        content = ByteReadChannel("""{"status": "UP"}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                request.url.toString().endsWith("/configprops") -> {
                    respond(
                        content = ByteReadChannel("""{"contexts": {}}"""),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }

                else -> error("Unexpected request: ${request.url}")
            }
        }

        val client = createMockClient(mockEngine)
        repository = ActuatorRepositoryImpl(client)

        // When
        repository.getBeans(testServer)
        repository.getHealth(testServer)
        repository.getConfigProps(testServer)

        // Then
        assertEquals("http://localhost:8080/actuator/beans", capturedUrls[0])
        assertEquals("http://localhost:8080/actuator/health", capturedUrls[1])
        assertEquals("http://localhost:8080/actuator/configprops", capturedUrls[2])
    }

    private fun createMockClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            expectSuccess = false
        }
    }
}
