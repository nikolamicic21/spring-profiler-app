package com.example.spring_profiler_app.ui.panels

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.data.ConfigPropsResponse
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.MetricsResponse
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
import io.ktor.http.Url
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ServerDetailsPanelTest {

    @Test
    fun `ServerDetailsPanel should display all endpoint tabs`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        // When
        setContent {
            ServerDetailsPanel(
                serverState = serverState,
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
    fun `ServerDetailsPanel should display Beans screen by default`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Success(BeansResponse(emptyMap())),
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        // When
        setContent {
            ServerDetailsPanel(
                serverState = serverState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Search for Bean by name").assertIsDisplayed()
    }

    @Test
    fun `ServerDetailsPanel should switch to Health screen when Health tab is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Success(BeansResponse(emptyMap())),
            health = UIState.Success(HealthResponse("UP", null)),
            configProps = UIState.Loading,
            metrics = UIState.Loading
        )

        // When
        setContent {
            ServerDetailsPanel(
                serverState = serverState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Health").performClick()
        onNodeWithText("health status: UP").assertIsDisplayed()
    }

    @Test
    fun `ServerDetailsPanel should switch to Configuration properties screen when tab is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Success(ConfigPropsResponse(emptyMap())),
            metrics = UIState.Loading
        )

        // When
        setContent {
            ServerDetailsPanel(
                serverState = serverState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Configuration properties").performClick()
        onNodeWithText("Configuration properties").assertIsDisplayed()
    }

    @Test
    fun `ServerDetailsPanel should switch to Metrics screen when Metrics tab is clicked`() = runComposeUiTest {
        // Given
        val server = Server(Url("http://localhost:8080/actuator"))
        val serverState = ServerState(
            server = server,
            beans = UIState.Loading,
            health = UIState.Loading,
            configProps = UIState.Loading,
            metrics = UIState.Success(MetricsResponse(emptyList()))
        )

        // When
        setContent {
            ServerDetailsPanel(
                serverState = serverState,
                refreshHealthCallback = {},
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("Metrics").performClick()
        onNodeWithText("Metrics").assertIsDisplayed()
    }
}
