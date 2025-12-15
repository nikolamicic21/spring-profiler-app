package com.example.spring_profiler_app.ui.panels

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.Client
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class AddServerFormTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `AddServerForm should display URL input field`() = runComposeUiTest {
        // Given
        val servers = mutableMapOf<Server, ServerState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Server's actuator endpoint (URL)").assertIsDisplayed()
    }

    @Test
    fun `AddServerForm should display Connect button`() = runComposeUiTest {
        // Given
        val servers = mutableMapOf<Server, ServerState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Connect").assertIsDisplayed()
    }

    @Test
    fun `AddServerForm should allow text input in URL field`() = runComposeUiTest {
        // Given
        val servers = mutableMapOf<Server, ServerState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Server's actuator endpoint (URL)").performClick()
        onNodeWithText("Server's actuator endpoint (URL)").performTextInput("http://localhost:8080/actuator")
        onNodeWithText("http://localhost:8080/actuator").assertIsDisplayed()
    }

    @Test
    fun `AddServerForm should not display error message initially`() = runComposeUiTest {
        // Given
        val servers = mutableMapOf<Server, ServerState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("There's been an error connecting to the server's actuator endpoint. Please check the URL!").assertDoesNotExist()
        onNodeWithText("Server already exists").assertDoesNotExist()
    }

    @Test
    fun `AddServerForm should show error when server connection fails`() = runComposeUiTest {
        // Given
        val servers = mutableMapOf<Server, ServerState>()
        val mockClient = createMockClient(HttpStatusCode.InternalServerError)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Server's actuator endpoint (URL)").performClick()
        onNodeWithText("Server's actuator endpoint (URL)").performTextInput("http://localhost:8080/actuator")
        onNodeWithText("Connect").performClick()

        // Advance the test dispatcher to execute pending coroutines
        testDispatcher.scheduler.runCurrent()
        waitForIdle()

        // Then
        onNodeWithText("There's been an error connecting to the server's actuator endpoint. Please check the URL!").assertIsDisplayed()
    }

    @Test
    fun `AddServerForm should show error when server already exists`() = runComposeUiTest {
        // Given
        val existingServer = Server(io.ktor.http.Url("http://localhost:8080/actuator"))
        val servers = mutableMapOf(
            existingServer to ServerState(
                existingServer,
                com.example.spring_profiler_app.data.UIState.Loading,
                com.example.spring_profiler_app.data.UIState.Loading,
                com.example.spring_profiler_app.data.UIState.Loading,
                com.example.spring_profiler_app.data.UIState.Loading
            )
        )
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Server's actuator endpoint (URL)").performClick()
        onNodeWithText("Server's actuator endpoint (URL)").performTextInput("http://localhost:8080/actuator")
        onNodeWithText("Connect").performClick()

        testDispatcher.scheduler.runCurrent()
        waitForIdle()

        // Then
        onNodeWithText("Server already exists").assertIsDisplayed()
    }

    @Test
    fun `AddServerForm should add server successfully when connection succeeds`() = runComposeUiTest {
        // Given
        val servers = mutableMapOf<Server, ServerState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())
        var addedServer: Server? = null

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerForm(
                    servers = servers,
                    onServerAdded = { addedServer = it },
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Server's actuator endpoint (URL)").performClick()
        onNodeWithText("Server's actuator endpoint (URL)").performTextInput("http://localhost:9090/actuator")
        onNodeWithText("Connect").performClick()
        
        testDispatcher.scheduler.runCurrent()
        waitForIdle()

        // Then
        assert(servers.isNotEmpty()) { "Server should be added to the map" }
        assert(addedServer != null) { "onServerAdded callback should be called" }
        assert(addedServer?.url?.host == "localhost") { "Server host should be localhost" }
        assert(addedServer?.url?.port == 9090) { "Server port should be 9090" }
    }

    private fun createMockClient(statusCode: HttpStatusCode): HttpClient {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = statusCode,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
            expectSuccess = false
        }
    }
}
