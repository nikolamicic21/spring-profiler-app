package com.example.spring_profiler_app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun `EndpointContextBadges should display endpoint badge`() = runComposeUiTest {
        // When
        setContent {
            EndpointContextBadges(
                endpoint = "localhost:8080",
                context = "application"
            )
        }

        // Then
        onNodeWithText("localhost:8080").assertIsDisplayed()
    }

    @Test
    fun `EndpointContextBadges should display context badge`() = runComposeUiTest {
        // When
        setContent {
            EndpointContextBadges(
                endpoint = "localhost:8080",
                context = "application"
            )
        }

        // Then
        onNodeWithText("application").assertIsDisplayed()
    }

    @Test
    fun `EndpointContextBadges should display both badges`() = runComposeUiTest {
        // When
        setContent {
            EndpointContextBadges(
                endpoint = "server1:9090",
                context = "myContext"
            )
        }

        // Then
        onNodeWithText("server1:9090").assertIsDisplayed()
        onNodeWithText("myContext").assertIsDisplayed()
    }

    @Test
    fun `StatusBadge should display UP status`() = runComposeUiTest {
        // When
        setContent {
            StatusBadge(status = "UP")
        }

        // Then
        onNodeWithText("UP").assertIsDisplayed()
    }

    @Test
    fun `StatusBadge should display DOWN status`() = runComposeUiTest {
        // When
        setContent {
            StatusBadge(status = "DOWN")
        }

        // Then
        onNodeWithText("DOWN").assertIsDisplayed()
    }

    @Test
    fun `StatusBadge should display OUT_OF_SERVICE status`() = runComposeUiTest {
        // When
        setContent {
            StatusBadge(status = "OUT_OF_SERVICE")
        }

        // Then
        onNodeWithText("OUT_OF_SERVICE").assertIsDisplayed()
    }

    @Test
    fun `StatusBadge should display unknown status`() = runComposeUiTest {
        // When
        setContent {
            StatusBadge(status = "UNKNOWN")
        }

        // Then
        onNodeWithText("UNKNOWN").assertIsDisplayed()
    }

    @Test
    fun `getStatusColor should return green for UP status`() {
        // When
        val color = getStatusColor("UP")

        // Then
        assertEquals(Color(0xFF2E7D32), color)
    }

    @Test
    fun `getStatusColor should return green for lowercase up status`() {
        // When
        val color = getStatusColor("up")

        // Then
        assertEquals(Color(0xFF2E7D32), color)
    }

    @Test
    fun `getStatusColor should return red for DOWN status`() {
        // When
        val color = getStatusColor("DOWN")

        // Then
        assertEquals(Color(0xFFD32F2F), color)
    }

    @Test
    fun `getStatusColor should return red for lowercase down status`() {
        // When
        val color = getStatusColor("down")

        // Then
        assertEquals(Color(0xFFD32F2F), color)
    }

    @Test
    fun `getStatusColor should return orange for OUT_OF_SERVICE status`() {
        // When
        val color = getStatusColor("OUT_OF_SERVICE")

        // Then
        assertEquals(Color(0xFFED6C02), color)
    }

    @Test
    fun `getStatusColor should return orange for lowercase out_of_service status`() {
        // When
        val color = getStatusColor("out_of_service")

        // Then
        assertEquals(Color(0xFFED6C02), color)
    }

    @Test
    fun `getStatusColor should return gray for unknown status`() {
        // When
        val color = getStatusColor("UNKNOWN")

        // Then
        assertEquals(Color.Gray, color)
    }

    @Test
    fun `getStatusColor should return gray for empty status`() {
        // When
        val color = getStatusColor("")

        // Then
        assertEquals(Color.Gray, color)
    }

    @Test
    fun `getStatusColor should return gray for arbitrary status`() {
        // When
        val color = getStatusColor("MAINTENANCE")

        // Then
        assertEquals(Color.Gray, color)
    }
}
