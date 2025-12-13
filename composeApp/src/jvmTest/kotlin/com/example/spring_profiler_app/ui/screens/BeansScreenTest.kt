package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.Bean
import com.example.spring_profiler_app.data.Beans
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BeansScreenTest {

    @Test
    fun `BeansScreen should display loading state`() = runComposeUiTest {
        // Given
        val beansState: UIState<BeansResponse> = UIState.Loading

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch beans data"
        val beansState: UIState<BeansResponse> = UIState.Error(errorMessage)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display active contexts`() = runComposeUiTest {
        // Given
        val contexts = mapOf(
            "application" to Beans(emptyMap())
        )
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Active contexts: application").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display multiple active contexts`() = runComposeUiTest {
        // Given
        val contexts = mapOf(
            "application" to Beans(emptyMap()),
            "management" to Beans(emptyMap())
        )
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Active contexts: application, management").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display search field`() = runComposeUiTest {
        // Given
        val beansResponse = BeansResponse(emptyMap())
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Search for Bean by name").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display bean information`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("myBean" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Bean name: myBean").assertIsDisplayed()
        onNodeWithText("Scope: singleton").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display bean with dependencies`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = listOf("dependency1", "dependency2"), scope = "singleton")
        val beans = Beans(mapOf("myBean" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Bean name: myBean").assertIsDisplayed()
        onNodeWithText("Dependencies:").assertIsDisplayed()
        onNodeWithText("dependency1").assertIsDisplayed()
        onNodeWithText("dependency2").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display multiple beans`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val beans = Beans(mapOf("bean1" to bean1, "bean2" to bean2))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Bean name: bean1").assertIsDisplayed()
        onNodeWithText("Scope: singleton").assertIsDisplayed()
        onNodeWithText("Bean name: bean2").assertIsDisplayed()
        onNodeWithText("Scope: prototype").assertIsDisplayed()
    }
}
