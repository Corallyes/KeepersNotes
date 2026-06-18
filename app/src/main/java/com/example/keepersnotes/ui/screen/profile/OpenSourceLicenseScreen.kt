package com.example.keepersnotes.ui.screen.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.keepersnotes.ui.component.CompactTopBar
import com.example.keepersnotes.util.LocalizedStrings

data class LicenseEntry(
    val name: String,
    val license: String
)

private val thirdPartyLicenses = listOf(
    LicenseEntry("Jetpack Compose", "Apache License 2.0"),
    LicenseEntry("Material 3", "Apache License 2.0"),
    LicenseEntry("Room Database", "Apache License 2.0"),
    LicenseEntry("Hilt", "Apache License 2.0"),
    LicenseEntry("Navigation Compose", "Apache License 2.0"),
    LicenseEntry("Coil", "Apache License 2.0"),
    LicenseEntry("Markwon", "Apache License 2.0"),
    LicenseEntry("Apache POI", "Apache License 2.0"),
    LicenseEntry("WorkManager", "Apache License 2.0"),
    LicenseEntry("Kotlin", "Apache License 2.0"),
    LicenseEntry("Kotlin Coroutines", "Apache License 2.0"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicenseScreen(
    onBack: () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CompactTopBar(
                title = LocalizedStrings.licenseTitle,
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
        ) {
            // App license
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = LocalizedStrings.licenseAppName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CC BY-NC-SA 4.0",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = LocalizedStrings.licenseCcByNcSa,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = LocalizedStrings.licenseYouCan,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = LocalizedStrings.licenseShare,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = LocalizedStrings.licenseConditions,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = LocalizedStrings.licenseAttribution,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Third-party libraries
            Text(
                text = LocalizedStrings.licenseThirdParty,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            thirdPartyLicenses.forEach { entry ->
                ListItem(
                    headlineContent = {
                        Text(entry.name, fontWeight = FontWeight.Medium)
                    },
                    supportingContent = {
                        Text(entry.license, style = MaterialTheme.typography.bodySmall)
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
