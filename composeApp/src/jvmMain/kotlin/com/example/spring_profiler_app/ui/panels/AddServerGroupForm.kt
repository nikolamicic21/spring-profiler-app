package com.example.spring_profiler_app.ui.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.Client
import com.example.spring_profiler_app.client.safeRequest
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState
import com.example.spring_profiler_app.data.ServerState
import com.example.spring_profiler_app.data.UIState
import io.ktor.client.HttpClient
import io.ktor.client.request.url
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AddServerGroupForm(
    serverGroups: MutableMap<ServerGroup, ServerGroupState>,
    onServerGroupAdded: (ServerGroup) -> Unit,
    ioScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    ServerGroupForm(
        serverGroups = serverGroups,
        existingGroup = null,
        onServerGroupSaved = onServerGroupAdded,
        ioScope = ioScope,
        modifier = modifier
    )
}

@Composable
fun EditServerGroupForm(
    serverGroups: MutableMap<ServerGroup, ServerGroupState>,
    existingGroup: ServerGroup,
    onServerGroupUpdated: (ServerGroup) -> Unit,
    ioScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    ServerGroupForm(
        serverGroups = serverGroups,
        existingGroup = existingGroup,
        onServerGroupSaved = onServerGroupUpdated,
        ioScope = ioScope,
        modifier = modifier
    )
}

@Composable
private fun ServerGroupForm(
    serverGroups: MutableMap<ServerGroup, ServerGroupState>,
    existingGroup: ServerGroup?,
    onServerGroupSaved: (ServerGroup) -> Unit,
    ioScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val client = Client.current
        val isEditMode by rememberSaveable(existingGroup) { mutableStateOf(existingGroup != null) }
        var groupName by rememberSaveable(existingGroup) { mutableStateOf(existingGroup?.name ?: "App") }
        val endpointUrls = rememberSaveable(existingGroup) {
            mutableStateListOf<String>().apply {
                if (existingGroup != null) {
                    addAll(existingGroup.endpoints.map { it.url.toString() })
                } else {
                    add("http://localhost:8080/actuator")
                }
            }
        }
        var errorMessage by rememberSaveable { mutableStateOf("") }
        var connecting by rememberSaveable { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditMode) "Edit Server Group" else "Add Server Group",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                placeholder = { Text("My Spring Application") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Actuator Endpoints",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            endpointUrls.forEachIndexed { index, url ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { endpointUrls[index] = it },
                        label = { Text("Endpoint ${index + 1}") },
                        placeholder = { Text("http://localhost:808${index}/actuator") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    if (endpointUrls.size > 1) {
                        IconButton(onClick = { endpointUrls.removeAt(index) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove endpoint"
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { endpointUrls.add("http://localhost:808${endpointUrls.size}/actuator") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(4.dp))
                Text("Add Another Endpoint")
            }

            if (errorMessage.isNotBlank()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    ioScope.launch {
                        try {
                            connecting = true

                            val validationResult = validateForm(
                                groupName = groupName,
                                endpointUrls = endpointUrls.toList(),
                                serverGroups = serverGroups,
                                existingGroup = existingGroup
                            )

                            if (validationResult is ValidationResult.Error) {
                                errorMessage = validationResult.message
                                return@launch
                            }

                            val servers = endpointUrls.toSet().map { Server(Url(it)) }

                            val failedEndpoints = testEndpointConnectivity(servers, client)
                            if (failedEndpoints.isNotEmpty()) {
                                errorMessage =
                                    "Failed to connect to actuator endpoint(s): ${failedEndpoints.joinToString(", ")}"
                                return@launch
                            }

                            val savedGroup = saveServerGroup(
                                serverGroups = serverGroups,
                                existingGroup = if (isEditMode) existingGroup else null,
                                groupName = groupName,
                                servers = servers
                            )

                            onServerGroupSaved(savedGroup)
                            errorMessage = ""
                        } catch (e: Exception) {
                            errorMessage = "Error: ${e.message ?: "Invalid URL or connection failed"}"
                        } finally {
                            connecting = false
                        }
                    }
                },
                enabled = groupName.isNotBlank() && endpointUrls.all { it.isNotBlank() } && !connecting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditMode) "Update Group" else "Connect Group")
            }
        }
    }
}

private sealed class ValidationResult {
    data object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

private fun validateForm(
    groupName: String,
    endpointUrls: List<String>,
    serverGroups: Map<ServerGroup, ServerGroupState>,
    existingGroup: ServerGroup?
): ValidationResult {
    if (groupName.isBlank()) {
        return ValidationResult.Error("Please enter a group name")
    }

    if (endpointUrls.any { it.isBlank() }) {
        return ValidationResult.Error("All endpoint URLs must be filled")
    }

    if (serverGroups.keys.any { it.name == groupName && it != existingGroup }) {
        return ValidationResult.Error("A group with this name already exists")
    }

    return ValidationResult.Success
}

private suspend fun testEndpointConnectivity(
    servers: List<Server>,
    client: HttpClient
): List<String> {
    val failedEndpoints = mutableListOf<String>()
    servers.forEach { server ->
        try {
            safeRequest<Unit>(client) {
                url(server.url)
            }
        } catch (_: Exception) {
            failedEndpoints.add(server.url.toString())
        }
    }
    return failedEndpoints
}

private fun createLoadingServerState(server: Server) = ServerState(
    server = server,
    beans = UIState.Loading,
    health = UIState.Loading,
    configProps = UIState.Loading,
    metrics = UIState.Loading
)

private fun saveServerGroup(
    serverGroups: MutableMap<ServerGroup, ServerGroupState>,
    existingGroup: ServerGroup?,
    groupName: String,
    servers: List<Server>
): ServerGroup {
    return if (existingGroup != null) {
        val oldGroupState = serverGroups.remove(existingGroup)

        val updatedGroup = ServerGroup(
            id = existingGroup.id,
            name = groupName,
            endpoints = servers
        )

        val endpointStates = servers.associateWith { server ->
            oldGroupState?.endpointStates?.entries?.find {
                it.key.url == server.url
            }?.value ?: createLoadingServerState(server)
        }

        serverGroups[updatedGroup] = ServerGroupState(
            group = updatedGroup,
            endpointStates = endpointStates
        )

        updatedGroup
    } else {
        val newGroup = ServerGroup(
            name = groupName,
            endpoints = servers
        )

        val endpointStates = servers.associateWith { server ->
            createLoadingServerState(server)
        }

        serverGroups[newGroup] = ServerGroupState(
            group = newGroup,
            endpointStates = endpointStates
        )

        newGroup
    }
}
