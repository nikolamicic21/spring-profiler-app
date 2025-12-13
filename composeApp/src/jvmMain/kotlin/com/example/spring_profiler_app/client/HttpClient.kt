package com.example.spring_profiler_app.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException

suspend inline fun <reified T> safeRequest(
    client: HttpClient,
    block: HttpRequestBuilder.() -> Unit
): T {
    val response: HttpResponse = client.request(block)

    return try {
        if (response.status.isSuccess()) {
            response.body<T>()
        } else {
            when (response.status.value) {
                in 400..499 -> throw ClientRequestException(response, "Client request failed")
                in 500..599 -> throw ServerResponseException(response, "Server Error: ${response.status}")
                else -> throw ServerResponseException(response, "Unknown Error: ${response.status}")
            }
        }
    } catch (_: JsonConvertException) {
        throw ServerResponseException(response, "Server Error: ${response.status}")
    } catch (_: ClientRequestException) {
        throw ClientRequestException(response, "Client request failed")
    } catch (_: Exception) {
        throw ServerResponseException(response, "Server Error: ${response.status}")
    }
}
