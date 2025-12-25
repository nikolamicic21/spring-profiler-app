package com.example.spring_profiler_app.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BadgeTest {

    @Test
    fun `Badge should display text`() = runComposeUiTest {
        // When
        setContent {
            Badge(text = "LABEL")
        }

        // Then
        onNodeWithText("LABEL").assertIsDisplayed()
    }

    @Test
    fun `Badge should display with PRIMARY style`() = runComposeUiTest {
        // When
        setContent {
            Badge(text = "PRIMARY", style = BadgeStyle.PRIMARY)
        }

        // Then
        onNodeWithText("PRIMARY").assertIsDisplayed()
    }

    @Test
    fun `Badge should display with SECONDARY style`() = runComposeUiTest {
        // When
        setContent {
            Badge(text = "SECONDARY", style = BadgeStyle.SECONDARY)
        }

        // Then
        onNodeWithText("SECONDARY").assertIsDisplayed()
    }

    @Test
    fun `Badge should display with TERTIARY style`() = runComposeUiTest {
        // When
        setContent {
            Badge(text = "TERTIARY", style = BadgeStyle.TERTIARY)
        }

        // Then
        onNodeWithText("TERTIARY").assertIsDisplayed()
    }

    @Test
    fun `Badge should display with OUTLINED style`() = runComposeUiTest {
        // When
        setContent {
            Badge(text = "OUTLINED", style = BadgeStyle.OUTLINED)
        }

        // Then
        onNodeWithText("OUTLINED").assertIsDisplayed()
    }

    @Test
    fun `ScopeBadge should display singleton scope`() = runComposeUiTest {
        // When
        setContent {
            ScopeBadge(scope = "singleton")
        }

        // Then
        onNodeWithText("SINGLETON").assertIsDisplayed()
    }

    @Test
    fun `ScopeBadge should display prototype scope`() = runComposeUiTest {
        // When
        setContent {
            ScopeBadge(scope = "prototype")
        }

        // Then
        onNodeWithText("PROTOTYPE").assertIsDisplayed()
    }

    @Test
    fun `ScopeBadge should display custom scope`() = runComposeUiTest {
        // When
        setContent {
            ScopeBadge(scope = "request")
        }

        // Then
        onNodeWithText("REQUEST").assertIsDisplayed()
    }

    @Test
    fun `ScopeBadge should uppercase scope text`() = runComposeUiTest {
        // When
        setContent {
            ScopeBadge(scope = "session")
        }

        // Then
        onNodeWithText("SESSION").assertIsDisplayed()
    }

    @Test
    fun `UnitBadge should display unit`() = runComposeUiTest {
        // When
        setContent {
            UnitBadge(unit = "bytes")
        }

        // Then
        onNodeWithText("bytes").assertIsDisplayed()
    }

    @Test
    fun `UnitBadge should display seconds unit`() = runComposeUiTest {
        // When
        setContent {
            UnitBadge(unit = "seconds")
        }

        // Then
        onNodeWithText("seconds").assertIsDisplayed()
    }

    @Test
    fun `UnitBadge should lowercase unit text`() = runComposeUiTest {
        // When
        setContent {
            UnitBadge(unit = "MILLISECONDS")
        }

        // Then
        onNodeWithText("milliseconds").assertIsDisplayed()
    }

    @Test
    fun `Badge should display multiple badges`() = runComposeUiTest {
        // When
        setContent {
            Badge(text = "TAG1", style = BadgeStyle.PRIMARY)
            Badge(text = "TAG2", style = BadgeStyle.SECONDARY)
            Badge(text = "TAG3", style = BadgeStyle.TERTIARY)
        }

        // Then
        onNodeWithText("TAG1").assertIsDisplayed()
        onNodeWithText("TAG2").assertIsDisplayed()
        onNodeWithText("TAG3").assertIsDisplayed()
    }
}
