package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ConfigPropsResponse
import com.example.spring_profiler_app.data.UIState
import com.example.spring_profiler_app.data.flattenConfigPropsObject
import com.example.spring_profiler_app.ui.components.ApiStateWrapper
import com.example.spring_profiler_app.ui.components.ScrollableContent

@Composable
fun ConfigPropsScreen(configPropsState: UIState<ConfigPropsResponse>) {
    ApiStateWrapper(
        state = configPropsState,
    ) { data ->
        ConfigPropsContent(data)
    }
}

@Composable
private fun ConfigPropsContent(configPropsResponse: ConfigPropsResponse) {
    val flatProperties = mutableMapOf<String, String>()

    for ((_, context) in configPropsResponse.contexts) {
        for ((_, bean) in context.beans) {
            val beanPrefix = bean.prefix
            val beanProperties = bean.properties

            val flatBeanProperties = mutableMapOf<String, String>()
            flattenConfigPropsObject(beanProperties, "", flatBeanProperties)

            flatBeanProperties.forEach { (propertyKey, value) ->
                val fullKey = "$beanPrefix.$propertyKey"
                flatProperties[fullKey] = value
            }
        }
    }

    ScrollableContent {
        flatProperties.forEach { property ->
            Card(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Row(modifier = Modifier.padding(10.dp)) {
                    Text(text = "${property.key}: ${property.value}")
                }
            }
        }
    }
}
