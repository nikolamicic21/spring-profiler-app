package com.example.spring_profiler_app.ui.panels

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.Bean
import com.example.spring_profiler_app.data.Beans
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.Measurement
import com.example.spring_profiler_app.data.Metric
import com.example.spring_profiler_app.data.MetricsResponse
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
import io.ktor.http.Url
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ServerGroupDetailsPanelTest {

    @Test
    fun `ServerGroupDetailsPanel should display all endpoint tabs`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Production", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Beans").assertIsDisplayed()
        onNodeWithText("Health").assertIsDisplayed()
        onNodeWithText("Configuration properties").assertIsDisplayed()
        onNodeWithText("Metrics").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupDetailsPanel should display group name and endpoint count`() = runComposeUiTest {
        // Given
        val server1 = Server(Url("http://localhost:8080/actuator"))
        val server2 = Server(Url("http://localhost:8081/actuator"))
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
        val group = ServerGroup(name = "Production Cluster", endpoints = listOf(server1, server2))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server1 to serverState1, server2 to serverState2)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Production Cluster").assertIsDisplayed()
        onNodeWithText("2 endpoints").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupDetailsPanel should display Beans tab by default`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Beans").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupDetailsPanel should switch to Health tab when clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        onNodeWithText("Health").performClick()
        waitForIdle()

        // Then
        onNode(hasText("Health")).assertIsSelected()
    }

    @Test
    fun `ServerGroupDetailsPanel should switch to Configuration properties tab when clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        onNodeWithText("Configuration properties").performClick()
        waitForIdle()

        // Then
        onNode(hasText("Configuration properties")).assertIsSelected()
    }

    @Test
    fun `ServerGroupDetailsPanel should switch to Metrics tab when clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        onNodeWithText("Metrics").performClick()
        waitForIdle()

        // Then
        onNode(hasText("Metrics")).assertIsSelected()
    }

    @Test
    fun `ServerGroupDetailsPanel should display single endpoint correctly`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Single Server", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Single Server").assertIsDisplayed()
        onNodeWithText("1 endpoint").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupDetailsPanel should display beans data when available`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val bean = Bean(
            dependencies = emptyList(),
            scope = "singleton"
        )
        val beans = Beans(
            beans = mapOf("testBean" to bean)
        )
        val beansData = BeansResponse(
            contexts = mapOf("application" to beans)
        )

        val serverState = ServerState(
            server = server,
            beans = UIState.Success(beansData),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        waitForIdle()
        onNodeWithText("testBean").assertIsDisplayed()
    }

    @Test
    fun `ServerGroupDetailsPanel should display health data when available`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val healthData = HealthResponse(
            status = "UP",
            components = mapOf("diskSpace" to HealthResponse.Component(status = "UP"))
        )

        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Success(healthData),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        onNodeWithText("Health").performClick()
        waitForIdle()

        // Then
        onNodeWithText("System is UP", substring = true).assertIsDisplayed()
    }

    @Test
    fun `ServerGroupDetailsPanel should display metrics data when available`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val metric = Metric(
            name = "jvm.memory.used",
            measurements = listOf(
                Measurement(statistic = "VALUE", value = 1024.0)
            ),
            unit = "bytes"
        )
        val metricsData = MetricsResponse(metrics = listOf(metric))

        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Success(metricsData)
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        onNodeWithText("Metrics").performClick()
        waitForIdle()

        // Then
        onNode(hasText("Metrics")).assertIsSelected()
    }

    @Test
    fun `ServerGroupDetailsPanel should call refresh callback when Health tab is active`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        onNodeWithText("Health").performClick()
        waitForIdle()

        // Then
        onNode(hasText("Health")).assertIsSelected()
    }

    @Test
    fun `ServerGroupDetailsPanel should display loading state for beans`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )
        val group = ServerGroup(name = "Test", endpoints = listOf(server))
        val groupState = ServerGroupState(
            group = group,
            endpointStates = mapOf(server to serverState)
        )

        // When
        setContent {
            ServerGroupDetailsPanel(
                groupState = groupState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading beans...", substring = true).assertExists()
    }
}
