package com.example.spring_profiler_app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.spring_profiler_app.data.ActuatorRepository
import com.example.spring_profiler_app.data.ActuatorRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val Client = compositionLocalOf<HttpClient> {
    error("HttpClient not provided. Make sure to wrap your composables with CompositionLocalProvider.")
}

val Repository = compositionLocalOf<ActuatorRepository> {
    error("Repository not provided. Make sure to wrap your composables with CompositionLocalProvider.")
}

fun main() = application {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        expectSuccess = false
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Spring Profiler",
    ) {
        CompositionLocalProvider(
            Client provides client,
            Repository provides ActuatorRepositoryImpl(client)
        ) {
            App()
        }
    }
}
