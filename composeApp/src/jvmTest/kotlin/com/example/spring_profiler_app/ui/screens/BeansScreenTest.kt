package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.AggregatedBeansResponse
import com.example.spring_profiler_app.data.Bean
import com.example.spring_profiler_app.data.Beans
import com.example.spring_profiler_app.data.UIState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BeansScreenTest {

    @Test
    fun `BeansScreen should display loading state`() = runComposeUiTest {
        // Given
        val beansState: UIState<AggregatedBeansResponse> = UIState.Loading

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading beans...").assertExists()
    }

    @Test
    fun `BeansScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch beans data"
        val beansState: UIState<AggregatedBeansResponse> = UIState.Error(errorMessage)

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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val beansResponse = AggregatedBeansResponse(endpoints = emptyList())
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("No beans match your filters.").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display empty state when search has no results`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("testBean" to bean))
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        onNodeWithText("Search beans by name...").performClick()
        onNodeWithText("Search beans by name...").performTextInput("nonexistent")
        waitForIdle()

        // Then
        onNodeWithText("No beans match your filters.").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display bean information`() = runComposeUiTest {
        // Given
        val bean = Bean(dependencies = emptyList(), scope = "singleton")
        val beans = Beans(mapOf("com.example.MyBean" to bean))
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
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
        val endpointBeans = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans1, "management" to beans2)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpointBeans))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("appBean").assertIsDisplayed()
        onNodeWithText("mgmtBean").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should display beans from multiple endpoints`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val beans1 = Beans(mapOf("bean1" to bean1))
        val beans2 = Beans(mapOf("bean2" to bean2))
        val endpoint1 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans1)
        )
        val endpoint2 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8081",
            contexts = mapOf("application" to beans2)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpoint1, endpoint2))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        // Then
        onNodeWithText("bean1").assertIsDisplayed()
        onNodeWithText("bean2").assertIsDisplayed()
        onNodeWithText("localhost:8080").assertIsDisplayed()
        onNodeWithText("localhost:8081").assertIsDisplayed()
    }

    @Test
    fun `BeansScreen should filter beans by endpoint`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val beans1 = Beans(mapOf("com.example.bean1" to bean1))
        val beans2 = Beans(mapOf("com.example.bean2" to bean2))
        val endpoint1 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to beans1)
        )
        val endpoint2 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8081",
            contexts = mapOf("application" to beans2)
        )
        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpoint1, endpoint2))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        waitForIdle()

        onNodeWithText("localhost:8080").assertIsDisplayed()
        onNodeWithText("localhost:8081").assertIsDisplayed()

        try {
            val expandButtons = onAllNodesWithContentDescription("Expand")
            if (expandButtons.fetchSemanticsNodes().isNotEmpty()) {
                expandButtons[0].performClick()
                waitForIdle()
            }
        } catch (_: Exception) {
            // If we can't find the expand button, the filters might already be expanded
            // or the test environment might be different
        }

        onAllNodesWithText("localhost:8080")[1].performClick()
        waitForIdle()

        // Then
        onNodeWithText("bean1").assertIsDisplayed()
        onNodeWithText("bean2").assertDoesNotExist()
    }

    @Test
    fun `BeansScreen should display beans from three different endpoints`() = runComposeUiTest {
        // Given
        val bean1 = Bean(dependencies = emptyList(), scope = "singleton")
        val bean2 = Bean(dependencies = emptyList(), scope = "prototype")
        val bean3 = Bean(dependencies = emptyList(), scope = "singleton")

        val endpoint1 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to Beans(mapOf("com.example.bean1" to bean1)))
        )
        val endpoint2 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8081",
            contexts = mapOf("application" to Beans(mapOf("com.example.bean2" to bean2)))
        )
        val endpoint3 = AggregatedBeansResponse.EndpointBeans(
            endpoint = "localhost:8082",
            contexts = mapOf("application" to Beans(mapOf("com.example.bean3" to bean3)))
        )

        val beansResponse = AggregatedBeansResponse(endpoints = listOf(endpoint1, endpoint2, endpoint3))
        val beansState = UIState.Success(beansResponse)

        // When
        setContent {
            BeansScreen(beansState = beansState)
        }

        waitForIdle()

        // Then
        onNodeWithText("localhost:8080").assertIsDisplayed()
        onNodeWithText("localhost:8081").assertIsDisplayed()
        onNodeWithText("localhost:8082").assertIsDisplayed()

        onNodeWithText("bean1").assertIsDisplayed()
        onNodeWithText("bean2").assertIsDisplayed()
        onNodeWithText("bean3").assertIsDisplayed()
    }
}
