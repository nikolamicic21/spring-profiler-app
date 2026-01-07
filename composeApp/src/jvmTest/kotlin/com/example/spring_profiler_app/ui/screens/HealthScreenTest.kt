package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.AggregatedHealthResponse
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HealthScreenTest {

    @Test
    fun `HealthScreen should display loading state`() = runComposeUiTest {
        // Given
        val healthState: UIState<AggregatedHealthResponse> = UIState.Loading

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading health status...").assertExists()
    }

    @Test
    fun `HealthScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch health data"
        val healthState: UIState<AggregatedHealthResponse> = UIState.Error(errorMessage)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display health status when data is loaded`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("All systems operational").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display health status DOWN`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "DOWN",
            components = emptyMap()
        )
        val healthResponse = AggregatedHealthResponse(status = "DOWN", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display component statuses when available`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to "UP",
            "diskSpace" to "UP"
        )
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = components
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("COMPONENTS (2)").assertIsDisplayed()
        onNodeWithText("db").assertIsDisplayed()
        onNodeWithText("diskSpace").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display component with DOWN status`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to "DOWN"
        )
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "DOWN",
            components = components
        )
        val healthResponse = AggregatedHealthResponse(status = "DOWN", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("db").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display multiple components with different statuses`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to "UP",
            "redis" to "DOWN",
            "diskSpace" to "UP"
        )
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "DOWN",
            components = components
        )
        val healthResponse = AggregatedHealthResponse(status = "DOWN", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("db").assertIsDisplayed()
        onNodeWithText("redis").assertIsDisplayed()
        onNodeWithText("diskSpace").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display System Components header when components exist`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to "UP"
        )
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = components
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("COMPONENTS (1)").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should handle empty components`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("All systems operational").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should NOT display System Components label when components is null`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("System Components").assertDoesNotExist()
    }

    @Test
    fun `HealthScreen should NOT display System Components label when components is empty map`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("All systems operational").assertIsDisplayed()
        onNodeWithText("System Components").assertDoesNotExist()
    }

    @Test
    fun `HealthScreen should NOT display System Components label when DOWN with no components`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "DOWN",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "DOWN", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
        onNodeWithText("COMPONENTS").assertDoesNotExist()
    }

    @Test
    fun `HealthScreen should display System Components label only when components exist`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to "UP",
            "cache" to "UP"
        )
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = components
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("COMPONENTS (2)").assertIsDisplayed()
        onNodeWithText("db").assertIsDisplayed()
        onNodeWithText("cache").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display OUT_OF_SERVICE status correctly`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "service" to "OUT_OF_SERVICE"
        )
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "OUT_OF_SERVICE",
            components = components
        )
        val healthResponse = AggregatedHealthResponse(status = "OUT_OF_SERVICE", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is OUT_OF_SERVICE").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
        onNodeWithText("COMPONENTS (1)").assertIsDisplayed()
        onNodeWithText("service").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display global status hero with correct message for UP status`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "UP",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "UP", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("All systems operational").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display global status hero with correct message for DOWN status`() = runComposeUiTest {
        // Given
        val endpointHealth = AggregatedHealthResponse.EndpointHealth(
            endpoint = "localhost:8080",
            status = "DOWN",
            components = emptyMap<String, String>()
        )
        val healthResponse = AggregatedHealthResponse(status = "DOWN", endpoints = listOf(endpointHealth))
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
    }

    @Test
    fun `AggregatedHealthScreen should display health from multiple endpoints`() = runComposeUiTest {
        // Given
        val endpoint1 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "prod-server-1",
            status = "UP",
            components = mapOf("db" to "UP", "diskSpace" to "UP")
        )
        val endpoint2 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "prod-server-2",
            status = "UP",
            components = mapOf("db" to "UP")
        )
        val endpoint3 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "staging-server",
            status = "DOWN",
            components = mapOf("db" to "DOWN")
        )

        val healthResponse = AggregatedHealthResponse(
            status = "DOWN",
            endpoints = listOf(endpoint1, endpoint2, endpoint3)
        )
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        waitForIdle()

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()

        onNodeWithText("prod-server-1").assertIsDisplayed()
        onNodeWithText("prod-server-2").assertIsDisplayed()
        onNodeWithText("staging-server").assertIsDisplayed()
    }

    @Test
    fun `AggregatedHealthScreen should calculate global status from multiple endpoints`() = runComposeUiTest {
        // Given
        val endpoint1 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "server-1",
            status = "UP",
            components = mapOf("db" to "UP")
        )
        val endpoint2 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "server-2",
            status = "UP",
            components = mapOf("db" to "UP")
        )

        val healthResponse = AggregatedHealthResponse(
            status = "UP",
            endpoints = listOf(endpoint1, endpoint2)
        )
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        waitForIdle()

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("All systems operational").assertIsDisplayed()
    }

    @Test
    fun `AggregatedHealthScreen should show mixed health statuses across endpoints`() = runComposeUiTest {
        // Given
        val endpoint1 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "api-server",
            status = "UP",
            components = mapOf(
                "db" to "UP",
                "diskSpace" to "UP",
                "ping" to "UP"
            )
        )
        val endpoint2 = AggregatedHealthResponse.EndpointHealth(
            endpoint = "worker-server",
            status = "DOWN",
            components = mapOf(
                "db" to "DOWN",
                "redis" to "UP"
            )
        )

        val healthResponse = AggregatedHealthResponse(
            status = "DOWN",
            endpoints = listOf(endpoint1, endpoint2)
        )
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            AggregatedHealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        waitForIdle()

        // Then
        onNodeWithText("api-server").assertIsDisplayed()
        onNodeWithText("worker-server").assertIsDisplayed()

        onNodeWithText("COMPONENTS (3)").assertIsDisplayed()
        onNodeWithText("COMPONENTS (2)").assertIsDisplayed()
    }
}
