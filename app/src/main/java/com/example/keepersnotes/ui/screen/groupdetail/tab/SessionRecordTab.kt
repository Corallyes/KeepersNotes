package com.example.keepersnotes.ui.screen.groupdetail.tab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.SessionEntity
import com.example.keepersnotes.ui.component.SessionCard
import com.example.keepersnotes.util.LocalizedStrings

@Composable
fun SessionRecordTab(
    sessions: List<SessionEntity>,
    onSessionClick: (String) -> Unit,
    onCreateSession: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                Text(
                    text = LocalizedStrings.sessionNoSessions,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sessions, key = { it.sessionId }) { session ->
                    SessionCard(
                        session = session,
                        onClick = { onSessionClick(session.sessionId) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateSession,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = LocalizedStrings.sessionAdd)
        }
    }
}
