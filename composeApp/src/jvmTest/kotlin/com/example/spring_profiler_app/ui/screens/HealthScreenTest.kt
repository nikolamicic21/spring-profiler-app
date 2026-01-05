package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.HealthResponse
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HealthScreenTest {

    @Test
    fun `HealthScreen should display loading state`() = runComposeUiTest {
        // Given
        val healthState: UIState<HealthResponse> = UIState.Loading

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch health data"
        val healthState: UIState<HealthResponse> = UIState.Error(errorMessage)

        // When
        setContent {
            HealthScreen(
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
        val healthResponse = HealthResponse("UP", null)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
        val healthResponse = HealthResponse("DOWN", null)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
            "db" to HealthResponse.Component("UP"),
            "diskSpace" to HealthResponse.Component("UP")
        )
        val healthResponse = HealthResponse("UP", components)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("System Components").assertIsDisplayed()
        onNodeWithText("db").assertIsDisplayed()
        onNodeWithText("diskSpace").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display component with DOWN status`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to HealthResponse.Component("DOWN")
        )
        val healthResponse = HealthResponse("DOWN", components)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
            "db" to HealthResponse.Component("UP"),
            "redis" to HealthResponse.Component("DOWN"),
            "diskSpace" to HealthResponse.Component("UP")
        )
        val healthResponse = HealthResponse("DOWN", components)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
            "db" to HealthResponse.Component("UP")
        )
        val healthResponse = HealthResponse("UP", components)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System Components").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should handle empty components`() = runComposeUiTest {
        // Given
        val healthResponse = HealthResponse("UP", emptyMap())
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
        val healthResponse = HealthResponse("UP", null)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
        val healthResponse = HealthResponse("UP", emptyMap())
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
        val healthResponse = HealthResponse("DOWN", null)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
        onNodeWithText("System Components").assertDoesNotExist()
    }

    @Test
    fun `HealthScreen should display System Components label only when components exist`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "db" to HealthResponse.Component("UP"),
            "cache" to HealthResponse.Component("UP")
        )
        val healthResponse = HealthResponse("UP", components)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is UP").assertIsDisplayed()
        onNodeWithText("System Components").assertIsDisplayed()
        onNodeWithText("db").assertIsDisplayed()
        onNodeWithText("cache").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display OUT_OF_SERVICE status correctly`() = runComposeUiTest {
        // Given
        val components = mapOf(
            "service" to HealthResponse.Component("OUT_OF_SERVICE")
        )
        val healthResponse = HealthResponse("OUT_OF_SERVICE", components)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is OUT_OF_SERVICE").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
        onNodeWithText("System Components").assertIsDisplayed()
        onNodeWithText("service").assertIsDisplayed()
    }

    @Test
    fun `HealthScreen should display global status hero with correct message for UP status`() = runComposeUiTest {
        // Given
        val healthResponse = HealthResponse("UP", null)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
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
        val healthResponse = HealthResponse("DOWN", null)
        val healthState = UIState.Success(healthResponse)

        // When
        setContent {
            HealthScreen(
                healthState = healthState,
                refreshHealthCallback = {}
            )
        }

        // Then
        onNodeWithText("System is DOWN").assertIsDisplayed()
        onNodeWithText("Action required: Some components are failing").assertIsDisplayed()
    }
}
