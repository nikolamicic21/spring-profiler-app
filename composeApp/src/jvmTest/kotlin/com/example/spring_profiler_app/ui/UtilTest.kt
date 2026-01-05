package com.example.spring_profiler_app.ui

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class UtilTest {

    @Test
    fun `rememberDebouncedCallback should invoke callback on first click`() = runComposeUiTest {
        // Given
        var callCount = 0
        var lastValue: String? = null

        // When
        setContent {
            val debouncedCallback = rememberDebouncedCallback<String> { value ->
                callCount++
                lastValue = value
            }

            Button(onClick = { debouncedCallback("test") }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        waitForIdle()

        assertEquals(1, callCount, "Callback should be invoked once on first click")
        assertEquals("test", lastValue, "Callback should receive correct value")
    }

    @Test
    fun `rememberDebouncedCallback should ignore rapid successive clicks`() = runComposeUiTest {
        // Given
        var callCount = 0

        // When
        setContent {
            val debouncedCallback = rememberDebouncedCallback<String>(
                debounceInterval = 500L
            ) { _ ->
                callCount++
            }

            Button(onClick = { debouncedCallback("test") }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        onNodeWithText("Click me").performClick()
        onNodeWithText("Click me").performClick()
        waitForIdle()

        assertEquals(1, callCount, "Callback should only be invoked once for rapid clicks")
    }

    @Test
    fun `rememberDebouncedCallback should allow clicks after debounce interval`() = runComposeUiTest {
        // Given
        var callCount = 0

        // When
        setContent {
            val debouncedCallback = rememberDebouncedCallback<String>(
                debounceInterval = 100L
            ) { _ ->
                callCount++
            }

            Button(onClick = { debouncedCallback("test") }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(1, callCount, "First click should be counted")

        Thread.sleep(150)

        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(2, callCount, "Second click after debounce interval should be counted")
    }

    @Test
    fun `rememberDebouncedCallback should handle different parameter values`() = runComposeUiTest {
        // Given
        val receivedValues = mutableListOf<Int>()

        // When
        setContent {
            var counter by remember { mutableStateOf(0) }
            val debouncedCallback = rememberDebouncedCallback<Int>(
                debounceInterval = 100L
            ) { value ->
                receivedValues.add(value)
            }

            Button(onClick = {
                debouncedCallback(counter)
                counter++
            }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(listOf(0), receivedValues, "First click should pass value 0")

        Thread.sleep(150)

        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(listOf(0, 1), receivedValues, "Second click should pass value 1")
    }

    @Test
    fun `rememberDebouncedCallback should use custom debounce interval`() = runComposeUiTest {
        // Given
        var callCount = 0

        // When
        setContent {
            val debouncedCallback = rememberDebouncedCallback<String>(
                debounceInterval = 300L
            ) { _ ->
                callCount++
            }

            Button(onClick = { debouncedCallback("test") }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(1, callCount, "First click should be counted")

        onNodeWithText("Click me").performClick()
        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(1, callCount, "Rapid clicks should be debounced")

        Thread.sleep(350)
        onNodeWithText("Click me").performClick()
        waitForIdle()
        assertEquals(2, callCount, "Click after 300ms interval should be counted")
    }

    @Test
    fun `rememberDebouncedCallback should maintain state across recompositions`() = runComposeUiTest {
        // Given
        var callCount = 0

        // When
        setContent {
            var recomposeKey by remember { mutableStateOf(0) }
            val debouncedCallback = rememberDebouncedCallback<String>(
                debounceInterval = 100L
            ) { _ ->
                callCount++
            }

            Button(onClick = {
                debouncedCallback("test")
                recomposeKey++
            }) {
                Text("Click me $recomposeKey")
            }
        }

        // Then
        onNodeWithText("Click me 0").performClick()
        waitForIdle()
        assertEquals(1, callCount, "First click should be counted")

        onNodeWithText("Click me 1").performClick()
        waitForIdle()
        assertEquals(1, callCount, "Rapid click should be debounced despite recomposition")
    }

    @Test
    fun `rememberDebouncedCallback should work with complex types`() = runComposeUiTest {
        // Given
        data class TestData(val id: Int, val name: String)

        var receivedData: TestData? = null

        // When
        setContent {
            val debouncedCallback = rememberDebouncedCallback<TestData> { data ->
                receivedData = data
            }

            Button(onClick = { debouncedCallback(TestData(1, "test")) }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        waitForIdle()

        assertEquals(TestData(1, "test"), receivedData, "Callback should receive complex type")
    }

    @Test
    fun `rememberDebouncedCallback should use default debounce interval when not specified`() = runComposeUiTest {
        // Given
        var callCount = 0

        // When
        setContent {
            val debouncedCallback = rememberDebouncedCallback<String> { _ ->
                callCount++
            }

            Button(onClick = { debouncedCallback("test") }) {
                Text("Click me")
            }
        }

        // Then
        onNodeWithText("Click me").performClick()
        onNodeWithText("Click me").performClick()
        waitForIdle()

        assertEquals(1, callCount, "Should use default 500ms interval and debounce rapid clicks")
    }
}
