package com.example.keepersnotes.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.keepersnotes.navigation.screen.DetailScreen
import com.example.keepersnotes.navigation.screen.Screen
import com.example.keepersnotes.ui.screen.groupdetail.*
import com.example.keepersnotes.ui.screen.grouplist.CreateGroupScreen
import com.example.keepersnotes.ui.screen.grouplist.GroupListScreen
import com.example.keepersnotes.ui.screen.home.HomeScreen
import com.example.keepersnotes.ui.screen.modulelibrary.EntityType
import com.example.keepersnotes.ui.screen.modulelibrary.ModuleDetailScreen
import com.example.keepersnotes.ui.screen.modulelibrary.ModuleEntityListScreen
import com.example.keepersnotes.ui.screen.modulelibrary.ModuleLibraryScreen
import com.example.keepersnotes.ui.screen.modulelibrary.ModuleReaderScreen
import com.example.keepersnotes.ui.screen.modulelibrary.ModuleRelationshipScreen
import com.example.keepersnotes.ui.screen.search.GlobalSearchScreen
import com.example.keepersnotes.ui.screen.collection.CollectionDetailScreen
import com.example.keepersnotes.ui.screen.collection.ArchiveReaderScreen
import com.example.keepersnotes.ui.screen.collection.ImageViewerScreen
import com.example.keepersnotes.ui.screen.profile.AnnouncementScreen
import com.example.keepersnotes.ui.screen.profile.BackupScreen
import com.example.keepersnotes.ui.screen.profile.HelpScreen
import com.example.keepersnotes.ui.screen.profile.NotificationSettingsScreen
import com.example.keepersnotes.ui.screen.profile.OpenSourceLicenseScreen
import com.example.keepersnotes.ui.screen.profile.ProfileScreen
import com.example.keepersnotes.ui.screen.profile.SettingsScreen
import com.example.keepersnotes.ui.screen.splash.BrandingScreen
import com.example.keepersnotes.ui.screen.splash.SplashScreen

