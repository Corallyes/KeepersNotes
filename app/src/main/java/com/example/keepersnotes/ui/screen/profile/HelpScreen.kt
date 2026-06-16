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
import com.example.keepersnotes.util.LocalizedStrings

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
        FaqItem(question = LocalizedStrings.faq1Q, answer = LocalizedStrings.faq1A),
        FaqItem(question = LocalizedStrings.faq2Q, answer = LocalizedStrings.faq2A),
        FaqItem(question = LocalizedStrings.faq3Q, answer = LocalizedStrings.faq3A),
        FaqItem(question = LocalizedStrings.faq4Q, answer = LocalizedStrings.faq4A),
        FaqItem(question = LocalizedStrings.faq5Q, answer = LocalizedStrings.faq5A),
        FaqItem(question = LocalizedStrings.faq6Q, answer = LocalizedStrings.faq6A),
        FaqItem(question = LocalizedStrings.faq7Q, answer = LocalizedStrings.faq7A),
        FaqItem(question = LocalizedStrings.faq8Q, answer = LocalizedStrings.faq8A)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.helpTitle,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = LocalizedStrings.back)
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
                    LocalizedStrings.helpAnnouncement,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            LocalizedStrings.helpAnnouncementTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            LocalizedStrings.helpAnnouncementBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    LocalizedStrings.helpFaq,
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
                    LocalizedStrings.helpContact,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ListItem(
                            headlineContent = { Text(LocalizedStrings.helpFeedback) },
                            supportingContent = { Text(LocalizedStrings.helpFeedbackDesc) },
                            leadingContent = { Icon(Icons.Default.Feedback, contentDescription = null) }
                        )
                        ListItem(
                            headlineContent = { Text(LocalizedStrings.helpEmail) },
                            supportingContent = { Text("1940401320@qq.com") },
                            leadingContent = { Icon(Icons.Default.Email, contentDescription = null) }
                        )
                        ListItem(
                            headlineContent = { Text(LocalizedStrings.helpGithub) },
                            supportingContent = { Text("https://github.com/Corallyes/KeepersNotes") },
                            leadingContent = { Icon(Icons.Default.Code, contentDescription = null) }
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
            Row(
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
                        contentDescription = if (expanded) LocalizedStrings.helpCollapse else LocalizedStrings.helpExpand
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
