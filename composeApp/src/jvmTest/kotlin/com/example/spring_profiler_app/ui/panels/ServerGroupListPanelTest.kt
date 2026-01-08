package com.example.spring_profiler_app.ui.panels

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
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
import kotlin.test.assertEquals
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
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
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
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
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
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = { addGroupClicked = true },
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
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
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
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
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
                onDeleteGroup = {},
            )
        }

        // Then
        onNodeWithText("Production").assertIsDisplayed()
        onNodeWithText("Staging").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListPanel should call onEditGroup when edit icon is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val group = ServerGroup(name = "Production", endpoints = listOf(server))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(group = group, endpointStates = mapOf(server to serverState))
        val serverGroups = mapOf(group to groupState)
        var editedGroup: ServerGroup? = null

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = { editedGroup = it },
                onDeleteGroup = {},
            )
        }

        onNode(hasContentDescription("Edit group")).performClick()
        waitForIdle()

        // Then
        assertTrue(editedGroup == group)
    }

    @Test
    fun `ServerGroupListItem should show individual action icons in wide layout`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(group = group, endpointStates = mapOf(server to serverState))
        val serverGroups = mapOf(group to groupState)

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
                onDeleteGroup = {},
            )
        }

        waitForIdle()

        // Then
        onNodeWithContentDescription("Refresh data").assertIsDisplayed()
        onNodeWithContentDescription("Edit group").assertIsDisplayed()
        onNodeWithContentDescription("Delete group").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListItem should call onRefresh when refresh icon is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(group = group, endpointStates = mapOf(server to serverState))
        val serverGroups = mapOf(group to groupState)
        var refreshedGroup: ServerGroup? = null

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = { refreshedGroup = it },
                onEditGroup = {},
                onDeleteGroup = {},
            )
        }

        waitForIdle()

        onNodeWithContentDescription("Refresh data").performClick()
        waitForIdle()

        // Then
        assertEquals(group, refreshedGroup)
    }

    @Test
    fun `ServerGroupListItem should call onDelete when delete icon is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(group = group, endpointStates = mapOf(server to serverState))
        val serverGroups = mapOf(group to groupState)
        var deletedGroup: ServerGroup? = null

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
                onDeleteGroup = { deletedGroup = it },
            )
        }

        waitForIdle()

        onNodeWithContentDescription("Delete group").performClick()
        waitForIdle()

        // Then
        assertEquals(group, deletedGroup)
    }

    @Test
    fun `ServerGroupListItem should display truncated name with ellipsis for long names`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val longName = "This is a very long server group name that should be truncated with ellipsis"
        val group = ServerGroup(name = longName, endpoints = listOf(server))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(group = group, endpointStates = mapOf(server to serverState))
        val serverGroups = mapOf(group to groupState)

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
                onDeleteGroup = {},
            )
        }

        waitForIdle()

        // Then
        onNodeWithText(longName, substring = true).assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListItem should display endpoint count correctly`() = runComposeUiTest {
        // Given
        val server1 = Server(Url("http://localhost:8080/actuator"))
        val server2 = Server(Url("http://localhost:8081/actuator"))
        val server3 = Server(Url("http://localhost:8082/actuator"))
        val group = ServerGroup(name = "Test", endpoints = listOf(server1, server2, server3))
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
        val serverState3 = ServerState(
            server = server3,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2, server3 to serverState3)
        )
        val serverGroups = mapOf(group to groupState)

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = null,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
                onDeleteGroup = {},
            )
        }

        waitForIdle()

        // Then
        onNodeWithText("3 endpoints").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupListItem should show selected state with primary color`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val group = ServerGroup(name = "Production", endpoints = listOf(server))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val groupState = ServerGroupState(group = group, endpointStates = mapOf(server to serverState))
        val serverGroups = mapOf(group to groupState)

        // When
        setContent {
            ServerGroupListPanel(
                serverGroups = serverGroups,
                currentGroup = group,
                editingGroup = null,
                onAddGroupClick = {},
                onGroupSelect = {},
                onRefreshGroup = {},
                onEditGroup = {},
                onDeleteGroup = {},
            )
        }

        waitForIdle()

        // Then
        onNodeWithText("Production").assertIsDisplayed()
    }
}
