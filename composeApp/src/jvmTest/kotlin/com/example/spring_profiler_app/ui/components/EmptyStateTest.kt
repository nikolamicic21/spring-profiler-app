package com.example.spring_profiler_app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EmptyStateTest {

    @Test
    fun `EmptyState should display message`() = runComposeUiTest {
        // When
        setContent {
            EmptyState(message = "No items found")
        }

        // Then
        onNodeWithText("No items found").assertIsDisplayed()
    }

    @Test
    fun `EmptyState should display custom message`() = runComposeUiTest {
        // When
        setContent {
            EmptyState(message = "No bean names match your search.")
        }

        // Then
        onNodeWithText("No bean names match your search.").assertIsDisplayed()
    }

    @Test
    fun `EmptyState should display with default icon`() = runComposeUiTest {
        // When
        setContent {
            EmptyState(
                message = "No results",
                icon = Icons.Default.SearchOff
            )
        }

        // Then
        onNodeWithText("No results").assertIsDisplayed()
    }

    @Test
    fun `EmptyState should display with custom icon`() = runComposeUiTest {
        // When
        setContent {
            EmptyState(
                message = "Error occurred",
                icon = Icons.Default.Error
            )
        }

        // Then
        onNodeWithText("Error occurred").assertIsDisplayed()
    }

    @Test
    fun `EmptyState should display without icon`() = runComposeUiTest {
        // When
        setContent {
            EmptyState(
                message = "No data available",
                icon = null
            )
        }

        // Then
        onNodeWithText("No data available").assertIsDisplayed()
    }

    @Test
    fun `EmptyState should display long message`() = runComposeUiTest {
        // Given
        val longMessage = "This is a very long message that explains why there are no items to display in this view"

        // When
        setContent {
            EmptyState(message = longMessage)
        }

        // Then
        onNodeWithText(longMessage).assertIsDisplayed()
    }
}
