package com.example.keepersnotes.ui.screen.groupdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.ui.screen.groupdetail.tab.*
import com.example.keepersnotes.util.LocalizedStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onPcClick: (String) -> Unit,
    onNpcClick: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    onMemoClick: (String) -> Unit = {},
    onCreatePc: () -> Unit = {},
    onCreateNpc: () -> Unit = {},
    onCreateSession: () -> Unit = {},
    onCreateMemo: () -> Unit = {},
    onNavigateToRelationship: () -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf(
        LocalizedStrings.groupOverview,
        LocalizedStrings.groupPcLibrary,
        LocalizedStrings.groupNpcArchive,
        LocalizedStrings.groupModuleContent,
        LocalizedStrings.groupSessionRecord,
        LocalizedStrings.groupKpMemo,
        "关系网"
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = uiState.group?.groupName ?: LocalizedStrings.groupDetailTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, maxLines = 1) }
                    )
                }
            }

            when (selectedTab) {
                0 -> GroupOverviewTab(
                    uiState = uiState,
                    onStatusChange = viewModel::updateGroupStatus
                )
                1 -> PcLibraryTab(
                    pcs = uiState.pcs,
                    onPcClick = onPcClick,
                    onCreatePc = onCreatePc
                )
                2 -> NpcArchiveTab(
                    npcs = uiState.npcs,
                    onNpcClick = onNpcClick,
                    onCreateNpc = onCreateNpc
                )
                3 -> ModuleContentTab(module = uiState.module)
                4 -> SessionRecordTab(
                    sessions = uiState.sessions,
                    onSessionClick = onSessionClick,
                    onCreateSession = onCreateSession
                )
                5 -> KpMemoTab(
                    memos = uiState.memos,
                    pendingTodos = uiState.pendingTodos,
                    onToggleCompleted = viewModel::toggleMemoCompleted,
                    onCreateMemo = onCreateMemo,
                    onMemoClick = onMemoClick
                )
                6 -> GroupRelationshipTab(
                    relationshipCount = uiState.relationships.size,
                    onNavigateToRelationship = onNavigateToRelationship
                )
            }
        }
    }
}

@Composable
private fun GroupRelationshipTab(
    relationshipCount: Int,
    onNavigateToRelationship: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AccountTree,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                LocalizedStrings.groupRelationshipTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "${LocalizedStrings.groupRelationshipPrefix}$relationshipCount${LocalizedStrings.groupRelationshipCount}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNavigateToRelationship) {
                Text(LocalizedStrings.groupRelationshipView)
            }
        }
    }
}
