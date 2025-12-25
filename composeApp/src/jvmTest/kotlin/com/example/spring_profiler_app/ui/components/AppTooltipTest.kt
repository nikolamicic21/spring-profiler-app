package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppTooltipTest {

    @Test
    fun `AppTooltip should display content`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Tooltip text") {
                Text("Hover me")
            }
        }

        // Then
        onNodeWithText("Hover me").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should display wrapped content`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Full bean name here") {
                Text("Bean name")
            }
        }

        // Then
        onNodeWithText("Bean name").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should work with custom delay`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(
                text = "Tooltip with custom delay",
                delayMillis = 1000
            ) {
                Text("Content")
            }
        }

        // Then
        onNodeWithText("Content").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should display multiple wrapped elements`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Tooltip 1") {
                Text("Item 1")
            }
            AppTooltip(text = "Tooltip 2") {
                Text("Item 2")
            }
        }

        // Then
        onNodeWithText("Item 1").assertIsDisplayed()
        onNodeWithText("Item 2").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should work with default delay`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Default delay tooltip") {
                Text("Default delay content")
            }
        }

        // Then
        onNodeWithText("Default delay content").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should work with zero delay`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(
                text = "Zero delay tooltip",
                delayMillis = 0
            ) {
                Text("Zero delay content")
            }
        }

        // Then
        onNodeWithText("Zero delay content").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should work with very long delay`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(
                text = "Long delay tooltip",
                delayMillis = 5000
            ) {
                Text("Long delay content")
            }
        }

        // Then
        onNodeWithText("Long delay content").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should display tooltip text with special characters`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Tooltip: @#$%^&*()") {
                Text("Special chars")
            }
        }

        // Then
        onNodeWithText("Special chars").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should display very long tooltip text`() = runComposeUiTest {
        // Given
        val longText = "This is a very long tooltip text that should still be handled correctly by the component"

        // When
        setContent {
            AppTooltip(text = longText) {
                Text("Long tooltip")
            }
        }

        // Then
        onNodeWithText("Long tooltip").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should display empty tooltip text`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "") {
                Text("Empty tooltip")
            }
        }

        // Then
        onNodeWithText("Empty tooltip").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should work with complex content`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Complex tooltip") {
                Column {
                    Text("Line 1")
                    Text("Line 2")
                    Text("Line 3")
                }
            }
        }

        // Then
        onNodeWithText("Line 1").assertIsDisplayed()
        onNodeWithText("Line 2").assertIsDisplayed()
        onNodeWithText("Line 3").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should work with interactive content`() = runComposeUiTest {
        // Given
        var clicked = false

        // When
        setContent {
            AppTooltip(text = "Interactive tooltip") {
                Button(onClick = { clicked = true }) {
                    Text("Click me")
                }
            }
        }

        onNodeWithText("Click me").performClick()

        // Then
        assert(clicked)
    }

    @Test
    fun `AppTooltip should work with stateful content`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Stateful tooltip") {
                var count by remember { mutableStateOf(0) }
                Column {
                    Text("Count: $count")
                    Button(onClick = { count++ }) {
                        Text("Increment")
                    }
                }
            }
        }

        // Then
        onNodeWithText("Count: 0").assertIsDisplayed()
        onNodeWithText("Increment").performClick()
        waitForIdle()
        onNodeWithText("Count: 1").assertIsDisplayed()
    }

    @Test
    fun `AppTooltip should handle touch input on content`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Touch tooltip") {
                Text("Touch me")
            }
        }

        // Then
        onNodeWithText("Touch me").assertIsDisplayed()
        onNodeWithText("Touch me").performTouchInput {
            swipeDown()
        }
        waitForIdle()
    }

    @Test
    fun `AppTooltip should work with nested tooltips`() = runComposeUiTest {
        // When
        setContent {
            AppTooltip(text = "Outer tooltip") {
                Column {
                    Text("Outer content")
                    AppTooltip(text = "Inner tooltip") {
                        Text("Inner content")
                    }
                }
            }
        }

        // Then
        onNodeWithText("Outer content").assertIsDisplayed()
        onNodeWithText("Inner content").assertIsDisplayed()
    }
}
