package com.example.spring_profiler_app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.spring_profiler_app.client.client
import com.example.spring_profiler_app.data.ActuatorRepositoryImpl

val repo = ActuatorRepositoryImpl(client)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Spring Profiler",
    ) {
        CompositionLocalProvider(Repository provides repo) {
            App()
        }
    }
}