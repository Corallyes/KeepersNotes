package com.example.keepersnotes.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar

data class FaqItem(
    val question: String,
    val answer: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBack: () -> Unit
) {
    val faqs = listOf(
        FaqItem(
            question = "如何创建一个新团？",
            answer = "在首页点击「新建团」按钮，或在「我的团」页面点击右下角的 + 按钮。填写团名、选择游戏系统、关联卷宗后即可创建。"
        ),
        FaqItem(
            question = "如何导入卷宗？",
            answer = "在卷宗库页面点击右下角的上传按钮，选择 .txt 或 .docx 文件导入单个文档，或选择 .zip 文件批量导入（包含文档和图片）。系统会自动识别章节结构。"
        ),
        FaqItem(
            question = "PC和NPC有什么区别？",
            answer = "PC（Player Character）是玩家角色，由玩家操控。NPC（Non-Player Character）是非玩家角色，由KP操控，包含隐藏信息如真实目的等。"
        ),
        FaqItem(
            question = "什么是暗线笔记？",
            answer = "暗线笔记是仅KP可见的备忘录，用于记录剧情暗线、NPC真实目的等不宜让玩家看到的信息。在创建备忘时勾选「暗线笔记」即可。"
        ),
        FaqItem(
            question = "如何备份数据？",
            answer = "在「我的」页面点击「数据备份」，可以导出所有数据为备份文件。建议在重要操作前进行备份。"
        ),
        FaqItem(
            question = "Session记录有什么用？",
            answer = "Session记录用于记录每次跑团的内容，包括摘要、重要事件、发现的线索等。方便回顾剧情和准备下次跑团。"
        )
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = "帮助中心",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "常见问题",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(faqs) { faq ->
                FaqCard(faq = faq)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "联系我们",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ListItem(
                            headlineContent = { Text("反馈与建议") },
                            supportingContent = { Text("欢迎提交Bug报告或功能建议，最近我在期末周可能回复不及时\n但是会尽量一直维护的") },
                            leadingContent = { Icon(Icons.Default.Feedback, contentDescription = null) }
                        )
                        ListItem(
                            headlineContent = { Text("邮箱联系") },
                            supportingContent = { Text("1940401320@qq.com") },
                            leadingContent = { Icon(Icons.Default.Email, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "收起" else "展开"
                    )
                }
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
