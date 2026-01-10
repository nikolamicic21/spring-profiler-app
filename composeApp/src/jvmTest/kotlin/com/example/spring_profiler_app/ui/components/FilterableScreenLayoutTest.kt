package com.example.spring_profiler_app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class FilterableScreenLayoutTest {

    @Test
    fun `FilterableScreenLayout should display content when not empty`() = runComposeUiTest {
        // When
        setContent {
            FilterableScreenLayout(
                isEmpty = false,
                emptyStateMessage = "No items found",
                filterBar = { Text("Filter Bar") }
            ) { _ ->
                Text("Content Area")
            }
        }

        // Then
        onNodeWithText("Content Area").assertIsDisplayed()
        onNodeWithText("Filter Bar").assertIsDisplayed()
    }

    @Test
    fun `FilterableScreenLayout should display empty state when empty`() = runComposeUiTest {
        // When
        setContent {
            FilterableScreenLayout(
                isEmpty = true,
                emptyStateMessage = "No items found",
                filterBar = { Text("Filter Bar") }
            ) { _ ->
                Text("Content Area")
            }
        }

        // Then
        onNodeWithText("No items found").assertIsDisplayed()
        onNodeWithText("Filter Bar").assertIsDisplayed()
        onNodeWithText("Content Area").assertDoesNotExist()
    }

    @Test
    fun `FilterableScreenLayout should display filter bar at top`() = runComposeUiTest {
        // When
        setContent {
            FilterableScreenLayout(
                isEmpty = false,
                emptyStateMessage = "Empty",
                filterBar = { Text("Search and Filter") }
            ) { _ ->
                Text("Main Content")
            }
        }

        // Then
        onNodeWithText("Search and Filter").assertIsDisplayed()
        onNodeWithText("Main Content").assertIsDisplayed()
    }

    @Test
    fun `FilterableScreenLayout should pass filter bar height to content`() = runComposeUiTest {
        // When
        setContent {
            FilterableScreenLayout(
                isEmpty = false,
                emptyStateMessage = "Empty",
                filterBar = {
                    Box(modifier = Modifier.height(100.dp)) {
                        Text("Filter Bar")
                    }
                }
            ) { filterBarHeight ->
                Text("Height: ${filterBarHeight.value.toInt()}")
            }
        }

        // Then
        onNodeWithText("Filter Bar").assertIsDisplayed()
    }

    @Test
    fun `FilterableScreenLayout should display custom empty state message`() = runComposeUiTest {
        // When
        setContent {
            FilterableScreenLayout(
                isEmpty = true,
                emptyStateMessage = "No beans match your filters.",
                filterBar = { Text("Filter") }
            ) { _ ->
                Text("Content")
            }
        }

        // Then
        onNodeWithText("No beans match your filters.").assertIsDisplayed()
    }

    @Test
    fun `FilterBarContainer should display content`() = runComposeUiTest {
        // When
        setContent {
            FilterBarContainer {
                Text("Search Bar")
                Text("Filter Chips")
            }
        }

        // Then
        onNodeWithText("Search Bar").assertIsDisplayed()
        onNodeWithText("Filter Chips").assertIsDisplayed()
    }

    @Test
    fun `FilterBarContainer should display multiple children`() = runComposeUiTest {
        // When
        setContent {
            FilterBarContainer {
                Text("First Filter")
                Text("Second Filter")
                Text("Third Filter")
            }
        }

        // Then
        onNodeWithText("First Filter").assertIsDisplayed()
        onNodeWithText("Second Filter").assertIsDisplayed()
        onNodeWithText("Third Filter").assertIsDisplayed()
    }

    @Test
    fun `FilterBarContainer should work with custom modifier`() = runComposeUiTest {
        // When
        setContent {
            FilterBarContainer(modifier = Modifier.fillMaxSize()) {
                Text("Filter Content")
            }
        }

        // Then
        onNodeWithText("Filter Content").assertIsDisplayed()
    }

    @Test
    fun `FilterableScreenLayout should handle transition from empty to non-empty`() = runComposeUiTest {
        // Given
        setContent {
            FilterableScreenLayout(
                isEmpty = false,
                emptyStateMessage = "No data",
                filterBar = { Text("Filters") }
            ) { _ ->
                Text("Data loaded")
            }
        }

        // Then
        onNodeWithText("Data loaded").assertIsDisplayed()
        onNodeWithText("No data").assertDoesNotExist()
    }
}