@Composable
fun KeeperNotesNavGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Splash screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToBrand = {
                    navController.navigate(Screen.Brand.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Branding screen
        composable(Screen.Brand.route) {
            BrandingScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Brand.route) { inclusive = true }
                    }
                }
            )
        }

        // Bottom nav destinations
        composable(Screen.Home.route) {
            HomeScreen(
                onGroupClick = { groupId -> navController.navigate(DetailScreen.groupDetail(groupId)) },
                onCreateGroup = { navController.navigate(DetailScreen.CREATE_GROUP) },
                onNavigateToCreatePc = { groupId -> navController.navigate(DetailScreen.createPc(groupId)) },
                onNavigateToSearch = { navController.navigate(DetailScreen.GLOBAL_SEARCH) }
            )
        }

        composable(Screen.GroupList.route) {
            GroupListScreen(
                onGroupClick = { groupId -> navController.navigate(DetailScreen.groupDetail(groupId)) },
                onCreateGroup = { navController.navigate(DetailScreen.CREATE_GROUP) }
            )
        }

        composable(Screen.ModuleLibrary.route) {
            ModuleLibraryScreen(
                onModuleClick = { moduleId, isCollection ->
                    if (isCollection) {
                        navController.navigate(DetailScreen.collectionDetail(moduleId))
                    } else {
                        navController.navigate(DetailScreen.moduleDetail(moduleId))
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToAnnouncement = { navController.navigate(DetailScreen.ANNOUNCEMENT) },
                onNavigateToBackup = { navController.navigate(DetailScreen.BACKUP) },
                onNavigateToSettings = { navController.navigate(DetailScreen.SETTINGS) },
                onNavigateToHelp = { navController.navigate(DetailScreen.HELP) }
            )
        }

        // Announcement
        composable(DetailScreen.ANNOUNCEMENT) {
            AnnouncementScreen(onBack = { navController.popBackStack() })
        }

        // Backup
        composable(DetailScreen.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }

        // Settings
        composable(DetailScreen.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNotificationSettings = { navController.navigate(DetailScreen.NOTIFICATION_SETTINGS) },
                onNavigateToLicense = { navController.navigate(DetailScreen.OPEN_SOURCE_LICENSE) }
            )
        }

        // Open Source License
        composable(DetailScreen.OPEN_SOURCE_LICENSE) {
            OpenSourceLicenseScreen(onBack = { navController.popBackStack() })
        }

        // Notification Settings
        composable(DetailScreen.NOTIFICATION_SETTINGS) {
            NotificationSettingsScreen(onBack = { navController.popBackStack() })
        }

        // Help
        composable(DetailScreen.HELP) {
            HelpScreen(onBack = { navController.popBackStack() })
        }

        // Create group
        composable(DetailScreen.CREATE_GROUP) {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onGroupCreated = { groupId ->
                    navController.popBackStack()
                    navController.navigate(DetailScreen.groupDetail(groupId))
                }
            )
        }

        // Group detail
        composable(
            route = DetailScreen.GROUP_DETAIL,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onPcClick = { pcId -> navController.navigate(DetailScreen.pcDetail(pcId)) },
                onNpcClick = { npcId -> navController.navigate(DetailScreen.npcDetail(npcId)) },
                onSessionClick = { sessionId -> navController.navigate(DetailScreen.sessionDetail(sessionId)) },
                onMemoClick = { memoId -> navController.navigate(DetailScreen.memoDetail(memoId)) },
                onCreatePc = { navController.navigate(DetailScreen.createPc(groupId)) },
                onCreateNpc = { navController.navigate(DetailScreen.createNpc(groupId)) },
                onCreateSession = { navController.navigate(DetailScreen.createSession(groupId)) },
                onCreateMemo = { navController.navigate(DetailScreen.createMemo(groupId)) },
                onNavigateToRelationship = { navController.navigate(DetailScreen.groupRelationship(groupId)) }
            )
        }

        // PC detail
        composable(
            route = DetailScreen.PC_DETAIL,
            arguments = listOf(navArgument("pcId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pcId = backStackEntry.arguments?.getString("pcId") ?: return@composable
            PcDetailScreen(
                pcId = pcId,
                onBack = { navController.popBackStack() }
            )
        }

        // NPC detail
        composable(
            route = DetailScreen.NPC_DETAIL,
            arguments = listOf(navArgument("npcId") { type = NavType.StringType })
        ) { backStackEntry ->
            val npcId = backStackEntry.arguments?.getString("npcId") ?: return@composable
            NpcDetailScreen(
                npcId = npcId,
                onBack = { navController.popBackStack() }
            )
        }

        // Session detail
        composable(
            route = DetailScreen.SESSION_DETAIL,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: return@composable
            SessionDetailScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }

        // Memo detail
        composable(
            route = DetailScreen.MEMO_DETAIL,
            arguments = listOf(navArgument("memoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val memoId = backStackEntry.arguments?.getString("memoId") ?: return@composable
            MemoDetailScreen(
                memoId = memoId,
                onBack = { navController.popBackStack() }
            )
        }

        // Module reader
        composable(
            route = DetailScreen.MODULE_READER,
            arguments = listOf(
                navArgument("moduleId") { type = NavType.StringType },
                navArgument("chapterId") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: return@composable
            ModuleReaderScreen(
                moduleId = moduleId,
                onBack = { navController.popBackStack() }
            )
        }

        // Module detail
        composable(
            route = DetailScreen.MODULE_DETAIL,
            arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: return@composable
            ModuleDetailScreen(
                moduleId = moduleId,
                onBack = { navController.popBackStack() },
                onStartReading = { navController.navigate(DetailScreen.moduleReader(moduleId)) },
                onNavigateToChapter = { chapterId -> navController.navigate(DetailScreen.moduleReader(moduleId, chapterId)) },
                onImageClick = { imageIndex -> navController.navigate(DetailScreen.imageViewer(moduleId, imageIndex)) },
                onNavigateToRelationship = { navController.navigate(DetailScreen.moduleRelationship(moduleId)) },
                onNavigateToEntityList = { entityType -> navController.navigate(DetailScreen.moduleEntityList(moduleId, entityType)) }
            )
        }

        // Collection detail
        composable(
            route = DetailScreen.COLLECTION_DETAIL,
            arguments = listOf(navArgument("collectionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: return@composable
            CollectionDetailScreen(
                collectionId = collectionId,
                onBack = { navController.popBackStack() },
                onArchiveClick = { archiveId -> navController.navigate(DetailScreen.archiveReader(archiveId)) },
                onImageClick = { imageIndex -> navController.navigate(DetailScreen.imageViewer(collectionId, imageIndex)) }
            )
        }

        // Archive reader
        composable(
            route = DetailScreen.ARCHIVE_READER,
            arguments = listOf(navArgument("archiveId") { type = NavType.StringType })
        ) { backStackEntry ->
            val archiveId = backStackEntry.arguments?.getString("archiveId") ?: return@composable
            ArchiveReaderScreen(
                archiveId = archiveId,
                onBack = { navController.popBackStack() }
            )
        }

        // Image viewer
        composable(
            route = DetailScreen.IMAGE_VIEWER,
            arguments = listOf(
                navArgument("collectionId") { type = NavType.StringType },
                navArgument("imageIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val collectionId = backStackEntry.arguments?.getString("collectionId") ?: return@composable
            val imageIndex = backStackEntry.arguments?.getInt("imageIndex") ?: 0
            ImageViewerScreen(
                collectionId = collectionId,
                initialIndex = imageIndex,
                onBack = { navController.popBackStack() }
            )
        }

        // Create PC
        composable(
            route = DetailScreen.CREATE_PC,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            CreatePcScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        // Create NPC
        composable(
            route = DetailScreen.CREATE_NPC,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            CreateNpcScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        // Create Session
        composable(
            route = DetailScreen.CREATE_SESSION,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            CreateSessionScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        // Create Memo
        composable(
            route = DetailScreen.CREATE_MEMO,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            CreateMemoScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() }
            )
        }

        // Global Search
        composable(DetailScreen.GLOBAL_SEARCH) {
            GlobalSearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToModule = { moduleId -> navController.navigate(DetailScreen.moduleDetail(moduleId)) },
                onNavigateToMemo = { memoId -> navController.navigate(DetailScreen.memoDetail(memoId)) }
            )
        }

        // Module Relationship
        composable(
            route = DetailScreen.MODULE_RELATIONSHIP,
            arguments = listOf(navArgument("moduleId") { type = NavType.StringType })
        ) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId") ?: return@composable
            ModuleRelationshipScreen(
                moduleId = moduleId,
                onBack = { navController.popBackStack() }
            )
        }

        // Module Entity List
        composable(
            route = DetailScreen.MODULE_ENTITY_LIST,
            arguments = listOf(
                navArgument("moduleId") { type = NavType.StringType },
                navArgument("entityType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entityTypeStr = backStackEntry.arguments?.getString("entityType") ?: return@composable
            val entityType = try {
                EntityType.valueOf(entityTypeStr.uppercase())
            } catch (_: Exception) {
                EntityType.NPC
            }
            ModuleEntityListScreen(
                entityType = entityType,
                onBack = { navController.popBackStack() }
            )
        }

        // Group Relationship
        composable(
            route = DetailScreen.GROUP_RELATIONSHIP,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupRelationshipScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
