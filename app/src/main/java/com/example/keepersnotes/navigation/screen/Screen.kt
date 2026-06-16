package com.example.keepersnotes.navigation.screen

/**
 * App-level bottom navigation destinations
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Brand : Screen("brand")
    data object Home : Screen("home")
    data object GroupList : Screen("group_list")
    data object ModuleLibrary : Screen("module_library")
    data object Profile : Screen("profile")
}

/**
 * Detail-level destinations (navigated from bottom nav screens)
 */
object DetailScreen {
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val PC_DETAIL = "pc_detail/{pcId}"
    const val NPC_DETAIL = "npc_detail/{npcId}"
    const val SESSION_DETAIL = "session_detail/{sessionId}"
    const val MODULE_READER = "module_reader/{moduleId}?chapterId={chapterId}"
    const val MODULE_DETAIL = "module_detail/{moduleId}"
    const val CREATE_GROUP = "create_group"
    const val CREATE_PC = "create_pc/{groupId}"
    const val CREATE_NPC = "create_npc/{groupId}"
    const val CREATE_SESSION = "create_session/{groupId}"
    const val CREATE_MEMO = "create_memo/{groupId}"
    const val MEMO_DETAIL = "memo_detail/{memoId}"
    const val BACKUP = "backup"
    const val SETTINGS = "settings"
    const val HELP = "help"
    const val COLLECTION_DETAIL = "collection_detail/{collectionId}"
    const val ARCHIVE_READER = "archive_reader/{archiveId}"
    const val IMAGE_VIEWER = "image_viewer/{collectionId}/{imageIndex}"
    const val GLOBAL_SEARCH = "global_search"
    const val MODULE_RELATIONSHIP = "module_relationship/{moduleId}"
    const val MODULE_ENTITY_LIST = "module_entity_list/{moduleId}/{entityType}"
    const val GROUP_RELATIONSHIP = "group_relationship/{groupId}"
    const val NOTIFICATION_SETTINGS = "notification_settings"
    const val ANNOUNCEMENT = "announcement"
    const val OPEN_SOURCE_LICENSE = "open_source_license"

    fun groupDetail(groupId: String) = "group_detail/$groupId"
    fun pcDetail(pcId: String) = "pc_detail/$pcId"
    fun npcDetail(npcId: String) = "npc_detail/$npcId"
    fun sessionDetail(sessionId: String) = "session_detail/$sessionId"
    fun moduleReader(moduleId: String, chapterId: String? = null) =
        if (chapterId != null) "module_reader/$moduleId?chapterId=$chapterId"
        else "module_reader/$moduleId"
    fun moduleDetail(moduleId: String) = "module_detail/$moduleId"
    fun createPc(groupId: String) = "create_pc/$groupId"
    fun createNpc(groupId: String) = "create_npc/$groupId"
    fun createSession(groupId: String) = "create_session/$groupId"
    fun createMemo(groupId: String) = "create_memo/$groupId"
    fun memoDetail(memoId: String) = "memo_detail/$memoId"
    fun collectionDetail(collectionId: String) = "collection_detail/$collectionId"
    fun archiveReader(archiveId: String) = "archive_reader/$archiveId"
    fun imageViewer(collectionId: String, imageIndex: Int) = "image_viewer/$collectionId/$imageIndex"
    fun moduleRelationship(moduleId: String) = "module_relationship/$moduleId"
    fun moduleEntityList(moduleId: String, entityType: String) = "module_entity_list/$moduleId/$entityType"
    fun groupRelationship(groupId: String) = "group_relationship/$groupId"
}
