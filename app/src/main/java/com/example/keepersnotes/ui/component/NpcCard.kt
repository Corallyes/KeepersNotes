package com.example.keepersnotes.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.NpcEntity
import com.example.keepersnotes.util.Constants
import com.example.keepersnotes.util.LocalizedStrings

@Composable
fun NpcCard(
    npc: NpcEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = npc.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                NpcStatusBadge(status = npc.status)
            }
            if (npc.alias.isNotBlank()) {
                Text(
                    text = "${LocalizedStrings.npcAliasLabel}${npc.alias}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (npc.occupation.isNotBlank()) {
                Text(
                    text = npc.occupation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (npc.description.isNotBlank()) {
                Text(
                    text = npc.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NpcStatusBadge(status: String) {
    val (label, color) = when (status) {
        Constants.NPC_STATUS_ALIVE -> LocalizedStrings.npcStatusAlive to MaterialTheme.colorScheme.primary
        Constants.NPC_STATUS_DEAD -> LocalizedStrings.npcStatusDead to MaterialTheme.colorScheme.error
        Constants.NPC_STATUS_MISSING -> LocalizedStrings.npcStatusMissing to MaterialTheme.colorScheme.tertiary
        Constants.NPC_STATUS_UNKNOWN -> LocalizedStrings.npcStatusUnknown to MaterialTheme.colorScheme.outline
        else -> status to MaterialTheme.colorScheme.outline
    }
    SuggestionChip(
        onClick = {},
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = color.copy(alpha = 0.12f),
            labelColor = color
        )
    )
}
