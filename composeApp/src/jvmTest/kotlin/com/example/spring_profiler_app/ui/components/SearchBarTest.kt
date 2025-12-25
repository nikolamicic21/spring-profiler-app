package com.example.spring_profiler_app.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SearchBarTest {

    @Test
    fun `SearchBar should display placeholder text`() = runComposeUiTest {
        // When
        setContent {
            SearchBar(
                searchQuery = "",
                onSearchQueryChange = {},
                placeholder = "Search items..."
            )
        }

        // Then
        onNodeWithText("Search items...").assertIsDisplayed()
    }

    @Test
    fun `SearchBar should display search query`() = runComposeUiTest {
        // When
        setContent {
            SearchBar(
                searchQuery = "test query",
                onSearchQueryChange = {},
                placeholder = "Search..."
            )
        }

        // Then
        onNodeWithText("test query").assertIsDisplayed()
    }

    @Test
    fun `SearchBar should show clear button when query is not empty`() = runComposeUiTest {
        // When
        setContent {
            SearchBar(
                searchQuery = "test",
                onSearchQueryChange = {},
                placeholder = "Search..."
            )
        }

        // Then
        onNodeWithContentDescription("Clear").assertIsDisplayed()
    }

    @Test
    fun `SearchBar should not show clear button when query is empty`() = runComposeUiTest {
        // When
        setContent {
            SearchBar(
                searchQuery = "",
                onSearchQueryChange = {},
                placeholder = "Search..."
            )
        }

        // Then
        onNodeWithContentDescription("Clear").assertDoesNotExist()
    }

    @Test
    fun `SearchBar should call onSearchQueryChange when text is entered`() = runComposeUiTest {
        // Given
        var capturedQuery = ""

        // When
        setContent {
            SearchBar(
                searchQuery = capturedQuery,
                onSearchQueryChange = { capturedQuery = it },
                placeholder = "Search..."
            )
        }

        onNodeWithText("Search...").performClick()
        onNodeWithText("Search...").performTextInput("new query")

        // Then
        assertEquals("new query", capturedQuery)
    }

    @Test
    fun `SearchBar should call onSearchQueryChange with empty string when clear is clicked`() = runComposeUiTest {
        // Given
        var capturedQuery = "initial query"

        // When
        setContent {
            SearchBar(
                searchQuery = capturedQuery,
                onSearchQueryChange = { capturedQuery = it },
                placeholder = "Search..."
            )
        }

        onNodeWithContentDescription("Clear").performClick()

        // Then
        assertEquals("", capturedQuery)
    }

    @Test
    fun `SearchBar should display custom placeholder`() = runComposeUiTest {
        // When
        setContent {
            SearchBar(
                searchQuery = "",
                onSearchQueryChange = {},
                placeholder = "Search beans by name..."
            )
        }

        // Then
        onNodeWithText("Search beans by name...").assertIsDisplayed()
    }
}
