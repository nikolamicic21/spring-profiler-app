package com.example.spring_profiler_app.data

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.io.IOException
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun getFriendlyMessage(
    screen: ActuatorEndpoints, exception: Throwable
): String = when (exception) {
    is ClientRequestException -> "Request failed: ${exception.response.status.value}. Did you enable the ${screen.title} endpoint? If not, please do and refresh the connection!"
    is ServerResponseException -> "Server error: ${exception.response.status.value}"
    is IOException -> "Connection Error. Check your network and refresh the connection!"
    else -> "An unknown error occurred: ${exception.message}"
}

fun formatNumberWithoutGrouping(number: Number): String {
    val pattern = "0.####################"
    val symbols = DecimalFormatSymbols(Locale.getDefault())

    val formatter = DecimalFormat(pattern, symbols)
    formatter.isGroupingUsed = false
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 20

    return formatter.format(number)
}