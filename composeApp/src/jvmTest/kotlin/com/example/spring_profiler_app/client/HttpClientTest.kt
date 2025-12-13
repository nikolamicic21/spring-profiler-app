package com.example.spring_profiler_app.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class HttpClientTest {

    @Serializable
    data class TestResponse(val message: String, val value: Int)

    @Test
    fun `safeRequest should return parsed response on 200 OK`() = runTest {
        // Given
        val responseJson = """{"message": "success", "value": 42}"""
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When
        val result = safeRequest<TestResponse>(client) {
            url("http://localhost:8080/test")
        }

        // Then
        assertEquals("success", result.message)
        assertEquals(42, result.value)
    }

    @Test
    fun `safeRequest should return parsed response on 201 Created`() = runTest {
        // Given
        val responseJson = """{"message": "created", "value": 100}"""
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When
        val result = safeRequest<TestResponse>(client) {
            url("http://localhost:8080/test")
        }

        // Then
        assertEquals("created", result.message)
        assertEquals(100, result.value)
    }

    @Test
    fun `safeRequest should throw ClientRequestException on 400 Bad Request`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Bad Request"),
                status = HttpStatusCode.BadRequest,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ClientRequestException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should throw ClientRequestException on 404 Not Found`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Not Found"),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ClientRequestException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should throw ClientRequestException on 401 Unauthorized`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Unauthorized"),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ClientRequestException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should throw ServerResponseException on 500 Internal Server Error`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Internal Server Error"),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ServerResponseException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should throw ServerResponseException on 503 Service Unavailable`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Service Unavailable"),
                status = HttpStatusCode.ServiceUnavailable,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ServerResponseException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should throw ServerResponseException on invalid JSON`() = runTest {
        // Given
        val invalidJson = """{"message": "incomplete"""
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(invalidJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ServerResponseException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should throw ServerResponseException on JSON with missing required fields`() = runTest {
        // Given
        val incompleteJson = """{"message": "test"}"""
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(incompleteJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ServerResponseException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should handle Unit type for responses without body`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When
        val result = safeRequest<Unit>(client) {
            url("http://localhost:8080/test")
        }

        // Then
        assertEquals(Unit, result)
    }

    @Test
    fun `safeRequest should throw ServerResponseException on 300 status code`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("Multiple Choices"),
                status = HttpStatusCode.MultipleChoices,
                headers = headersOf(HttpHeaders.ContentType, "text/plain")
            )
        }
        val client = createTestClient(mockEngine)

        // When/Then
        assertFailsWith<ServerResponseException> {
            safeRequest<TestResponse>(client) {
                url("http://localhost:8080/test")
            }
        }
    }

    @Test
    fun `safeRequest should handle 204 No Content successfully`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When
        val result = safeRequest<Unit>(client) {
            url("http://localhost:8080/test")
        }

        // Then
        assertEquals(Unit, result)
    }

    @Test
    fun `safeRequest should handle 202 Accepted successfully`() = runTest {
        // Given
        val responseJson = """{"message": "accepted", "value": 999}"""
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel(responseJson),
                status = HttpStatusCode.Accepted,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createTestClient(mockEngine)

        // When
        val result = safeRequest<TestResponse>(client) {
            url("http://localhost:8080/test")
        }

        // Then
        assertEquals("accepted", result.message)
        assertEquals(999, result.value)
    }

    private fun createTestClient(mockEngine: MockEngine): HttpClient {
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            expectSuccess = false
        }
    }
}
