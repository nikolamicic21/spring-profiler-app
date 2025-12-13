package com.example.spring_profiler_app.ui.components

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ApiStateWrapperTest {

    @Test
    fun `ApiStateWrapper should display loading text when state is Loading`() = runComposeUiTest {
        // Given
        val loadingState: UIState<String> = UIState.Loading

        // When
        setContent {
            UIStateWrapper(state = loadingState) { data ->
                Text(text = data)
            }
        }

        // Then
        onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun `ApiStateWrapper should display error message when state is Error`() = runComposeUiTest {
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
    fun `ApiStateWrapper should display content when state is Success`() = runComposeUiTest {
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
    fun `ApiStateWrapper should render custom content for Success state`() = runComposeUiTest {
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
    fun `ApiStateWrapper should not display content when state is Loading`() = runComposeUiTest {
        // Given
        val loadingState: UIState<String> = UIState.Loading

        // When
        setContent {
            UIStateWrapper(state = loadingState) { _ ->
                Text(text = "This should not be visible")
            }
        }

        // Then
        onNodeWithText("Loading...").assertIsDisplayed()
        onNodeWithText("This should not be visible").assertDoesNotExist()
    }

    @Test
    fun `ApiStateWrapper should not display content when state is Error`() = runComposeUiTest {
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
}
