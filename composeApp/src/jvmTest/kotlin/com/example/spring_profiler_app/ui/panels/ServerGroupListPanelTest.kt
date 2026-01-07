package com.example.spring_profiler_app.ui.panels

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ServerGroupListPanelTest {

    @Test
    fun `ServerGroupListPanel should display title`() = runComposeUiTest {
        // Given
        val serverGroups = emptyMap<ServerGroup, ServerGroupState>()

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroupKey = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onDeleteGroup = {},
            )
        }

        // Then
        onNodeWithText("Server Groups").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListPanel should display Add a new group button`() = runComposeUiTest {
        // Given
        val serverGroups = emptyMap<ServerGroup, ServerGroupState>()

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroupKey = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onDeleteGroup = {},
            )
        }

        // Then
        onNodeWithText("Add a new group").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListPanel should call onAddGroupClick when Add a new group button is clicked`() = runComposeUiTest {
        // Given
        val serverGroups = emptyMap<ServerGroup, ServerGroupState>()
        var addGroupClicked = false

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroupKey = null,
                onAddGroupClick = { addGroupClicked = true },
                onGroupSelect = {},
                onRefreshGroup = {},
                onDeleteGroup = {},
            )
        }

        // Then
        onNodeWithText("Add a new group").performClick()
        assertTrue(addGroupClicked)
    }

    @Test
    fun `ServerGroupListPanel should display group name and endpoint count when group is added`() = runComposeUiTest {
        // Given
        val server1 = Server(Url("http://localhost:8080/actuator"))
        val server2 = Server(Url("http://localhost:8081/actuator"))
        val group = ServerGroup(name = "Production", endpoints = listOf(server1, server2))
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
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )
        val serverGroups = mapOf(group to groupState)

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroupKey = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onDeleteGroup = {},
            )
        }

        // Then
        onNodeWithText("Production").assertIsDisplayed()
        onNodeWithText("2 endpoints").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListPanel should display multiple groups`() = runComposeUiTest {
        // Given
        val server1 = Server(Url("http://localhost:8080/actuator"))
        val server2 = Server(Url("http://localhost:9090/actuator"))
        val group1 = ServerGroup(name = "Production", endpoints = listOf(server1))
        val group2 = ServerGroup(name = "Staging", endpoints = listOf(server2))

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

        val groupState1 = ServerGroupState(group = group1, endpointStates = mapOf(server1 to serverState1))
        val groupState2 = ServerGroupState(group = group2, endpointStates = mapOf(server2 to serverState2))

        val serverGroups = mapOf(group1 to groupState1, group2 to groupState2)

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroupKey = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onDeleteGroup = {},
            )
        }

        // Then
        onNodeWithText("Production").assertIsDisplayed()
        onNodeWithText("Staging").assertIsDisplayed()
    }
}
