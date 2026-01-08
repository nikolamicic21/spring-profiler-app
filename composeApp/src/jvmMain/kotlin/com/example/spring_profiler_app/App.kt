package com.example.spring_profiler_app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState
import com.example.spring_profiler_app.data.refreshGroupHealthState
import com.example.spring_profiler_app.data.refreshGroupMetricsState
import com.example.spring_profiler_app.data.refreshGroupState
import com.example.spring_profiler_app.ui.panels.AddServerGroupForm
import com.example.spring_profiler_app.ui.panels.EditServerGroupForm
import com.example.spring_profiler_app.ui.panels.ServerGroupDetailsPanel
import com.example.spring_profiler_app.ui.panels.ServerGroupListPanel
import com.example.spring_profiler_app.ui.rememberDebouncedCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val repository = Repository.current
            val serverGroups = remember { mutableStateMapOf<ServerGroup, ServerGroupState>() }
            val currentGroup = remember { mutableStateOf<ServerGroup?>(null) }
            val editingGroup = remember { mutableStateOf<ServerGroup?>(null) }
            val scope = rememberCoroutineScope()
            val ioScope = remember(scope) {
                CoroutineScope(scope.coroutineContext + Dispatchers.IO + SupervisorJob(scope.coroutineContext[Job]))
            }

            val refreshHealthCallback: suspend () -> Unit = {
                currentGroup.value?.let { group ->
                    val groupState = serverGroups[group] ?: return@let
                    groupState.group.endpoints.forEach { server ->
                        serverGroups.refreshGroupHealthState(group, server, repository, showLoadingOnRefresh = false)
                    }
                }
            }

            val refreshMetricsCallback: suspend () -> Unit = {
                currentGroup.value?.let { group ->
                    val groupState = serverGroups[group] ?: return@let
                    groupState.group.endpoints.forEach { server ->
                        serverGroups.refreshGroupMetricsState(group, server, repository, showLoadingOnRefresh = false)
                    }
                }
            }

            Row(Modifier.fillMaxHeight()) {
                Box(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    val debouncedRefreshGroup = rememberDebouncedCallback<ServerGroup>(
                        debounceInterval = 1000L
                    ) { group ->
                        ioScope.launch { serverGroups.refreshGroupState(group, repository) }
                    }

                    ServerGroupListPanel(
                        serverGroups = serverGroups,
                        currentGroup = currentGroup.value,
                        editingGroup = editingGroup.value,
                        onAddGroupClick = {
                            currentGroup.value = null
                            editingGroup.value = null
                        },
                        onGroupSelect = { group ->
                            currentGroup.value = group
                            editingGroup.value = null
                        },
                        onRefreshGroup = debouncedRefreshGroup,
                        onEditGroup = { group ->
                            editingGroup.value = group
                            currentGroup.value = null
                        },
                        onDeleteGroup = { group ->
                            if (currentGroup.value == group) {
                                currentGroup.value = null
                            }
                            if (editingGroup.value == group) {
                                editingGroup.value = null
                            }
                            serverGroups.remove(group)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                VerticalDivider(
                    modifier = Modifier.width(1.dp).fillMaxHeight(),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Box(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val currentGroupState = currentGroup.value?.let { serverGroups[it] }
                    val editingGroupValue = editingGroup.value

                    when {
                        editingGroupValue != null -> {
                            EditServerGroupForm(
                                serverGroups = serverGroups,
                                existingGroup = editingGroupValue,
                                onServerGroupUpdated = { updatedGroup ->
                                    ioScope.launch {
                                        serverGroups.refreshGroupState(updatedGroup, repository)
                                    }
                                    currentGroup.value = updatedGroup
                                    editingGroup.value = null
                                },
                                ioScope = ioScope,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        currentGroupState == null -> {
                            AddServerGroupForm(
                                serverGroups = serverGroups,
                                onServerGroupAdded = { newGroup ->
                                    ioScope.launch {
                                        serverGroups.refreshGroupState(newGroup, repository)
                                    }
                                    currentGroup.value = newGroup
                                },
                                ioScope = ioScope,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        else -> {
                            ServerGroupDetailsPanel(
                                groupState = currentGroupState,
                                refreshHealthCallback = refreshHealthCallback,
                                refreshMetricsCallback = refreshMetricsCallback,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
