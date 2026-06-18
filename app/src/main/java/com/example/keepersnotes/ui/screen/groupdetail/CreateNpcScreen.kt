package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.screen.modulelibrary.Gender
import com.example.keepersnotes.util.LocalizedStrings
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNpcScreen(
    groupId: String,
    onBack: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: CreateNpcViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var genderExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.createdNpcId) {
        uiState.createdNpcId?.let { onCreated(it) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.npcAdd,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text(LocalizedStrings.npcName) },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.alias,
                onValueChange = viewModel::updateAlias,
                label = { Text(LocalizedStrings.npcAlias) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            // Gender selector
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = it }
            ) {
                OutlinedTextField(
                    value = Gender.fromKey(uiState.gender)?.labelRes() ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(LocalizedStrings.pcGender) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    Gender.entries.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g.labelRes()) },
                            onClick = { viewModel.updateGender(g.key); genderExpanded = false }
                        )
                    }
                }
            }
            OutlinedTextField(
                value = uiState.occupation,
                onValueChange = viewModel::updateOccupation,
                label = { Text(LocalizedStrings.npcOccupation) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::updateDescription,
                label = { Text(LocalizedStrings.npcDescription) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            OutlinedTextField(
                value = uiState.truePurpose,
                onValueChange = viewModel::updateTruePurpose,
                label = { Text(LocalizedStrings.npcTruePurpose) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::submit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(LocalizedStrings.create)
                }
            }
        }
    }
}
