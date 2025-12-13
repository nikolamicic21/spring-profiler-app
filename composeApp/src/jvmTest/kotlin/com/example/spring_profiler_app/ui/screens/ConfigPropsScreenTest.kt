package com.example.spring_profiler_app.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.spring_profiler_app.data.BeanProperties
import com.example.spring_profiler_app.data.ConfigPropsResponse
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
        val configPropsState: UIState<ConfigPropsResponse> = UIState.Loading

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("Loading...").assertIsDisplayed()
    }

    @Test
    fun `ConfigPropsScreen should display error state`() = runComposeUiTest {
        // Given
        val errorMessage = "Failed to fetch configuration properties"
        val configPropsState: UIState<ConfigPropsResponse> = UIState.Error(errorMessage)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
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
        val configPropsResponse = ConfigPropsResponse(
            contexts = mapOf("application" to context)
        )
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server.port: 8080").assertIsDisplayed()
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
        val configPropsResponse = ConfigPropsResponse(
            contexts = mapOf("application" to context)
        )
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server.port: 8080").assertIsDisplayed()
        onNodeWithText("server.address: localhost").assertIsDisplayed()
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
        val configPropsResponse = ConfigPropsResponse(
            contexts = mapOf("application" to context)
        )
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server.port: 8080").assertIsDisplayed()
        onNodeWithText("server.tomcat.max-threads: 200").assertIsDisplayed()
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
        val configPropsResponse = ConfigPropsResponse(
            contexts = mapOf("application" to context)
        )
        val configPropsState = UIState.Success(configPropsResponse)

        // When
        setContent {
            ConfigPropsScreen(configPropsState = configPropsState)
        }

        // Then
        onNodeWithText("server.port: 8080").assertIsDisplayed()
        onNodeWithText("management.port: 9090").assertIsDisplayed()
    }
}
