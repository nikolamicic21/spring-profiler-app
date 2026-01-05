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
}
