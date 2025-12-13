package com.example.spring_profiler_app.ui.panels

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ServerListPanelTest {

    @Test
    fun `ServerListPanel should display title`() = runComposeUiTest {
        // Given
        val servers = emptyMap<Server, ServerState>()

        // When
        setContent {
            ServerListPanel(
                servers = servers,
                currentServerKey = null,
                onAddServerClick = {},
                onServerSelect = {},
                onRefreshServer = {}
            )
        }

        // Then
        onNodeWithText("Servers").assertIsDisplayed()
    }

    @Test
    fun `ServerListPanel should display Add a new server button`() = runComposeUiTest {
        // Given
        val servers = emptyMap<Server, ServerState>()

        // When
        setContent {
            ServerListPanel(
                servers = servers,
                currentServerKey = null,
                onAddServerClick = {},
                onServerSelect = {},
                onRefreshServer = {}
            )
        }

        // Then
        onNodeWithText("Add a new server").assertIsDisplayed()
    }

    @Test
    fun `ServerListPanel should call onAddServerClick when Add a new server button is clicked`() = runComposeUiTest {
        // Given
        val servers = emptyMap<Server, ServerState>()
        var addServerClicked = false

        // When
        setContent {
            ServerListPanel(
                servers = servers,
                currentServerKey = null,
                onAddServerClick = { addServerClicked = true },
                onServerSelect = {},
                onRefreshServer = {}
            )
        }

        // Then
        onNodeWithText("Add a new server").performClick()
        assertTrue(addServerClicked)
    }

    @Test
    fun `ServerListPanel should display server host and port when server is added`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val servers = mapOf(server to serverState)

        // When
        setContent {
            ServerListPanel(
                servers = servers,
                currentServerKey = null,
                onAddServerClick = {},
                onServerSelect = {},
                onRefreshServer = {}
            )
        }

        // Then
        onNodeWithText("localhost:8080").assertIsDisplayed()
    }

    @Test
    fun `ServerListPanel should display multiple servers`() = runComposeUiTest {
        // Given
        val server1 = Server(Url("http://localhost:8080/actuator"))
        val server2 = Server(Url("http://localhost:9090/actuator"))
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
        val servers = mapOf(
            server1 to serverState1,
            server2 to serverState2
        )

        // When
        setContent {
            ServerListPanel(
                servers = servers,
                currentServerKey = null,
                onAddServerClick = {},
                onServerSelect = {},
                onRefreshServer = {}
            )
        }

        // Then
        onNodeWithText("localhost:8080").assertIsDisplayed()
        onNodeWithText("localhost:9090").assertIsDisplayed()
    }

    @Test
    fun `ServerListPanel should call onServerSelect when server is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val servers = mapOf(server to serverState)
        var selectedServer: Server? = null

        // When
        setContent {
            ServerListPanel(
                servers = servers,
                currentServerKey = null,
                onAddServerClick = {},
                onServerSelect = { selectedServer = it },
                onRefreshServer = {}
            )
        }

        // Then
        onNodeWithText("localhost:8080").performClick()
        assertEquals(server, selectedServer)
    }
}
