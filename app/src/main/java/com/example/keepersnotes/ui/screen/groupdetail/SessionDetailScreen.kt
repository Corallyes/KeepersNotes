package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val session = uiState.sessions.find { it.sessionId == sessionId }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = session?.let { "Session ${it.sessionNumber}" } ?: "Session详情",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: edit session */ }) {
                        Icon(Icons.Default.Edit, contentDescription = "编辑")
                    }
                }
            )
        }
    ) { padding ->
        if (session == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("加载中...", modifier = Modifier.padding(16.dp))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Session info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Session ${session.sessionNumber}", style = MaterialTheme.typography.headlineSmall)
                    Text("日期: ${formatDate(session.date)}", style = MaterialTheme.typography.bodyMedium)
                    if (session.durationMinutes > 0) {
                        Text("时长: ${session.durationMinutes / 60}h${session.durationMinutes % 60}min", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Summary
            if (session.summary.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("本场摘要", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(session.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // TODO: Important events list
            // TODO: Clues found list
            // TODO: Dice rolls log
            // TODO: Next session notes

            if (session.nextSessionNotes.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("下局预告", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(session.nextSessionNotes, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
