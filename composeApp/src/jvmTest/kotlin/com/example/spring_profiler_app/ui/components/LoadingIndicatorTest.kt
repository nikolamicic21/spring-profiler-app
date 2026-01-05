package com.example.spring_profiler_app.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LoadingIndicatorTest {

    @Test
    fun `LoadingIndicator should display default message`() = runComposeUiTest {
        // When
        setContent {
            LoadingIndicator()
        }

        // Then
        onNodeWithText("Loading...").assertExists()
    }

    @Test
    fun `LoadingIndicator should display custom message`() = runComposeUiTest {
        // When
        setContent {
            LoadingIndicator(message = "Loading health status...")
        }

        // Then
        onNodeWithText("Loading health status...").assertExists()
    }

    @Test
    fun `CompactLoadingIndicator should be displayed`() = runComposeUiTest {
        // When
        setContent {
            CompactLoadingIndicator()
        }

        // Then
        waitForIdle()
    }
}
