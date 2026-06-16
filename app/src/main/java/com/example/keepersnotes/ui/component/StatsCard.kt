package com.example.keepersnotes.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun StatsCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun StatsCardRow(
    activeGroupCount: Int,
    totalPcCount: Int,
    upcomingCount: Int,
    weeklySessionCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatsCard(
            icon = Icons.Default.People,
            label = "进行中",
            value = "$activeGroupCount",
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            icon = Icons.Default.Person,
            label = "总PC数",
            value = "$totalPcCount",
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            icon = Icons.Default.DateRange,
            label = "待开团",
            value = "$upcomingCount",
            modifier = Modifier.weight(1f)
        )
        StatsCard(
            icon = Icons.Default.Schedule,
            label = "本周",
            value = "$weeklySessionCount",
            modifier = Modifier.weight(1f)
        )
    }
}
