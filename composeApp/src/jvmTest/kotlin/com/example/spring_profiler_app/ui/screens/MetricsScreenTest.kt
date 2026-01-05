package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.Measurement
import com.example.spring_profiler_app.data.Metric
import com.example.spring_profiler_app.data.MetricsResponse
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MetricsScreenTest {

    @Test
    fun `MetricsScreen should display loading state`() = runComposeUiTest {
        // Given
        val metricsState: UIState<MetricsResponse> = UIState.Loading

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading metrics...").assertExists()
    }

    @Test
    fun `MetricsScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch metrics data"
        val metricsState: UIState<MetricsResponse> = UIState.Error(errorMessage)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should display metric name`() = runComposeUiTest {
        // Given
        val metric = Metric(
            name = "jvm.memory.used",
            measurements = emptyList(),
            unit = null
        )
        val metricsResponse = MetricsResponse(listOf(metric))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("JVM").assertIsDisplayed()
        onNodeWithText("memory.used").assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should display metric with unit`() = runComposeUiTest {
        // Given
        val metric = Metric(
            name = "jvm.memory.used",
            measurements = emptyList(),
            unit = "bytes"
        )
        val metricsResponse = MetricsResponse(listOf(metric))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("JVM").assertIsDisplayed()
        onNodeWithText("memory.used").assertIsDisplayed()
        onNodeWithText("bytes").assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should display metric with measurements`() = runComposeUiTest {
        // Given
        val measurements = listOf(
            Measurement("VALUE", 1234567.89)
        )
        val metric = Metric(
            name = "jvm.memory.used",
            measurements = measurements,
            unit = "bytes"
        )
        val metricsResponse = MetricsResponse(listOf(metric))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("JVM").assertIsDisplayed()
        onNodeWithText("memory.used").assertIsDisplayed()
        onNodeWithText("VALUE").assertIsDisplayed()
        onNodeWithText("1234567.89").assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should display multiple measurements`() = runComposeUiTest {
        // Given
        val measurements = listOf(
            Measurement("COUNT", 100.0),
            Measurement("TOTAL_TIME", 5000.0),
            Measurement("MAX", 250.0)
        )
        val metric = Metric(
            name = "http.server.requests",
            measurements = measurements,
            unit = "seconds"
        )
        val metricsResponse = MetricsResponse(listOf(metric))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("HTTP").assertIsDisplayed()
        onNodeWithText("server.requests").assertIsDisplayed()
        onNodeWithText("COUNT").assertIsDisplayed()
        onNodeWithText("100").assertIsDisplayed()
        onNodeWithText("TOTAL_TIME").assertIsDisplayed()
        onNodeWithText("5000").assertIsDisplayed()
        onNodeWithText("MAX").assertIsDisplayed()
        onNodeWithText("250").assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should display multiple metrics`() = runComposeUiTest {
        // Given
        val metric1 = Metric(
            name = "jvm.memory.used",
            measurements = listOf(Measurement("VALUE", 1000000.0)),
            unit = "bytes"
        )
        val metric2 = Metric(
            name = "system.cpu.usage",
            measurements = listOf(Measurement("VALUE", 0.5)),
            unit = null
        )
        val metricsResponse = MetricsResponse(listOf(metric1, metric2))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("JVM").assertIsDisplayed()
        onNodeWithText("memory.used").assertIsDisplayed()
        onNodeWithText("SYSTEM").assertIsDisplayed()
        onNodeWithText("cpu.usage").assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should group metrics by prefix`() = runComposeUiTest {
        // Given
        val metric1 = Metric(
            name = "jvm.memory.used",
            measurements = listOf(Measurement("VALUE", 1000000.0)),
            unit = "bytes"
        )
        val metric2 = Metric(
            name = "jvm.threads.live",
            measurements = listOf(Measurement("VALUE", 50.0)),
            unit = null
        )
        val metricsResponse = MetricsResponse(listOf(metric1, metric2))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("JVM").assertIsDisplayed()
        onNodeWithText("memory.used").assertIsDisplayed()
        onNodeWithText("threads.live").assertIsDisplayed()
    }

    @Test
    fun `MetricsScreen should display metric without unit`() = runComposeUiTest {
        // Given
        val metric = Metric(
            name = "system.cpu.count",
            measurements = listOf(Measurement("VALUE", 8.0)),
            unit = null
        )
        val metricsResponse = MetricsResponse(listOf(metric))
        val metricsState = UIState.Success(metricsResponse)

        // When
        setContent {
            MetricsScreen(
                metricsState = metricsState,
                refreshMetricsCallback = {}
            )
        }

        // Then
        onNodeWithText("SYSTEM").assertIsDisplayed()
        onNodeWithText("cpu.count").assertIsDisplayed()
        onNodeWithText("VALUE").assertIsDisplayed()
        onNodeWithText("8").assertIsDisplayed()
    }
}
