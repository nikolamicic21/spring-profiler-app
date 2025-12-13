package com.example.spring_profiler_app.data

import androidx.compose.runtime.snapshots.SnapshotStateMap
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class StateTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var stateMap: SnapshotStateMap<Server, ServerState>
    private lateinit var testServer: Server
    private lateinit var mockRepository: ActuatorRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        testServer = Server(Url("http://localhost:8080/actuator"))

        stateMap = SnapshotStateMap()
        stateMap[testServer] = ServerState(
            server = testServer,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        mockRepository = mockk()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `refreshBeansState should update state to Success when repository call succeeds`() = runTest {
        // Given
        val beansResponse = BeansResponse(
            contexts = mapOf(
                "application" to Beans(
                    beans = mapOf(
                        "testBean" to Bean(dependencies = listOf("dep1"), scope = "singleton")
                    )
                )
            )
        )
        coEvery { mockRepository.getBeans(testServer) } returns beansResponse

        // When
        stateMap.refreshBeansState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<BeansResponse>>(updatedState?.beans)
        assertEquals(beansResponse, (updatedState?.beans as UIState.Success).data)
        coVerify(exactly = 1) { mockRepository.getBeans(testServer) }
    }

    @Test
    fun `refreshBeansState should update state to Error when repository call fails with ClientRequestException`() =
        runTest {
            // Given
            val exception = mockk<ClientRequestException>()
            val response = mockk<io.ktor.client.statement.HttpResponse>()
            every { exception.response } returns response
            every { response.status } returns HttpStatusCode.NotFound
            coEvery { mockRepository.getBeans(testServer) } throws exception

            // When
            stateMap.refreshBeansState(testServer, mockRepository)
            advanceUntilIdle()

            // Then
            val updatedState = stateMap[testServer]
            assertIs<UIState.Error>(updatedState?.beans)
            val errorMessage = (updatedState?.beans as UIState.Error).message
            assert(errorMessage.contains("404"))
            assert(errorMessage.contains("Beans"))
            coVerify(exactly = 1) { mockRepository.getBeans(testServer) }
        }

    @Test
    fun `refreshBeansState should update state to Error when repository call fails with IOException`() = runTest {
        // Given
        val exception = IOException("Network error")
        coEvery { mockRepository.getBeans(testServer) } throws exception

        // When
        stateMap.refreshBeansState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Error>(updatedState?.beans)
        val errorMessage = (updatedState?.beans as UIState.Error).message
        assert(errorMessage.contains("Connection Error"))
        coVerify(exactly = 1) { mockRepository.getBeans(testServer) }
    }

    @Test
    fun `refreshBeansState should not update state when server is not in map`() = runTest {
        // Given
        val nonExistentServer = Server(Url("http://nonexistent:9999/actuator"))
        val beansResponse = BeansResponse(contexts = emptyMap())
        coEvery { mockRepository.getBeans(nonExistentServer) } returns beansResponse

        // When
        stateMap.refreshBeansState(nonExistentServer, mockRepository)
        advanceUntilIdle()

        // Then
        assertEquals(null, stateMap[nonExistentServer])
        coVerify(exactly = 1) { mockRepository.getBeans(nonExistentServer) }
    }

    @Test
    fun `refreshHealthState should update state to Success when repository call succeeds`() = runTest {
        // Given
        val healthResponse = HealthResponse(
            status = "UP",
            components = mapOf("diskSpace" to HealthResponse.Component(status = "UP"))
        )
        coEvery { mockRepository.getHealth(testServer) } returns healthResponse

        // When
        stateMap.refreshHealthState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<HealthResponse>>(updatedState?.health)
        assertEquals(healthResponse, (updatedState?.health as UIState.Success).data)
        coVerify(exactly = 1) { mockRepository.getHealth(testServer) }
    }

    @Test
    fun `refreshHealthState should update state to Error when repository call fails`() = runTest {
        // Given
        val exception = IOException("Connection timeout")
        coEvery { mockRepository.getHealth(testServer) } throws exception

        // When
        stateMap.refreshHealthState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Error>(updatedState?.health)
        coVerify(exactly = 1) { mockRepository.getHealth(testServer) }
    }

    @Test
    fun `refreshConfigPropsState should update state to Success when repository call succeeds`() = runTest {
        // Given
        val configPropsResponse = ConfigPropsResponse(
            contexts = mapOf(
                "application" to Context(
                    beans = mapOf(
                        "spring.datasource" to BeanProperties(
                            prefix = "spring.datasource",
                            properties = kotlinx.serialization.json.buildJsonObject {
                                put("url", kotlinx.serialization.json.JsonPrimitive("jdbc:h2:mem:testdb"))
                            }
                        )
                    )
                )
            )
        )
        coEvery { mockRepository.getConfigProps(testServer) } returns configPropsResponse

        // When
        stateMap.refreshConfigPropsState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<ConfigPropsResponse>>(updatedState?.configProps)
        assertEquals(configPropsResponse, (updatedState?.configProps as UIState.Success).data)
        coVerify(exactly = 1) { mockRepository.getConfigProps(testServer) }
    }

    @Test
    fun `refreshConfigPropsState should update state to Error when repository call fails`() = runTest {
        // Given
        val exception = mockk<ServerResponseException>()
        val response = mockk<io.ktor.client.statement.HttpResponse>()
        every { exception.response } returns response
        every { response.status } returns HttpStatusCode.InternalServerError
        coEvery { mockRepository.getConfigProps(testServer) } throws exception

        // When
        stateMap.refreshConfigPropsState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Error>(updatedState?.configProps)
        val errorMessage = (updatedState?.configProps as UIState.Error).message
        assert(errorMessage.contains("500"))
        coVerify(exactly = 1) { mockRepository.getConfigProps(testServer) }
    }

    @Test
    fun `refreshMetricsState should update state to Success when repository call succeeds`() = runTest {
        // Given
        val metricsResponse = MetricsResponse(
            metrics = listOf(
                Metric(
                    name = "jvm.memory.used",
                    measurements = listOf(Measurement(statistic = "VALUE", value = 123456.0)),
                    unit = "bytes"
                )
            )
        )
        coEvery { mockRepository.getMetrics(testServer) } returns metricsResponse

        // When
        stateMap.refreshMetricsState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<MetricsResponse>>(updatedState?.metrics)
        assertEquals(metricsResponse, (updatedState?.metrics as UIState.Success).data)
        coVerify(exactly = 1) { mockRepository.getMetrics(testServer) }
    }

    @Test
    fun `refreshMetricsState should update state to Error when repository call fails`() = runTest {
        // Given
        val exception = RuntimeException("Unexpected error")
        coEvery { mockRepository.getMetrics(testServer) } throws exception

        // When
        stateMap.refreshMetricsState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Error>(updatedState?.metrics)
        val errorMessage = (updatedState?.metrics as UIState.Error).message
        assert(errorMessage.contains("unknown error"))
        coVerify(exactly = 1) { mockRepository.getMetrics(testServer) }
    }

    @Test
    fun `refreshState should refresh all four endpoints concurrently`() = runTest {
        // Given
        val beansResponse = BeansResponse(contexts = emptyMap())
        val healthResponse = HealthResponse(status = "UP")
        val configPropsResponse = ConfigPropsResponse(contexts = emptyMap())
        val metricsResponse = MetricsResponse(metrics = emptyList())

        coEvery { mockRepository.getBeans(testServer) } returns beansResponse
        coEvery { mockRepository.getHealth(testServer) } returns healthResponse
        coEvery { mockRepository.getConfigProps(testServer) } returns configPropsResponse
        coEvery { mockRepository.getMetrics(testServer) } returns metricsResponse

        // When
        stateMap.refreshState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<BeansResponse>>(updatedState?.beans)
        assertIs<UIState.Success<HealthResponse>>(updatedState?.health)
        assertIs<UIState.Success<ConfigPropsResponse>>(updatedState?.configProps)
        assertIs<UIState.Success<MetricsResponse>>(updatedState?.metrics)

        coVerify(exactly = 1) { mockRepository.getBeans(testServer) }
        coVerify(exactly = 1) { mockRepository.getHealth(testServer) }
        coVerify(exactly = 1) { mockRepository.getConfigProps(testServer) }
        coVerify(exactly = 1) { mockRepository.getMetrics(testServer) }
    }

    @Test
    fun `refreshState should handle partial failures gracefully`() = runTest {
        // Given
        val beansResponse = BeansResponse(contexts = emptyMap())
        val healthResponse = HealthResponse(status = "UP")

        coEvery { mockRepository.getBeans(testServer) } returns beansResponse
        coEvery { mockRepository.getHealth(testServer) } returns healthResponse
        coEvery { mockRepository.getConfigProps(testServer) } throws IOException("Network error")
        coEvery { mockRepository.getMetrics(testServer) } throws RuntimeException("Server error")

        // When
        stateMap.refreshState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<BeansResponse>>(updatedState?.beans)
        assertIs<UIState.Success<HealthResponse>>(updatedState?.health)
        assertIs<UIState.Error>(updatedState?.configProps)
        assertIs<UIState.Error>(updatedState?.metrics)

        coVerify(exactly = 1) { mockRepository.getBeans(testServer) }
        coVerify(exactly = 1) { mockRepository.getHealth(testServer) }
        coVerify(exactly = 1) { mockRepository.getConfigProps(testServer) }
        coVerify(exactly = 1) { mockRepository.getMetrics(testServer) }
    }

    @Test
    fun `refreshState should handle all endpoints failing`() = runTest {
        // Given
        coEvery { mockRepository.getBeans(testServer) } throws IOException("Network error")
        coEvery { mockRepository.getHealth(testServer) } throws IOException("Network error")
        coEvery { mockRepository.getConfigProps(testServer) } throws IOException("Network error")
        coEvery { mockRepository.getMetrics(testServer) } throws IOException("Network error")

        // When
        stateMap.refreshState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Error>(updatedState?.beans)
        assertIs<UIState.Error>(updatedState?.health)
        assertIs<UIState.Error>(updatedState?.configProps)
        assertIs<UIState.Error>(updatedState?.metrics)

        coVerify(exactly = 1) { mockRepository.getBeans(testServer) }
        coVerify(exactly = 1) { mockRepository.getHealth(testServer) }
        coVerify(exactly = 1) { mockRepository.getConfigProps(testServer) }
        coVerify(exactly = 1) { mockRepository.getMetrics(testServer) }
    }

    @Test
    fun `refresh functions should preserve other endpoint states when updating one`() = runTest {
        // Given
        val initialBeansResponse = BeansResponse(contexts = emptyMap())
        stateMap[testServer] = stateMap[testServer]!!.copy(
            beans = UIState.Success(initialBeansResponse)
        )

        val healthResponse = HealthResponse(status = "UP")
        coEvery { mockRepository.getHealth(testServer) } returns healthResponse

        // When
        stateMap.refreshHealthState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<BeansResponse>>(updatedState?.beans)
        assertEquals(initialBeansResponse, (updatedState?.beans as UIState.Success).data)
        assertIs<UIState.Success<HealthResponse>>(updatedState?.health)
    }

    @Test
    fun `refresh functions should handle empty responses correctly`() = runTest {
        // Given
        val emptyBeansResponse = BeansResponse(contexts = emptyMap())
        val emptyHealthResponse = HealthResponse(status = "UP", components = null)
        val emptyConfigPropsResponse = ConfigPropsResponse(contexts = emptyMap())
        val emptyMetricsResponse = MetricsResponse(metrics = emptyList())

        coEvery { mockRepository.getBeans(testServer) } returns emptyBeansResponse
        coEvery { mockRepository.getHealth(testServer) } returns emptyHealthResponse
        coEvery { mockRepository.getConfigProps(testServer) } returns emptyConfigPropsResponse
        coEvery { mockRepository.getMetrics(testServer) } returns emptyMetricsResponse

        // When
        stateMap.refreshState(testServer, mockRepository)
        advanceUntilIdle()

        // Then
        val updatedState = stateMap[testServer]
        assertIs<UIState.Success<BeansResponse>>(updatedState?.beans)
        assertIs<UIState.Success<HealthResponse>>(updatedState?.health)
        assertIs<UIState.Success<ConfigPropsResponse>>(updatedState?.configProps)
        assertIs<UIState.Success<MetricsResponse>>(updatedState?.metrics)
    }
}
