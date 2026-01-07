package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.AggregatedConfigPropsResponse
import com.example.spring_profiler_app.data.BeanProperties
import com.example.spring_profiler_app.data.Context
import com.example.spring_profiler_app.data.UIState
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ConfigPropsScreenTest {

    @Test
    fun `ConfigPropsScreen should display loading state`() = runComposeUiTest {
        // Given
        val configPropsState: UIState<AggregatedConfigPropsResponse> = UIState.Loading

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        waitForIdle()
        mainClock.advanceTimeBy(100)
        onNodeWithText("Loading configuration properties...").assertExists()
    }

    @Test
    fun `ConfigPropsScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch configuration properties"
        val configPropsState: UIState<AggregatedConfigPropsResponse> = UIState.Error(errorMessage)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display search field`() = runComposeUiTest {
        // Given
        val properties = JsonObject(mapOf("port" to JsonPrimitive(8080)))
        val beanProperties = BeanProperties(prefix = "server", properties = properties)
        val context = Context(beans = mapOf("serverProperties" to beanProperties))
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("Search prefixes (e.g., server, datasource)...").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display empty state when no properties`() = runComposeUiTest {
        // Given
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = emptyList())
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("No configuration properties match your filters.").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display configuration property`() = runComposeUiTest {
        // Given
        val properties = JsonObject(
            mapOf("port" to JsonPrimitive(8080))
        )
        val beanProperties = BeanProperties(
            prefix = "server",
            properties = properties
        )
        val context = Context(
            beans = mapOf("serverProperties" to beanProperties)
        )
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server").assertIsDisplayed()
        onNodeWithText("port").assertIsDisplayed()
        onNodeWithText("8080").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display multiple properties`() = runComposeUiTest {
        // Given
        val properties = JsonObject(
            mapOf(
                "port" to JsonPrimitive(8080),
                "address" to JsonPrimitive("localhost")
            )
        )
        val beanProperties = BeanProperties(
            prefix = "server",
            properties = properties
        )
        val context = Context(
            beans = mapOf("serverProperties" to beanProperties)
        )
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server").assertIsDisplayed()
        onNodeWithText("port").assertIsDisplayed()
        onNodeWithText("8080").assertIsDisplayed()
        onNodeWithText("address").assertIsDisplayed()
        onNodeWithText("localhost").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display nested properties`() = runComposeUiTest {
        // Given
        val nestedProperties = JsonObject(
            mapOf(
                "max-threads" to JsonPrimitive(200)
            )
        )
        val properties = JsonObject(
            mapOf(
                "port" to JsonPrimitive(8080),
                "tomcat" to nestedProperties
            )
        )
        val beanProperties = BeanProperties(
            prefix = "server",
            properties = properties
        )
        val context = Context(
            beans = mapOf("serverProperties" to beanProperties)
        )
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server").assertIsDisplayed()
        onNodeWithText("port").assertIsDisplayed()
        onNodeWithText("8080").assertIsDisplayed()
        onNodeWithText("tomcat.max-threads").assertIsDisplayed()
        onNodeWithText("200").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display properties from multiple beans`() = runComposeUiTest {
        // Given
        val serverProperties = JsonObject(
            mapOf("port" to JsonPrimitive(8080))
        )
        val managementProperties = JsonObject(
            mapOf("port" to JsonPrimitive(9090))
        )
        val serverBean = BeanProperties(prefix = "server", properties = serverProperties)
        val managementBean = BeanProperties(prefix = "management", properties = managementProperties)
        val context = Context(
            beans = mapOf(
                "serverProperties" to serverBean,
                "managementProperties" to managementBean
            )
        )
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server").assertIsDisplayed()
        onNodeWithText("management").assertIsDisplayed()
        onNodeWithText("8080").assertIsDisplayed()
        onNodeWithText("9090").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should filter properties by search query`() = runComposeUiTest {
        // Given
        val serverProperties = JsonObject(mapOf("port" to JsonPrimitive(8080)))
        val datasourceProperties = JsonObject(mapOf("url" to JsonPrimitive("jdbc:mysql://localhost")))
        val serverBean = BeanProperties(prefix = "server", properties = serverProperties)
        val datasourceBean = BeanProperties(prefix = "datasource", properties = datasourceProperties)
        val context = Context(
            beans = mapOf(
                "serverProperties" to serverBean,
                "datasourceProperties" to datasourceBean
            )
        )
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        onNodeWithText("Search prefixes (e.g., server, datasource)...").performClick()
        onNodeWithText("Search prefixes (e.g., server, datasource)...").performTextInput("server")
        waitForIdle()

        // Then
        onNodeWithText("8080").assertIsDisplayed()
        onNodeWithText("jdbc:mysql://localhost").assertDoesNotExist()
    }

    @Test
    fun `ConfigPropsScreen should clear search query when clear button is clicked`() = runComposeUiTest {
        // Given
        val properties = JsonObject(mapOf("port" to JsonPrimitive(8080)))
        val beanProperties = BeanProperties(prefix = "server", properties = properties)
        val context = Context(beans = mapOf("serverProperties" to beanProperties))
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        onNodeWithText("Search prefixes (e.g., server, datasource)...").performClick()
        onNodeWithText("Search prefixes (e.g., server, datasource)...").performTextInput("test")
        waitForIdle()
        onNodeWithContentDescription("Clear").performClick()
        waitForIdle()

        // Then
        onNodeWithText("8080").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display empty state when search has no results`() = runComposeUiTest {
        // Given
        val properties = JsonObject(mapOf("port" to JsonPrimitive(8080)))
        val beanProperties = BeanProperties(prefix = "server", properties = properties)
        val context = Context(beans = mapOf("serverProperties" to beanProperties))
        val endpointConfigProps = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "localhost:8080",
            contexts = mapOf("application" to context)
        )
        val configPropsResponse = AggregatedConfigPropsResponse(endpoints = listOf(endpointConfigProps))
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        onNodeWithText("Search prefixes (e.g., server, datasource)...").performClick()
        onNodeWithText("Search prefixes (e.g., server, datasource)...").performTextInput("nonexistent")
        waitForIdle()

        // Then
        onNodeWithText("No configuration properties match your filters.").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display config props from multiple endpoints`() = runComposeUiTest {
        // Given
        val endpoint1 = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "prod-server-1",
            contexts = mapOf(
                "application" to Context(
                    beans = mapOf(
                        "serverProperties" to BeanProperties(
                            prefix = "server",
                            properties = JsonObject(
                                mapOf(
                                    "port" to JsonPrimitive(8080),
                                    "address" to JsonPrimitive("0.0.0.0")
                                )
                            )
                        )
                    )
                )
            )
        )
        val endpoint2 = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "prod-server-2",
            contexts = mapOf(
                "application" to Context(
                    beans = mapOf(
                        "serverProperties" to BeanProperties(
                            prefix = "server",
                            properties = JsonObject(
                                mapOf(
                                    "port" to JsonPrimitive(8081),
                                    "address" to JsonPrimitive("0.0.0.0")
                                )
                            )
                        )
                    )
                )
            )
        )
        val endpoint3 = AggregatedConfigPropsResponse.EndpointConfigProps(
            endpoint = "staging-server",
            contexts = mapOf(
                "application" to Context(
                    beans = mapOf(
                        "serverProperties" to BeanProperties(
                            prefix = "server",
                            properties = JsonObject(
                                mapOf(
                                    "port" to JsonPrimitive(9000)
                                )
                            )
                        )
                    )
                )
            )
        )

        val configPropsResponse = AggregatedConfigPropsResponse(
            endpoints = listOf(endpoint1, endpoint2, endpoint3)
        )
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        waitForIdle()

        // Then
        onNodeWithText("prod-server-1").assertIsDisplayed()
        onNodeWithText("prod-server-2").assertIsDisplayed()
        onNodeWithText("staging-server").assertIsDisplayed()
    }
}
