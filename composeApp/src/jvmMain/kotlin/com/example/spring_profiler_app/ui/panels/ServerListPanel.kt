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
import com.example.spring_profiler_app.data.Server
import com.example.spring_profiler_app.data.ServerState

@Composable
fun ServerListPanel(
    servers: Map<Server, ServerState>,
    currentServerKey: Server?,
    onAddServerClick: () -> Unit,
    onServerSelect: (Server) -> Unit,
    onRefreshServer: (Server) -> Unit,
    onDeleteServer: (Server) -> Unit,
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
                text = "Servers",
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
                    Button(onClick = onAddServerClick) {
                        Text("Add a new server")
                    }
                }
                servers.keys.toList().forEach { server ->
                    ServerListItem(
                        server = server,
                        isSelected = currentServerKey == server,
                        onSelect = { onServerSelect(server) },
                        onRefresh = { onRefreshServer(server) },
                        onDelete = { onDeleteServer(server) },
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
private fun ServerListItem(
    server: Server,
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
                    Column(modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                        Text(text = "${server.url.host}:${server.url.port}")
                    }
                }
            }
            Box(
                modifier = Modifier.padding(2.dp).weight(0.15f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh data",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Box(
                modifier = Modifier.padding(2.dp).weight(0.15f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete server",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
