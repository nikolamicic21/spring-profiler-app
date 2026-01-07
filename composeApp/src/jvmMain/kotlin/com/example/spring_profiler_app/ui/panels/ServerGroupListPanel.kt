package com.example.spring_profiler_app.ui.panels

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState

@Composable
fun ServerGroupListPanel(
    serverGroups: Map<ServerGroup, ServerGroupState>,
    currentGroupKey: ServerGroup?,
    onAddGroupClick: () -> Unit,
    onGroupSelect: (ServerGroup) -> Unit,
    onRefreshGroup: (ServerGroup) -> Unit,
    onDeleteGroup: (ServerGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Server Groups",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        HorizontalDivider()

        Box(Modifier.fillMaxHeight().weight(1f)) {
            val scroll = rememberScrollState()
            Column(
                modifier = Modifier.verticalScroll(scroll).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier.padding(10.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(onClick = onAddGroupClick) {
                        Text("Add a new group")
                    }
                }
                serverGroups.keys.toList().forEach { group ->
                    ServerGroupListItem(
                        group = group,
                        isSelected = currentGroupKey == group,
                        onSelect = { onGroupSelect(group) },
                        onRefresh = { onRefreshGroup(group) },
                        onDelete = { onDeleteGroup(group) },
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(scrollState = scroll)
            )
        }
    }
}

@Composable
private fun ServerGroupListItem(
    group: ServerGroup,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRefresh: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Box(contentAlignment = Alignment.CenterStart) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Box(modifier = Modifier.padding(2.dp).weight(0.7f)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier.clickable(onClick = onSelect)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = group.name,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${group.endpoints.size} endpoint${if (group.endpoints.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Row {
                            Column {
                                IconButton(onClick = onRefresh) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh data",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column {
                                IconButton(onClick = onDelete) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete group",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}
