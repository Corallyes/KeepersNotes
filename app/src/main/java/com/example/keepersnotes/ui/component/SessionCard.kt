package com.example.keepersnotes.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.SessionEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionCard(
    session: SessionEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    pcNames: List<String> = emptyList()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Header: session number + date
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Session ${session.sessionNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatDate(session.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Participants
            if (pcNames.isNotEmpty()) {
                Text(
                    text = "参与: ${pcNames.joinToString("、")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Duration
            if (session.durationMinutes > 0) {
                Text(
                    text = "时长: ${session.durationMinutes / 60}h${session.durationMinutes % 60}min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Summary preview
            if (session.summary.isNotBlank()) {
                Text(
                    text = session.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
