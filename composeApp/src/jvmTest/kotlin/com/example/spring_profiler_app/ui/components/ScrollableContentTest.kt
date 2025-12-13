package com.example.spring_profiler_app.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ScrollableContentTest {

    @Test
    fun `ScrollableContent should display content`() = runComposeUiTest {
        // When
        setContent {
            ScrollableContent {
                Text("Test content")
            }
        }

        // Then
        onNodeWithText("Test content").assertIsDisplayed()
    }

    @Test
    fun `ScrollableContent should display multiple items`() = runComposeUiTest {
        // When
        setContent {
            ScrollableContent {
                Text("Item 1")
                Text("Item 2")
                Text("Item 3")
            }
        }

        // Then
        onNodeWithText("Item 1").assertIsDisplayed()
        onNodeWithText("Item 2").assertIsDisplayed()
        onNodeWithText("Item 3").assertIsDisplayed()
    }
}
