package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePcScreen(
    groupId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreatePcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdPcId) {
        uiState.createdPcId?.let { onCreated(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "添加PC",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.playerName,
                onValueChange = viewModel::updatePlayerName,
                label = { Text("玩家昵称") },
                isError = uiState.playerNameError != null,
                supportingText = uiState.playerNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.characterName,
                onValueChange = viewModel::updateCharacterName,
                label = { Text("角色名称") },
                isError = uiState.characterNameError != null,
                supportingText = uiState.characterNameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // System selector
            Text("游戏系统", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(Constants.SYSTEM_COC7, Constants.SYSTEM_DND5E).forEach { s ->
                    FilterChip(
                        selected = uiState.system == s,
                        onClick = { viewModel.updateSystem(s) },
                        label = { Text(s) }
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.hpMax,
                    onValueChange = viewModel::updateHpMax,
                    label = { Text("HP上限") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = uiState.sanMax,
                    onValueChange = viewModel::updateSanMax,
                    label = { Text("SAN上限") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("创建")
                }
            }
        }
    }
}
