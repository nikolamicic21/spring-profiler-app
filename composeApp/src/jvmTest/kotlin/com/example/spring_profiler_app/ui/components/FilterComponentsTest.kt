package com.example.spring_profiler_app.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalTestApi::class)
class FilterComponentsTest {

    @Test
    fun `FilterChipGroup should display label`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter by Status",
                options = listOf("Active", "Inactive"),
                selectedOption = null,
                onOptionSelect = {}
            )
        }

        // Then
        onNodeWithText("Filter by Status").assertIsDisplayed()
    }

    @Test
    fun `FilterChipGroup should display all option and custom options`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2", "Option3"),
                selectedOption = null,
                onOptionSelect = {}
            )
        }

        // Then
        onNodeWithText("all").assertIsDisplayed()
        onNodeWithText("Option1").assertIsDisplayed()
        onNodeWithText("Option2").assertIsDisplayed()
        onNodeWithText("Option3").assertIsDisplayed()
    }

    @Test
    fun `FilterChipGroup should select all by default when selectedOption is null`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = null,
                onOptionSelect = {},
                initiallyExpanded = true
            )
        }

        waitForIdle()

        // Then
        onNode(hasText("all")).assertIsSelected()
    }

    @Test
    fun `FilterChipGroup should select specified option`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = "Option1",
                onOptionSelect = {},
                initiallyExpanded = true
            )
        }

        waitForIdle()

        // Then
        onNode(hasText("Option1")).assertIsSelected()
    }

    @Test
    fun `FilterChipGroup should call onOptionSelect when chip is clicked`() = runComposeUiTest {
        // Given
        var selectedOption: String? = null

        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = selectedOption,
                onOptionSelect = { selectedOption = it },
                initiallyExpanded = true
            )
        }

        waitForIdle()
        onNodeWithText("Option1").performClick()
        waitForIdle()

        // Then
        assertEquals("Option1", selectedOption)
    }

    @Test
    fun `FilterChipGroup should toggle selection when clicking selected option`() = runComposeUiTest {
        // Given
        var selectedOption: String? = "Option1"

        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = selectedOption,
                onOptionSelect = { selectedOption = it },
                initiallyExpanded = true
            )
        }

        waitForIdle()
        onNodeWithText("Option1").performClick()
        waitForIdle()

        // Then
        assertNull(selectedOption)
    }

    @Test
    fun `FilterChipGroup should set selection to null when all is clicked`() = runComposeUiTest {
        // Given
        var selectedOption: String? = "Option1"

        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = selectedOption,
                onOptionSelect = { selectedOption = it },
                initiallyExpanded = true
            )
        }

        waitForIdle()
        onNodeWithText("all").performClick()
        waitForIdle()

        // Then
        assertNull(selectedOption)
    }

    @Test
    fun `FilterChipGroup should be collapsible when collapsible is true`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = null,
                onOptionSelect = {},
                collapsible = true,
                initiallyExpanded = false
            )
        }

        waitForIdle()

        // Then
        onNodeWithContentDescription("Expand").assertIsDisplayed()
        onNode(hasText("all")).assertDoesNotExist()
    }

    @Test
    fun `FilterChipGroup should expand when expand button is clicked`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = null,
                onOptionSelect = {},
                collapsible = true,
                initiallyExpanded = false
            )
        }

        waitForIdle()
        onNodeWithContentDescription("Expand").performClick()
        waitForIdle()

        // Then
        onNodeWithContentDescription("Collapse").assertIsDisplayed()
        onNodeWithText("all").assertIsDisplayed()
        onNodeWithText("Option1").assertIsDisplayed()
    }

    @Test
    fun `FilterChipGroup should collapse when collapse button is clicked`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = null,
                onOptionSelect = {},
                collapsible = true,
                initiallyExpanded = true
            )
        }

        waitForIdle()
        onNodeWithContentDescription("Collapse").performClick()
        waitForIdle()

        // Then
        onNodeWithContentDescription("Expand").assertIsDisplayed()
        onNode(hasText("all")).assertDoesNotExist()
    }

    @Test
    fun `FilterChipGroup should be expanded by default when initiallyExpanded is true`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = null,
                onOptionSelect = {},
                collapsible = true,
                initiallyExpanded = true
            )
        }

        waitForIdle()

        // Then
        onNodeWithContentDescription("Collapse").assertIsDisplayed()
        onNodeWithText("all").assertIsDisplayed()
    }

    @Test
    fun `FilterChipGroup should not show expand button when collapsible is false`() = runComposeUiTest {
        // When
        setContent {
            FilterChipGroup(
                label = "Filter",
                options = listOf("Option1", "Option2"),
                selectedOption = null,
                onOptionSelect = {},
                collapsible = false
            )
        }

        waitForIdle()

        // Then
        onNode(hasContentDescription("Expand")).assertDoesNotExist()
        onNode(hasContentDescription("Collapse")).assertDoesNotExist()
        onNodeWithText("all").assertIsDisplayed()
    }

    @Test
    fun `PartialDataWarning should display title and description`() = runComposeUiTest {
        // When
        setContent {
            PartialDataWarning(
                warnings = listOf("localhost:8080 - Connection failed")
            )
        }

        // Then
        onNodeWithText("Partial Data Available").assertIsDisplayed()
        onNodeWithText("Some endpoints failed or are still loading:").assertIsDisplayed()
    }

    @Test
    fun `PartialDataWarning should display all warnings`() = runComposeUiTest {
        // When
        setContent {
            PartialDataWarning(
                warnings = listOf(
                    "localhost:8080 - Connection failed",
                    "localhost:8081 - Timeout",
                    "localhost:8082 - Still loading..."
                )
            )
        }

        // Then
        onNodeWithText("• localhost:8080 - Connection failed").assertIsDisplayed()
        onNodeWithText("• localhost:8081 - Timeout").assertIsDisplayed()
        onNodeWithText("• localhost:8082 - Still loading...").assertIsDisplayed()
    }

    @Test
    fun `PartialDataWarning should display warning icon`() = runComposeUiTest {
        // When
        setContent {
            PartialDataWarning(
                warnings = listOf("localhost:8080 - Error")
            )
        }

        // Then
        onNodeWithText("Partial Data Available").assertIsDisplayed()
    }

    @Test
    fun `PartialDataWarning should handle empty warnings list`() = runComposeUiTest {
        // When
        setContent {
            PartialDataWarning(warnings = emptyList())
        }

        // Then
        onNodeWithText("Partial Data Available").assertIsDisplayed()
        onNodeWithText("Some endpoints failed or are still loading:").assertIsDisplayed()
    }

    @Test
    fun `PartialDataWarning should handle single warning`() = runComposeUiTest {
        // When
        setContent {
            PartialDataWarning(
                warnings = listOf("localhost:8080 - Connection timeout")
            )
        }

        // Then
        onNodeWithText("• localhost:8080 - Connection timeout").assertIsDisplayed()
    }
}
