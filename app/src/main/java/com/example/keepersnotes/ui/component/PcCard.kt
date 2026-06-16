package com.example.keepersnotes.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import com.example.keepersnotes.util.Constants

@Composable
fun PcCard(
    pc: PlayerCharacterEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: PC info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = pc.characterName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PcStatusBadge(status = pc.status)
                }
                Text(
                    text = pc.playerName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: HP / SAN stats
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "HP: ${pc.hpCurrent}/${pc.hpMax}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pc.hpCurrent < pc.hpMax / 2) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "SAN: ${pc.sanCurrent}/${pc.sanMax}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (pc.sanCurrent < pc.sanMax / 2) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PcStatusBadge(status: String) {
    val (label, color) = when (status) {
        Constants.PC_STATUS_NORMAL -> "正常" to MaterialTheme.colorScheme.primary
        Constants.PC_STATUS_WOUNDED -> "重伤" to MaterialTheme.colorScheme.error
        Constants.PC_STATUS_INSANE -> "疯狂" to MaterialTheme.colorScheme.tertiary
        Constants.PC_STATUS_DEAD -> "死亡" to MaterialTheme.colorScheme.outline
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
