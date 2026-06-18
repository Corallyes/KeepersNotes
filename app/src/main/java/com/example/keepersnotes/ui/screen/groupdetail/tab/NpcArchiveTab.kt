package com.example.keepersnotes.ui.screen.groupdetail.tab

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.data.local.entity.NpcEntity
import com.example.keepersnotes.ui.component.NpcCard
import com.example.keepersnotes.util.LocalizedStrings

@Composable
fun NpcArchiveTab(
    npcs: List<NpcEntity>,
    onNpcClick: (String) -> Unit,
    onCreateNpc: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNpcs = if (searchQuery.isBlank()) npcs
    else npcs.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.alias.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(LocalizedStrings.npcSearchPlaceholder) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (filteredNpcs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    Text(
                        text = if (searchQuery.isBlank()) LocalizedStrings.npcNoNpcs else LocalizedStrings.npcNoMatch,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredNpcs, key = { it.npcId }) { npc ->
                        NpcCard(
                            npc = npc,
                            onClick = { onNpcClick(npc.npcId) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateNpc,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = LocalizedStrings.npcAdd)
        }
    }
}
