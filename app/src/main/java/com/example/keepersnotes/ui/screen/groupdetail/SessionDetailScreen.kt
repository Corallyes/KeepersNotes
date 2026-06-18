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
import com.example.keepersnotes.util.LocalizedStrings
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel()
) {
    val session by viewModel.session.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = session?.let { "${LocalizedStrings.session} ${it.sessionNumber}" } ?: LocalizedStrings.sessionDetail,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: edit session */ }) {
                        Icon(Icons.Default.Edit, contentDescription = LocalizedStrings.edit)
                    }
                }
            )
        }
    ) { padding ->
        val currentSession = session
        if (currentSession == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text(LocalizedStrings.groupLoading, modifier = Modifier.padding(16.dp))
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
                    Text("${LocalizedStrings.session} ${currentSession.sessionNumber}", style = MaterialTheme.typography.headlineSmall)
                    Text("${LocalizedStrings.sessionDate}: ${formatDate(currentSession.date)}", style = MaterialTheme.typography.bodyMedium)
                    if (currentSession.durationMinutes > 0) {
                        Text("${LocalizedStrings.sessionDuration}: ${currentSession.durationMinutes / 60}h${currentSession.durationMinutes % 60}min", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Summary
            if (currentSession.summary.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.sessionSummary, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentSession.summary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (currentSession.nextSessionNotes.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(LocalizedStrings.sessionNextNotes, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(currentSession.nextSessionNotes, style = MaterialTheme.typography.bodyMedium)
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
