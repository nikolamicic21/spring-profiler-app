package com.example.spring_profiler_app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.ActuatorRepository
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.data.ConfigPropsResponse
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.MetricsResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class AppTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: ActuatorRepository
    private lateinit var mockClient: HttpClient

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        mockClient = createMockClient()

        coEvery { mockRepository.getBeans(any()) } returns BeansResponse(emptyMap())
        coEvery { mockRepository.getHealth(any()) } returns HealthResponse("UP", emptyMap())
        coEvery { mockRepository.getConfigProps(any()) } returns ConfigPropsResponse(emptyMap())
        coEvery { mockRepository.getMetrics(any()) } returns MetricsResponse(emptyList())
    }

    @Test
    fun `App should display Servers title`() = runComposeUiTest {
        // When
        setContent {
            CompositionLocalProvider(
                Client provides mockClient,
                Repository provides mockRepository
            ) {
                App()
            }
        }

        // Then
        onNodeWithText("Servers").assertIsDisplayed()
    }

    @Test
    fun `App should display Add a new server button`() = runComposeUiTest {
        // When
        setContent {
            CompositionLocalProvider(
                Client provides mockClient,
                Repository provides mockRepository
            ) {
                App()
            }
        }

        // Then
        onNodeWithText("Add a new server").assertIsDisplayed()
    }

    @Test
    fun `App should display AddServerForm when no servers are added`() = runComposeUiTest {
        // When
        setContent {
            CompositionLocalProvider(
                Client provides mockClient,
                Repository provides mockRepository
            ) {
                App()
            }
        }

        // Then
        onNodeWithText("Server's actuator endpoint (URL)").assertIsDisplayed()
        onNodeWithText("Connect").assertIsDisplayed()
    }

    @Test
    fun `App should display AddServerForm when no server is selected`() = runComposeUiTest {
        // When
        setContent {
            CompositionLocalProvider(
                Client provides mockClient,
                Repository provides mockRepository
            ) {
                App()
            }
        }

        // Then
        onNodeWithText("Server's actuator endpoint (URL)").assertIsDisplayed()
    }

    private fun createMockClient(): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
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
