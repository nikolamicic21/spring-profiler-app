package com.example.spring_profiler_app.data

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

enum class ActuatorEndpoints(val title: String) {
    BEANS("Beans"), HEALTH("Health"), CONFIG_PROPS("Configuration properties"), METRICS("Metrics")
}

data class Server(val url: Url)

@Serializable
data class BeansResponse(
    val contexts: Map<String, Beans>,
)

@Serializable
data class Beans(
    val beans: Map<String, Bean>
)

@Serializable
data class Bean(
    val dependencies: List<String>,
    val scope: String,
)

@Serializable
data class HealthResponse(
    val status: String, val components: Map<String, Component>? = null
) {

    @Serializable
    data class Component(
        val status: String,
    )
}

@Serializable
data class ConfigPropsResponse(
    val contexts: Map<String, Context>
)

@Serializable
data class Context(
    val beans: Map<String, BeanProperties>
)

@Serializable
data class BeanProperties(
    val prefix: String,
    val properties: JsonObject
)

fun flattenConfigPropsObject(
    obj: JsonObject,
    path: String,
    result: MutableMap<String, String>
) {
    for ((key, element) in obj) {
        val currentPath = if (path.isEmpty()) key else "$path.$key"

        when (element) {
            is JsonObject -> {
                // Nested object, recurse
                flattenConfigPropsObject(element, currentPath, result)
            }

            is JsonPrimitive -> {
                // Primitive value (String, Boolean, Number)
                // Use content for the raw string value (e.g., "true", "/actuator")
                result[currentPath] = element.content
            }

            else -> {
                // Array or other types (e.g., empty array [], list of strings ["*"])
                // Simply serialize the element as a clean JSON string
                result[currentPath] = element.toString()
            }
        }
    }
}

@Serializable
data class MetricNamesResult(
    val names: List<String>
)

@Serializable
data class MetricsResult(
    val name: String,
    val description: String? = null,
    val baseUnit: String? = null,
    val measurements: List<MeasurementResult>
)

@Serializable
data class MeasurementResult(
    val statistic: String,
    val value: Double
)

data class Measurement(val statistic: String, val value: Double)

data class Metric(
    val name: String,
    val measurements: List<Measurement>,
    val unit: String?
)

data class MetricsResponse(val metrics: List<Metric>)