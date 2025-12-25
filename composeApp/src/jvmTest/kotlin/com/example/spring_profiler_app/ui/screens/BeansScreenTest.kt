package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
    fun `BeansScreen should display search field`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("testBean" to bean))
        val beansResponse = BeansResponse(mapOf("application" to beans))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Search beans by name...").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display empty state when no beans`() = runComposeUiTest {
        // Given
        val beansResponse = BeansResponse(emptyMap())
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("No bean names match your search.").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display empty state when search has no results`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("testBean" to bean))
        val beansResponse = BeansResponse(mapOf("application" to beans))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        onNodeWithText("Search beans by name...").performClick()
        onNodeWithText("Search beans by name...").performTextInput("nonexistent")
        waitForIdle()

        // Then
        onNodeWithText("No bean names match your search.").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display bean information`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("com.example.MyBean" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("MyBean").assertIsDisplayed()
        onNodeWithText("SINGLETON").assertIsDisplayed()
        onNodeWithText("DEPENDENCIES (0)").assertIsDisplayed()
        onNodeWithText("No dependencies").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display bean with dependencies`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = listOf("dependency1", "dependency2"), scope = "singleton")
        val beans = Beans(mapOf("com.example.MyBean" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("MyBean").assertIsDisplayed()
        onNodeWithText("DEPENDENCIES (2)").assertIsDisplayed()
        onNodeWithText("dependency1").assertIsDisplayed()
        onNodeWithText("dependency2").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display multiple beans`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val beans = Beans(mapOf("com.example.Bean1" to bean1, "com.example.Bean2" to bean2))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("Bean1").assertIsDisplayed()
        onNodeWithText("SINGLETON").assertIsDisplayed()
        onNodeWithText("Bean2").assertIsDisplayed()
        onNodeWithText("PROTOTYPE").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should filter beans by search query`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val beans = Beans(mapOf("userService" to bean1, "orderService" to bean2))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        onNodeWithText("Search beans by name...").performClick()
        onNodeWithText("Search beans by name...").performTextInput("user")
        waitForIdle()

        // Then
        onNodeWithText("userService").assertIsDisplayed()
        onNodeWithText("orderService").assertDoesNotExist()
    }

    @Test
    fun `BeansScreen should clear search query when clear button is clicked`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("testBean" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        onNodeWithText("Search beans by name...").performClick()
        onNodeWithText("Search beans by name...").performTextInput("test")
        waitForIdle()
        onNodeWithContentDescription("Clear").performClick()
        waitForIdle()

        // Then
        onNodeWithText("testBean").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display bean with full package name`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("com.example.service.UserService" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("UserService").assertIsDisplayed()
        onNodeWithText("com.example.service").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should handle bean with no package`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("SimpleBean" to bean))
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("SimpleBean").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display different scope types`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val bean3 = Bean(dependencies = emptyList(), scope = "request")
        val beans = Beans(
            mapOf(
                "singletonBean" to bean1,
                "prototypeBean" to bean2,
                "requestBean" to bean3
            )
        )
        val contexts = mapOf("application" to beans)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("SINGLETON").assertIsDisplayed()
        onNodeWithText("PROTOTYPE").assertIsDisplayed()
        onNodeWithText("REQUEST").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should handle beans from multiple contexts`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val beans1 = Beans(mapOf("appBean" to bean1))
        val beans2 = Beans(mapOf("mgmtBean" to bean2))
        val contexts = mapOf("application" to beans1, "management" to beans2)
        val beansResponse = BeansResponse(contexts)
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("appBean").assertIsDisplayed()
        onNodeWithText("mgmtBean").assertIsDisplayed()
    }
}
