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
        onNodeWithText("health status: UP").assertIsDisplayed()
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
        onNodeWithText("health status: DOWN").assertIsDisplayed()
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
        onNodeWithText("health status: UP").assertIsDisplayed()
        onNodeWithText("db component status: UP").assertIsDisplayed()
        onNodeWithText("diskSpace component status: UP").assertIsDisplayed()
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
        onNodeWithText("health status: DOWN").assertIsDisplayed()
        onNodeWithText("db component status: DOWN").assertIsDisplayed()
    }
}
