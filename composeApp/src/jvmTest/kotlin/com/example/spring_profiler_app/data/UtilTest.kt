package com.example.spring_profiler_app.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UtilTest {

    @Test
    fun `getFriendlyMessage should return friendly message for ClientRequestException with 404`() {
        // Given
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        every { mockResponse.status } returns HttpStatusCode.NotFound
        val exception = ClientRequestException(mockResponse, "Not Found")
        val endpoint = ActuatorEndpoints.BEANS

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("404"))
        assertTrue(result.contains("Beans"))
        assertTrue(result.contains("enable the"))
        assertTrue(result.contains("endpoint"))
    }

    @Test
    fun `getFriendlyMessage should return friendly message for ClientRequestException with 403`() {
        // Given
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        every { mockResponse.status } returns HttpStatusCode.Forbidden
        val exception = ClientRequestException(mockResponse, "Forbidden")
        val endpoint = ActuatorEndpoints.HEALTH

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("403"))
        assertTrue(result.contains("Health"))
        assertTrue(result.contains("Request failed"))
    }

    @Test
    fun `getFriendlyMessage should return friendly message for ServerResponseException with 500`() {
        // Given
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        every { mockResponse.status } returns HttpStatusCode.InternalServerError
        val exception = ServerResponseException(mockResponse, "Internal Server Error")
        val endpoint = ActuatorEndpoints.CONFIG_PROPS

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("Server error"))
        assertTrue(result.contains("500"))
    }

    @Test
    fun `getFriendlyMessage should return friendly message for ServerResponseException with 503`() {
        // Given
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        every { mockResponse.status } returns HttpStatusCode.ServiceUnavailable
        val exception = ServerResponseException(mockResponse, "Service Unavailable")
        val endpoint = ActuatorEndpoints.METRICS

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("Server error"))
        assertTrue(result.contains("503"))
    }

    @Test
    fun `getFriendlyMessage should return connection error message for IOException`() {
        // Given
        val exception = IOException("Network timeout")
        val endpoint = ActuatorEndpoints.BEANS

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("Connection Error"))
        assertTrue(result.contains("network"))
        assertTrue(result.contains("refresh"))
    }

    @Test
    fun `getFriendlyMessage should return unknown error message for other exceptions`() {
        // Given
        val exception = RuntimeException("Something went wrong")
        val endpoint = ActuatorEndpoints.HEALTH

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("unknown error"))
        assertTrue(result.contains("Something went wrong"))
    }

    @Test
    fun `getFriendlyMessage should return unknown error message for NullPointerException`() {
        // Given
        val exception = NullPointerException("Null value encountered")
        val endpoint = ActuatorEndpoints.METRICS

        // When
        val result = getFriendlyMessage(endpoint, exception)

        // Then
        assertTrue(result.contains("unknown error"))
        assertTrue(result.contains("Null value encountered"))
    }

    @Test
    fun `getFriendlyMessage should handle all ActuatorEndpoints types`() {
        // Given
        val mockResponse = mockk<HttpResponse>(relaxed = true)
        every { mockResponse.status } returns HttpStatusCode.NotFound
        val exception = ClientRequestException(mockResponse, "Not Found")

        // When/Then
        val beansResult = getFriendlyMessage(ActuatorEndpoints.BEANS, exception)
        assertTrue(beansResult.contains("Beans"))

        val healthResult = getFriendlyMessage(ActuatorEndpoints.HEALTH, exception)
        assertTrue(healthResult.contains("Health"))

        val configPropsResult = getFriendlyMessage(ActuatorEndpoints.CONFIG_PROPS, exception)
        assertTrue(configPropsResult.contains("Configuration properties"))

        val metricsResult = getFriendlyMessage(ActuatorEndpoints.METRICS, exception)
        assertTrue(metricsResult.contains("Metrics"))
    }

    @Test
    fun `formatNumberWithoutGrouping should format integer without grouping`() {
        // Given
        val number = 1234567

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("1234567", result)
        assertFalse(result.contains(","))
        assertFalse(result.contains(" "))
    }

    @Test
    fun `formatNumberWithoutGrouping should format large integer without grouping`() {
        // Given
        val number = 1234567890

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("1234567890", result)
        assertFalse(result.contains(","))
    }

    @Test
    fun `formatNumberWithoutGrouping should format double with decimal places`() {
        // Given
        val number = 123.456

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("123"))
        assertTrue(result.contains(".") || result.contains(","))
        assertFalse(result.contains(" "))
    }

    @Test
    fun `formatNumberWithoutGrouping should format double without trailing zeros`() {
        // Given
        val number = 123.0

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("123", result)
    }

    @Test
    fun `formatNumberWithoutGrouping should format small decimal number`() {
        // Given
        val number = 0.123456789

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("0"))
        assertTrue(result.length > 2)
    }

    @Test
    fun `formatNumberWithoutGrouping should format very small decimal number`() {
        // Given
        val number = 0.00000123456789

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("0"))
        assertTrue(result.contains("123456789") || result.contains("0.00000123456789"))
    }

    @Test
    fun `formatNumberWithoutGrouping should format zero`() {
        // Given
        val number = 0

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("0", result)
    }

    @Test
    fun `formatNumberWithoutGrouping should format negative integer`() {
        // Given
        val number = -12345

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("-"))
        assertTrue(result.contains("12345"))
        assertFalse(result.contains(","))
    }

    @Test
    fun `formatNumberWithoutGrouping should format negative double`() {
        // Given
        val number = -123.456

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("-"))
        assertTrue(result.contains("123"))
    }

    @Test
    fun `formatNumberWithoutGrouping should handle Long type`() {
        // Given
        val number = 9876543210L

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("9876543210", result)
        assertFalse(result.contains(","))
    }

    @Test
    fun `formatNumberWithoutGrouping should handle Float type`() {
        // Given
        val number = 123.45f

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("123"))
        assertTrue(result.length >= 3)
    }

    @Test
    fun `formatNumberWithoutGrouping should handle very large double`() {
        // Given
        val number = 123456789.987654321

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("123456789"))
        assertFalse(result.contains(","))
    }

    @Test
    fun `formatNumberWithoutGrouping should limit decimal places to maximum 20`() {
        // Given
        val number = 1.123456789012345678901234567890

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("1"))
        val decimalPart = result.substringAfter(".", "").substringAfter(",", "")
        assertTrue(decimalPart.length <= 20)
    }

    @Test
    fun `formatNumberWithoutGrouping should handle Short type`() {
        // Given
        val number: Short = 12345

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("12345", result)
    }

    @Test
    fun `formatNumberWithoutGrouping should handle Byte type`() {
        // Given
        val number: Byte = 127

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertEquals("127", result)
    }

    @Test
    fun `formatNumberWithoutGrouping should format number with repeating decimals`() {
        // Given
        val number = 1.0 / 3.0

        // When
        val result = formatNumberWithoutGrouping(number)

        // Then
        assertTrue(result.startsWith("0"))
        assertTrue(result.contains("3"))
    }
}
