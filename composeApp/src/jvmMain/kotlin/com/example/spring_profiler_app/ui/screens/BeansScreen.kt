package com.example.spring_profiler_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.spring_profiler_app.data.ApiUiState
import com.example.spring_profiler_app.data.BeansResponse
import com.example.spring_profiler_app.ui.components.ApiStateWrapper
import kotlinx.coroutines.launch

@Composable
fun BeansScreen(beansState: ApiUiState<BeansResponse>) {
    ApiStateWrapper(
        state = beansState,
    ) { data ->
        BeansContent(data)
    }
}

@Composable
private fun BeansContent(beansResponse: BeansResponse) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val beans = beansResponse.contexts.values.flatMap { beans ->
        beans.beans.map { bean -> Pair(bean.key, bean.value) }
    }

    var searchQuery by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize().padding(10.dp)) {
        Text(text = "Active contexts: ${beansResponse.contexts.keys.joinToString(", ")}")

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search for Bean by name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().onKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
                    if (!searchQuery.isBlank()) {
                        val targetIndex = beans.map { it.first }
                            .indexOfFirst { it.contains(searchQuery, ignoreCase = true) }
                        if (targetIndex != -1) {
                            scope.launch {
                                lazyListState.animateScrollToItem(index = targetIndex)
                            }
                        }
                    } else {
                        scope.launch {
                            lazyListState.animateScrollToItem(index = 0)
                        }
                    }
                    true
                } else {
                    false
                }
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(beans) { bean ->
                BeanCard(
                    beanName = bean.first,
                    scope = bean.second.scope,
                    dependencies = bean.second.dependencies,
                    onDependencyClick = { dependency ->
                        scope.launch {
                            val targetIndex = beans.map { it.first }.indexOf(dependency)
                            if (targetIndex != -1) {
                                lazyListState.animateScrollToItem(index = targetIndex)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BeanCard(
    beanName: String,
    scope: String,
    dependencies: List<String>,
    onDependencyClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(10.dp)) {
            Text(text = "Bean name: $beanName")
        }
        Row(modifier = Modifier.padding(10.dp)) {
            Text(text = "Scope: $scope")
        }
        if (dependencies.isNotEmpty()) {
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = "Dependencies:")
                dependencies.forEach { dependency ->
                    Row(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                        Text(
                            text = "•",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            modifier = Modifier.clickable { onDependencyClick(dependency) },
                            text = dependency,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
