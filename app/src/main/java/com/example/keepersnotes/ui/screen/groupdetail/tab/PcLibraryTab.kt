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
import com.example.keepersnotes.data.local.entity.PlayerCharacterEntity
import com.example.keepersnotes.ui.component.PcCard

@Composable
fun PcLibraryTab(
    pcs: List<PlayerCharacterEntity>,
    onPcClick: (String) -> Unit,
    onCreatePc: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredPcs = if (searchQuery.isBlank()) pcs
    else pcs.filter {
        it.characterName.contains(searchQuery, ignoreCase = true) ||
                it.playerName.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("搜索角色名或玩家名") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (filteredPcs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp)) {
                    Text(
                        text = if (searchQuery.isBlank()) "还没有添加PC角色" else "没有匹配的角色",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPcs, key = { it.pcId }) { pc ->
                        PcCard(
                            pc = pc,
                            onClick = { onPcClick(pc.pcId) }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreatePc,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "添加PC")
        }
    }
}
