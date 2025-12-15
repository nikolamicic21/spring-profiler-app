package com.example.spring_profiler_app.ui.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.Client
import com.example.spring_profiler_app.client.safeRequest
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AddServerForm(
    servers: MutableMap<Server, ServerState>,
    onServerAdded: (Server) -> Unit,
    ioScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val client = Client.current
        var urlText by rememberSaveable { mutableStateOf("") }
        var errorMessage by rememberSaveable { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                label = { Text("Server's actuator endpoint (URL)") },
                placeholder = { Text("http://localhost:8080/actuator") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotBlank()) {
                Text(text = errorMessage)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    ioScope.launch {
                        try {
                            val serverUrl = Url(urlText)
                            if (servers.keys.any { it.url.host == serverUrl.host && it.url.port == serverUrl.port }) {
                                errorMessage = "Server already exists"
                            } else {
                                safeRequest<Unit>(client) {
                                    url(serverUrl)
                                }
                                val newServer = Server(serverUrl)
                                if (!servers.keys.any { it.url.host == newServer.url.host && it.url.port == newServer.url.port }) {
                                    servers[newServer] = ServerState(
                                        newServer,
                                        UIState.Loading,
                                        UIState.Loading,
                                        UIState.Loading,
                                        UIState.Loading
                                    )
                                    onServerAdded(newServer)
                                }

                                errorMessage = ""
                            }
                        } catch (_: Exception) {
                            errorMessage =
                                "There's been an error connecting to the server's actuator endpoint. Please check the URL!"
                        }
                    }
                },
                enabled = urlText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect")
            }
        }
    }
}
