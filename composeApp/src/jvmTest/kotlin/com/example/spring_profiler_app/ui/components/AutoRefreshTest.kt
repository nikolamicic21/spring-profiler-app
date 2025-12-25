package com.example.spring_profiler_app.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalTestApi::class)
class AutoRefreshTest {

    @Test
    fun `AutoRefresh should call onRefresh initially`() = runComposeUiTest {
        // Given
        var refreshCount = 0

        // When
        setContent {
            AutoRefresh(interval = 1.seconds) {
                refreshCount++
            }
            Text("Content")
        }

        waitForIdle()

        // Then
        assertEquals(1, refreshCount)
        onNodeWithText("Content").assertIsDisplayed()
    }

    @Test
    fun `AutoRefresh should work with different intervals`() = runComposeUiTest {
        // Given
        var refreshCount = 0

        // When
        setContent {
            AutoRefresh(interval = 500.milliseconds) {
                refreshCount++
            }
            Text("Test")
        }

        waitForIdle()

        // Then
        assertEquals(1, refreshCount)
        onNodeWithText("Test").assertIsDisplayed()
    }

    @Test
    fun `AutoRefresh should update state on refresh`() = runComposeUiTest {
        // Given
        var counter by mutableStateOf(0)

        // When
        setContent {
            AutoRefresh(interval = 1.seconds) {
                counter++
            }
            Text("Counter: $counter")
        }

        waitForIdle()

        // Then
        onNodeWithText("Counter: 1").assertIsDisplayed()
    }

    @Test
    fun `AutoRefresh should work with suspend functions`() = runComposeUiTest {
        // Given
        var refreshed = false

        // When
        setContent {
            AutoRefresh(interval = 1.seconds) {
                kotlinx.coroutines.delay(10)
                refreshed = true
            }
            Text("Refreshing")
        }

        waitForIdle()

        // Then
        assertEquals(true, refreshed)
        onNodeWithText("Refreshing").assertIsDisplayed()
    }
}
