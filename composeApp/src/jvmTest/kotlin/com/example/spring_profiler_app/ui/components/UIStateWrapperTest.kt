package com.example.spring_profiler_app.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class UIStateWrapperTest {

    @Test
    fun `UIStateWrapper should display loading text when state is Loading`() = runComposeUiTest {
        // Given
        val loadingState: UIState<String> = UIState.Loading

        // When
        setContent {
            UIStateWrapper(state = loadingState) { data ->
                Text(text = data)
            }
        }

        // Then - Wait for debounce delay
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading...").assertExists()
    }

    @Test
    fun `UIStateWrapper should display error message when state is Error`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to load data"
        val errorState: UIState<String> = UIState.Error(errorMessage)

        // When
        setContent {
            UIStateWrapper(state = errorState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should display content when state is Success`() = runComposeUiTest {
        // Given
        val successData = "Success data loaded"
        val successState = UIState.Success(successData)

        // When
        setContent {
            UIStateWrapper(state = successState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText(successData).assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should render custom content for Success state`() = runComposeUiTest {
        // Given
        data class TestData(val title: String, val value: Int)

        val testData = TestData("Test Title", 42)
        val successState = UIState.Success(testData)

        // When
        setContent {
            UIStateWrapper(state = successState) { data ->
                Text(text = "${data.title}: ${data.value}")
            }
        }

        // Then
        onNodeWithText("Test Title: 42").assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should not display content when state is Loading`() = runComposeUiTest {
        // Given
        val loadingState: UIState<String> = UIState.Loading

        // When
        setContent {
            UIStateWrapper(state = loadingState) { _ ->
                Text(text = "This should not be visible")
            }
        }

        // Then - Wait for debounce delay
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading...").assertExists()
        onNodeWithText("This should not be visible").assertDoesNotExist()
    }

    @Test
    fun `UIStateWrapper should not display content when state is Error`() = runComposeUiTest {
        // Given
        val errorState: UIState<String> = UIState.Error("Error occurred")

        // When
        setContent {
            UIStateWrapper(state = errorState) { _ ->
                Text(text = "This should not be visible")
            }
        }

        // Then
        onNodeWithText("Error occurred").assertIsDisplayed()
        onNodeWithText("This should not be visible").assertDoesNotExist()
    }

    @Test
    fun `UIStateWrapper should display custom loading message`() = runComposeUiTest {
        // Given
        val loadingState: UIState<String> = UIState.Loading

        // When
        setContent {
            UIStateWrapper(
                state = loadingState,
                loadingMessage = "Loading health status..."
            ) { data ->
                Text(text = data)
            }
        }

        // Then
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading health status...").assertExists()
    }

    @Test
    fun `UIStateWrapper should display error with auto-refresh info when autoRefreshInterval is provided`() =
        runComposeUiTest {
            // Given
            val errorState: UIState<String> = UIState.Error("Connection failed")

            // When
            setContent {
                UIStateWrapper(
                    state = errorState,
                    autoRefreshInterval = kotlin.time.Duration.parse("5s")
                ) { data ->
                    Text(text = data)
                }
            }

            // Then
            onNodeWithText("Something went wrong").assertIsDisplayed()
            onNodeWithText("Connection failed").assertIsDisplayed()
            onNodeWithText("Retrying in 5s...").assertIsDisplayed()
        }

    @Test
    fun `UIStateWrapper should display error without auto-refresh info when autoRefreshInterval is null`() =
        runComposeUiTest {
            // Given
            val errorState: UIState<String> = UIState.Error("Connection failed")

            // When
            setContent {
                UIStateWrapper(
                    state = errorState,
                    autoRefreshInterval = null
                ) { data ->
                    Text(text = data)
                }
            }

            // Then
            onNodeWithText("Something went wrong").assertIsDisplayed()
            onNodeWithText("Connection failed").assertIsDisplayed()
            onNodeWithText("Retrying in 5s...").assertDoesNotExist()
        }

    @Test
    fun `UIStateWrapper should display enhanced error UI with icon`() = runComposeUiTest {
        // Given
        val errorState: UIState<String> = UIState.Error("Network error")

        // When
        setContent {
            UIStateWrapper(state = errorState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText("Something went wrong").assertIsDisplayed()
        onNodeWithText("Network error").assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should display content and warning for PartialSuccess state`() = runComposeUiTest {
        // Given
        val successData = "Partial data loaded"
        val warnings = listOf("localhost:8080 - Connection failed", "localhost:8081 - Timeout")
        val partialSuccessState = UIState.PartialSuccess(data = successData, warnings = warnings)

        // When
        setContent {
            UIStateWrapper(state = partialSuccessState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText(successData).assertIsDisplayed()
        onNodeWithText("Partial Data Available").assertIsDisplayed()
        onNodeWithText("• localhost:8080 - Connection failed").assertIsDisplayed()
        onNodeWithText("• localhost:8081 - Timeout").assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should display all warnings for PartialSuccess state`() = runComposeUiTest {
        // Given
        data class TestData(val value: String)

        val testData = TestData("Test Value")
        val warnings = listOf(
            "localhost:8080 - Error 1",
            "localhost:8081 - Error 2",
            "localhost:8082 - Still loading..."
        )
        val partialSuccessState = UIState.PartialSuccess(data = testData, warnings = warnings)

        // When
        setContent {
            UIStateWrapper(state = partialSuccessState) { data ->
                Text(text = data.value)
            }
        }

        // Then
        onNodeWithText("Test Value").assertIsDisplayed()
        onNodeWithText("Partial Data Available").assertIsDisplayed()
        onNodeWithText("• localhost:8080 - Error 1").assertIsDisplayed()
        onNodeWithText("• localhost:8081 - Error 2").assertIsDisplayed()
        onNodeWithText("• localhost:8082 - Still loading...").assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should display PartialDataWarning component for PartialSuccess state`() = runComposeUiTest {
        // Given
        val partialSuccessState = UIState.PartialSuccess(
            data = "Data",
            warnings = listOf("Warning message")
        )

        // When
        setContent {
            UIStateWrapper(state = partialSuccessState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText("Partial Data Available").assertIsDisplayed()
        onNodeWithText("Some endpoints failed or are still loading:").assertIsDisplayed()
        onNodeWithText("• Warning message").assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should render custom content for PartialSuccess state`() = runComposeUiTest {
        // Given
        data class ComplexData(val title: String, val count: Int)

        val complexData = ComplexData("Test", 123)
        val partialSuccessState = UIState.PartialSuccess(
            data = complexData,
            warnings = listOf("localhost:8080 - Failed")
        )

        // When
        setContent {
            UIStateWrapper(state = partialSuccessState) { data ->
                Text(text = "${data.title}: ${data.count}")
            }
        }

        // Then
        onNodeWithText("Test: 123").assertIsDisplayed()
        onNodeWithText("Partial Data Available").assertIsDisplayed()
        onNodeWithText("• localhost:8080 - Failed").assertIsDisplayed()
    }

    @Test
    fun `UIStateWrapper should handle PartialSuccess with empty warnings list`() = runComposeUiTest {
        // Given
        val partialSuccessState = UIState.PartialSuccess(
            data = "Data",
            warnings = emptyList()
        )

        // When
        setContent {
            UIStateWrapper(state = partialSuccessState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText("Data").assertIsDisplayed()
        onNodeWithText("Partial Data Available").assertIsDisplayed()
    }
}
