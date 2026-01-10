package com.example.spring_profiler_app.ui.panels

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ServerGroup
import com.example.spring_profiler_app.data.ServerGroupState

@Composable
fun ServerGroupListPanel(
    serverGroups: Map<ServerGroup, ServerGroupState>,
    currentGroup: ServerGroup?,
    editingGroup: ServerGroup?,
    onAddGroupClick: () -> Unit,
    onGroupSelect: (ServerGroup) -> Unit,
    onRefreshGroup: (ServerGroup) -> Unit,
    onEditGroup: (ServerGroup) -> Unit,
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
                        isSelected = currentGroup == group || editingGroup == group,
                        onSelect = { onGroupSelect(group) },
                        onRefresh = { onRefreshGroup(group) },
                        onEdit = { onEditGroup(group) },
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ServerGroupListItem(
    group: ServerGroup,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    var cardWidth by remember { mutableStateOf(0) }
    val widthThreshold = 300
    var showHamburgerMenu by remember(cardWidth) { mutableStateOf(cardWidth < widthThreshold) }
    var menuExpanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.CenterStart) {
        Row(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Box(modifier = Modifier.padding(2.dp).weight(0.7f)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = color),
                    modifier = Modifier
                        .clickable(onClick = onSelect)
                        .fillMaxWidth()
                        .onSizeChanged { size ->
                            cardWidth = size.width
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            TooltipArea(
                                tooltip = {
                                    Surface(
                                        modifier = Modifier.shadow(4.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = group.name,
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                },
                                delayMillis = 600,
                                tooltipPlacement = TooltipPlacement.CursorPoint(
                                    offset = DpOffset(0.dp, 16.dp)
                                )
                            ) {
                                Text(
                                    text = group.name,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
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

                        ServerGroupActions(
                            showHamburgerMenu = showHamburgerMenu,
                            menuExpanded = menuExpanded,
                            onMenuExpandedChange = { menuExpanded = it },
                            onRefresh = onRefresh,
                            onEdit = onEdit,
                            onDelete = onDelete
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerGroupActions(
    showHamburgerMenu: Boolean,
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    if (showHamburgerMenu) {
        ServerGroupDropdownMenu(
            expanded = menuExpanded,
            onExpandedChange = onMenuExpandedChange,
            onRefresh = onRefresh,
            onEdit = onEdit,
            onDelete = onDelete
        )
    } else {
        ServerGroupActionButtons(
            onRefresh = onRefresh,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
private fun ServerGroupDropdownMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        IconButton(onClick = { onExpandedChange(true) }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More actions",
                modifier = Modifier.size(24.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("Refresh") },
                leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                onClick = {
                    onExpandedChange(false)
                    onRefresh()
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    onExpandedChange(false)
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                onClick = {
                    onExpandedChange(false)
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun ServerGroupActionButtons(
    onRefresh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row {
        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh data",
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit group",
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete group",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
