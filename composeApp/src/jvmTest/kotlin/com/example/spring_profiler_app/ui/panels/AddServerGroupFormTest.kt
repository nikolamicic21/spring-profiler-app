package com.example.spring_profiler_app.ui.panels

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.Client
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class AddServerGroupFormTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `AddServerGroupForm should display Group Name input field`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Group Name").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should display Connect Group button`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Connect Group").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should display Add Server Group title`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Add Server Group").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should display default endpoint field`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Endpoint 1").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should allow text input in Group Name field`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextInput("My Production Servers")
        onNodeWithText("My Production Servers", substring = true).assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should display Add Another Endpoint button`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Add Another Endpoint").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should add new endpoint field when Add Another Endpoint is clicked`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Add Another Endpoint").performClick()
        waitForIdle()
        onNodeWithText("Endpoint 2").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should show delete button when multiple endpoints exist`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Add Another Endpoint").performClick()
        waitForIdle()

        // Then
        onAllNodesWithContentDescription("Remove endpoint").assertCountEquals(2)
    }

    @Test
    fun `AddServerGroupForm should not show delete button with single endpoint`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onAllNodesWithContentDescription("Remove endpoint").assertCountEquals(0)
    }

    @Test
    fun `AddServerGroupForm should allow text input in endpoint fields`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Endpoint 1").performClick()
        onNodeWithText("Endpoint 1").performTextInput("http://localhost:9090/actuator")
        onNodeWithText("http://localhost:9090/actuator", substring = true).assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should display Actuator Endpoints title`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        // Then
        onNodeWithText("Actuator Endpoints").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should disable Connect button when group name is blank`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextClearance()
        waitForIdle()

        // Then
        onNode(hasText("Connect Group")).assertIsNotEnabled()
    }

    @Test
    fun `AddServerGroupForm should enable Connect button when all fields are filled`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextInput("Test Group")
        waitForIdle()

        // Then
        onNode(hasText("Connect Group")).assertIsEnabled()
    }

    @Test
    fun `AddServerGroupForm should show error when group name is blank on submit`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextClearance()
        waitForIdle()

        // Then
        onNode(hasText("Connect Group")).assertIsNotEnabled()
    }

    @Test
    fun `AddServerGroupForm should show error when duplicate group name is used`() = runComposeUiTest {
        // Given
        val existingServer = Server(Url("http://localhost:8080/actuator"))
        val existingGroup = ServerGroup(name = "Production", endpoints = listOf(existingServer))
        val serverGroups = mutableMapOf(
            existingGroup to ServerGroupState(
                group = existingGroup,
                endpointStates = mapOf(
                    existingServer to ServerState(
                        server = existingServer,
                        beans = UIState.Loading,
                        health = UIState.Loading,
                        configProps = UIState.Loading,
                        metrics = UIState.Loading
                    )
                )
            )
        )
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextClearance()
        onNodeWithText("Group Name").performTextInput("Production")
        waitForIdle()

        onNodeWithText("Connect Group").performClick()
        waitForIdle()

        // Then
        onNodeWithText("A group with this name already exists").assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should show error when endpoint connection fails`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.InternalServerError)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = {},
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextInput("Test")
        waitForIdle()

        onNodeWithText("Connect Group").performClick()
        waitForIdle()

        // Then
        onNodeWithText("Failed to connect to actuator endpoint(s):", substring = true).assertIsDisplayed()
    }

    @Test
    fun `AddServerGroupForm should successfully create server group on happy path`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())
        var addedGroup: ServerGroup? = null

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = { group -> addedGroup = group },
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextClearance()
        onNodeWithText("Group Name").performTextInput("Production")
        waitForIdle()

        onNodeWithText("Connect Group").performClick()
        waitForIdle()

        // Then
        assertEquals(1, serverGroups.size)
        val createdGroup = serverGroups.keys.first()
        assertEquals("Production", createdGroup.name)
        assertEquals(1, createdGroup.endpoints.size)
        assertEquals("http://localhost:8080/actuator", createdGroup.endpoints.first().url.toString())

        val groupState = serverGroups[createdGroup]
        assertNotNull(groupState)
        assertEquals(1, groupState.endpointStates.size)

        val endpointState = groupState.endpointStates.values.first()
        assertTrue(endpointState.beans is UIState.Loading)
        assertTrue(endpointState.health is UIState.Loading)
        assertTrue(endpointState.configProps is UIState.Loading)
        assertTrue(endpointState.metrics is UIState.Loading)

        assertNotNull(addedGroup)
        assertEquals("Production", addedGroup.name)

        onNodeWithText("Error:", substring = true).assertDoesNotExist()
        onNodeWithText("Failed", substring = true).assertDoesNotExist()
    }

    @Test
    fun `AddServerGroupForm should successfully create server group with multiple endpoints`() = runComposeUiTest {
        // Given
        val serverGroups = mutableMapOf<ServerGroup, ServerGroupState>()
        val mockClient = createMockClient(HttpStatusCode.OK)
        val ioScope = CoroutineScope(testDispatcher + SupervisorJob())
        var addedGroup: ServerGroup? = null

        // When
        setContent {
            CompositionLocalProvider(Client provides mockClient) {
                AddServerGroupForm(
                    serverGroups = serverGroups,
                    onServerGroupAdded = { group -> addedGroup = group },
                    ioScope = ioScope
                )
            }
        }

        onNodeWithText("Group Name").performClick()
        onNodeWithText("Group Name").performTextClearance()
        onNodeWithText("Group Name").performTextInput("Multi-Instance")
        waitForIdle()

        onNodeWithText("Add Another Endpoint").performClick()
        waitForIdle()

        onNodeWithText("Endpoint 2").performClick()
        onNodeWithText("Endpoint 2").performTextClearance()
        onNodeWithText("Endpoint 2").performTextInput("http://localhost:8081/actuator")
        waitForIdle()

        onNodeWithText("Connect Group").performClick()
        waitForIdle()

        // Then
        assertEquals(1, serverGroups.size)
        val createdGroup = serverGroups.keys.first()
        assertEquals("Multi-Instance", createdGroup.name)
        assertEquals(2, createdGroup.endpoints.size)

        val endpointUrls = createdGroup.endpoints.map { it.url.toString() }.sorted()
        assertEquals(listOf("http://localhost:8080/actuator", "http://localhost:8081/actuator"), endpointUrls)

        val groupState = serverGroups[createdGroup]
        assertNotNull(groupState)
        assertEquals(2, groupState.endpointStates.size)

        groupState.endpointStates.values.forEach { endpointState ->
            assertTrue(endpointState.beans is UIState.Loading)
            assertTrue(endpointState.health is UIState.Loading)
            assertTrue(endpointState.configProps is UIState.Loading)
            assertTrue(endpointState.metrics is UIState.Loading)
        }

        assertNotNull(addedGroup)
        assertEquals("Multi-Instance", addedGroup.name)
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
