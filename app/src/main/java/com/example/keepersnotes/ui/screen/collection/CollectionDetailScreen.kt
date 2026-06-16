package com.example.keepersnotes.ui.screen.collection

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
import com.example.keepersnotes.ui.screen.collection.tab.ArchiveTab
import com.example.keepersnotes.ui.screen.collection.tab.ClueTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    onBack: () -> Unit,
    onArchiveClick: (String) -> Unit,
    onImageClick: (Int) -> Unit = {},
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("档案(${uiState.archives.size})", "线索(${uiState.images.size})")
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = uiState.collection?.title ?: "卷宗详情",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ArchiveTab(
                    archives = uiState.archives,
                    onArchiveClick = onArchiveClick
                )
                1 -> ClueTab(
                    images = uiState.images,
                    groups = uiState.groups,
                    selectedGroupId = uiState.selectedGroupId,
                    onImageClick = onImageClick,
                    onGroupSelect = viewModel::selectGroup,
                    onCreateGroup = viewModel::createGroup
                )
            }
        }
    }
}
