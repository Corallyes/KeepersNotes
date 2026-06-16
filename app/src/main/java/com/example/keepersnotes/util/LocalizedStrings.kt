package com.example.keepersnotes.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object LocalizedStrings {

    val isEnglish: Boolean
        @Composable
        @ReadOnlyComposable
        get() = ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_ENGLISH ||
                (ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_SYSTEM && isSystemEnglish())

    private fun isSystemEnglish(): Boolean {
        return java.util.Locale.getDefault().language == "en"
    }

    // ==================== 开屏 ====================
    val splashTitle get() = if (isEnglishSync()) "Keeper's Notes" else "守密人笔记"
    val splashSubtitle get() = if (isEnglishSync()) "Behind the curtain, truth endures" else "帷幕之下，真相永存"

    // ==================== 通用 ====================
    val confirm get() = if (isEnglishSync()) "Confirm" else "确认"
    val cancel get() = if (isEnglishSync()) "Cancel" else "取消"
    val save get() = if (isEnglishSync()) "Save" else "保存"
    val delete get() = if (isEnglishSync()) "Delete" else "删除"
    val edit get() = if (isEnglishSync()) "Edit" else "编辑"
    val back get() = if (isEnglishSync()) "Back" else "返回"
    val search get() = if (isEnglishSync()) "Search" else "搜索"
    val loading get() = if (isEnglishSync()) "Loading..." else "加载中..."

    // ==================== 底部导航 ====================
    val navHome get() = if (isEnglishSync()) "Home" else "首页"
    val navGroups get() = if (isEnglishSync()) "Groups" else "团"
    val navModules get() = if (isEnglishSync()) "Modules" else "模组"
    val navProfile get() = if (isEnglishSync()) "Me" else "我的"

    // ==================== 设置 ====================
    val settingsTitle get() = if (isEnglishSync()) "Settings" else "设置"
    val settingsAppearance get() = if (isEnglishSync()) "Appearance" else "外观"
    val settingsTheme get() = if (isEnglishSync()) "Theme" else "外观模式"
    val settingsLanguage get() = if (isEnglishSync()) "Language" else "语言"
    val settingsFollowSystem get() = if (isEnglishSync()) "Follow System" else "跟随系统"
    val settingsLight get() = if (isEnglishSync()) "Light" else "浅色模式"
    val settingsLightShort get() = if (isEnglishSync()) "Light" else "浅色"
    val settingsDark get() = if (isEnglishSync()) "Dark" else "深色模式"
    val settingsDarkShort get() = if (isEnglishSync()) "Dark" else "深色"
    val settingsNotification get() = if (isEnglishSync()) "Notifications" else "通知"
    val settingsNotificationDesc get() = if (isEnglishSync()) "Set alarm and notification reminders" else "设置闹钟和通知提醒方式"
    val settingsData get() = if (isEnglishSync()) "Data" else "数据"
    val settingsAutoSave get() = if (isEnglishSync()) "Auto Save" else "自动保存"
    val settingsAutoSaveDesc get() = if (isEnglishSync()) "Auto save after editing" else "编辑后自动保存数据"
    val settingsAbout get() = if (isEnglishSync()) "About" else "关于"
    val settingsVersion get() = if (isEnglishSync()) "Version" else "版本"
    val settingsLicense get() = if (isEnglishSync()) "Open Source Licenses" else "开源许可"

    // ==================== 帮助中心 ====================
    val helpTitle get() = if (isEnglishSync()) "Help Center" else "帮助中心"
    val helpAnnouncement get() = if (isEnglishSync()) "Announcement" else "公告"
    val helpAnnouncementTitle get() = if (isEnglishSync()) "Welcome to Keeper's Notes" else "欢迎使用守密人笔记"
    val helpAnnouncementBody get() = if (isEnglishSync())
        "This app was created as a dedicated note-taking tool for Keepers." +
        "\n\nAbout document reading: Currently only supports converting Word to Markdown format. Due to encryption protection on original Word documents, direct parsing is not available. It may take some time to get used to." +
        "\n\nAbout feature completeness: The current version is a product of rapid iteration. Some features have not been fully tested. If you encounter issues, please contact us via email or GitHub. Please provide clear problem descriptions when giving feedback." +
        "\n\nThank you for using."
    else
        "本应用的开发初衷是为KP提供一款专用的带团备忘工具。" +
        "\n\n关于文档阅读：目前仅支持将Word转换为Markdown格式呈现。由于原始Word文档存在加密保护，暂时无法直接解析，初次使用可能需要一定适应。" +
        "\n\n关于功能完善度：当前版本为快速迭代的产物，部分功能尚未经过充分测试。如有问题，可通过邮箱或GitHub反馈，我会尽快处理。反馈时请尽量提供清晰的问题描述。" +
        "\n\n感谢使用。"

    val helpFaq get() = if (isEnglishSync()) "FAQ" else "常见问题"
    val helpContact get() = if (isEnglishSync()) "Contact Us" else "联系我们"
    val helpFeedback get() = if (isEnglishSync()) "Feedback & Suggestions" else "反馈与建议"
    val helpFeedbackDesc get() = if (isEnglishSync())
        "Welcome to submit bug reports or feature suggestions"
    else
        "欢迎提交Bug报告或功能建议，最近我在期末周可能回复不及时\n但是会尽量一直维护的"
    val helpEmail get() = if (isEnglishSync()) "Email" else "邮箱联系"
    val helpGithub get() = if (isEnglishSync()) "GitHub" else "GitHub"
    val helpExpand get() = if (isEnglishSync()) "Expand" else "展开"
    val helpCollapse get() = if (isEnglishSync()) "Collapse" else "收起"

    // ==================== FAQ ====================
    val faq1Q get() = if (isEnglishSync()) "How to create a new group?" else "如何创建一个新团？"
    val faq1A get() = if (isEnglishSync())
        "Click the \"New Group\" button on the home page, or click the + button in the \"My Groups\" page. Fill in the group name, select the game system, link a module, and you're done."
    else
        "在首页点击「新建团」按钮，或在「我的团」页面点击右下角的 + 按钮。填写团名、选择游戏系统、关联卷宗后即可创建。"

    val faq2Q get() = if (isEnglishSync()) "How to import a module?" else "如何导入卷宗？"
    val faq2A get() = if (isEnglishSync())
        "Click the upload button in the module library page. Select .txt or .docx files to import a single document, or .zip files for batch import (including documents and images). The system will automatically detect the chapter structure."
    else
        "在卷宗库页面点击右下角的上传按钮，选择 .txt 或 .docx 文件导入单个文档，或选择 .zip 文件批量导入（包含文档和图片）。系统会自动识别章节结构。"

    val faq3Q get() = if (isEnglishSync()) "What is the difference between PC and NPC?" else "PC和NPC有什么区别？"
    val faq3A get() = if (isEnglishSync())
        "PC (Player Character) is a player-controlled character. NPC (Non-Player Character) is controlled by the Keeper, and may contain hidden information such as true motives."
    else
        "PC（Player Character）是玩家角色，由玩家操控。NPC（Non-Player Character）是非玩家角色，由KP操控，包含隐藏信息如真实目的等。"

    val faq4Q get() = if (isEnglishSync()) "What are Secret Notes?" else "什么是暗线笔记？"
    val faq4A get() = if (isEnglishSync())
        "Secret Notes are memos visible only to the Keeper, used for recording plot secrets, NPC true motives, and other information that players should not see. Check \"Secret Note\" when creating a memo."
    else
        "暗线笔记是仅KP可见的备忘录，用于记录剧情暗线、NPC真实目的等不宜让玩家看到的信息。在创建备忘时勾选「暗线笔记」即可。"

    val faq5Q get() = if (isEnglishSync()) "How to backup data?" else "如何备份数据？"
    val faq5A get() = if (isEnglishSync())
        "Click \"Data Backup\" in the \"Me\" page to export all data as a backup file. It is recommended to backup before important operations."
    else
        "在「我的」页面点击「数据备份」，可以导出所有数据为备份文件。建议在重要操作前进行备份。"

    val faq6Q get() = if (isEnglishSync()) "What are Session Records for?" else "Session记录有什么用？"
    val faq6A get() = if (isEnglishSync())
        "Session Records are used to document each game session, including summaries, important events, discovered clues, etc. Useful for reviewing the story and preparing for the next session."
    else
        "Session记录用于记录每次跑团的内容，包括摘要、重要事件、发现的线索等。方便回顾剧情和准备下次跑团。"

    val faq7Q get() = if (isEnglishSync()) "What is the Relationship Network?" else "关系网是什么？"
    val faq7A get() = if (isEnglishSync())
        "The Relationship Network visualizes connections between PCs and NPCs. You can create associations between characters in the NPC archive, helping the Keeper manage complex character relationships."
    else
        "关系网用于可视化展示PC、NPC之间的关系。在NPC档案中可以为角色之间建立关联，帮助KP理清复杂的人物关系，避免带团时混淆。"

    val faq8Q get() = if (isEnglishSync()) "What is the difference between Module and Group?" else "模组和团有什么区别？"
    val faq8A get() = if (isEnglishSync())
        "A Module is a scenario resource containing plot, NPCs, clues, and other reusable content. A Group is a specific game instance linked to a particular module, PCs, and session records. The same module can be used for multiple groups, each with different progress and experiences."
    else
        "模组是跑团的剧本素材，包含剧情、NPC、线索等通用内容，可以被多个团复用。团是具体的跑团实例，关联了特定的模组、PC和Session记录。同一个模组可以开多个团，每个团的进度和体验各不相同。"

    // ==================== 我的 ====================
    val profileTitle get() = if (isEnglishSync()) "Me" else "我的"
    val profileUserDesc get() = if (isEnglishSync()) "Keeper's Notes User" else "守密人笔记 用户"
    val profileBackup get() = if (isEnglishSync()) "Data Backup" else "数据备份"
    val profileBackupDesc get() = if (isEnglishSync()) "Export / Import data" else "导出/导入数据"
    val profileAnnouncement get() = if (isEnglishSync()) "Announcements" else "公告"
    val profileAnnouncementDesc get() = if (isEnglishSync()) "View latest announcements and changelog" else "查看最新公告和更新日志"
    val profileHelp get() = if (isEnglishSync()) "Help Center" else "帮助中心"
    val profileHelpDesc get() = if (isEnglishSync()) "FAQ and feedback" else "常见问题与反馈"
    val profileSettings get() = if (isEnglishSync()) "Settings" else "设置"
    val profileSettingsDesc get() = if (isEnglishSync()) "Theme, notifications, etc." else "主题、通知等"
    val profileVersion get() = if (isEnglishSync()) "Keeper's Notes v1.1" else "守密人笔记 v1.1"

    // ==================== 首页 ====================
    val homeTitle get() = if (isEnglishSync()) "Keeper's Notes" else "守密人笔记"
    val homeDataOverview get() = if (isEnglishSync()) "Data Overview" else "数据概览"
    val homeCalendar get() = if (isEnglishSync()) "Schedule Calendar" else "日程日历"
    val homeDaySchedule get() = if (isEnglishSync()) "Day Schedule" else "当天日程"
    val homeUpcoming get() = if (isEnglishSync()) "Upcoming Sessions" else "近期开团计划"
    val homeNoUpcoming get() = if (isEnglishSync()) "No upcoming sessions" else "暂无近期开团计划"
    val homeQuickActions get() = if (isEnglishSync()) "Quick Actions" else "快速操作"
    val homeActiveGroups get() = if (isEnglishSync()) "Active Groups" else "进行中的团"
    val homeNoGroups get() = if (isEnglishSync()) "No groups yet. Click + to start" else "还没有创建任何团，点击右下角 + 开始吧"
    val homeTotalPcs get() = if (isEnglishSync()) "Total PCs" else "总PC数量"
    val homeNewGroup get() = if (isEnglishSync()) "New Group" else "新建团"
    val homeImportModule get() = if (isEnglishSync()) "Import Module" else "导入模组"
    val homeAddPc get() = if (isEnglishSync()) "Add PC" else "添加PC"
    val homeImportSuccess get() = if (isEnglishSync()) "Module imported successfully" else "模组导入成功"
    val homeImportFail get() = if (isEnglishSync()) "Import failed" else "导入失败"
    val homeNewVersion get() = if (isEnglishSync()) "New version found" else "发现新版本"
    val homeUpdateContent get() = if (isEnglishSync()) "Update content:" else "更新内容："
    val homeDownload get() = if (isEnglishSync()) "Download" else "去下载"
    val homeLater get() = if (isEnglishSync()) "Later" else "稍后"
    val homeImportDialogTitle get() = if (isEnglishSync()) "Import Module" else "导入模组"
    val homeModuleName get() = if (isEnglishSync()) "Module Name" else "模组名称"
    val homeAuthor get() = if (isEnglishSync()) "Author" else "作者"
    val homeGameSystem get() = if (isEnglishSync()) "Game System" else "游戏系统"
    val homeImport get() = if (isEnglishSync()) "Import" else "导入"
    val homeSelectGroup get() = if (isEnglishSync()) "Select Group" else "选择团"
    val homeNoGroupPrompt get() = if (isEnglishSync()) "No groups yet, please create one first" else "还没有创建任何团，请先创建一个团"

    // ==================== 团列表 ====================
    val groupListTitle get() = if (isEnglishSync()) "My Groups" else "我的团"
    val groupCreateTitle get() = if (isEnglishSync()) "Create Group" else "创建新团"
    val groupName get() = if (isEnglishSync()) "Group Name" else "团名"
    val groupStatusActive get() = if (isEnglishSync()) "Active" else "进行中"
    val groupStatusPaused get() = if (isEnglishSync()) "Paused" else "暂停"
    val groupStatusCompleted get() = if (isEnglishSync()) "Completed" else "已完结"
    val groupNoGroupsAll get() = if (isEnglishSync()) "No groups created yet" else "还没有创建任何团"
    val groupNoGroupsActive get() = if (isEnglishSync()) "No active groups" else "没有进行中的团"
    val groupNoGroupsPaused get() = if (isEnglishSync()) "No paused groups" else "没有暂停的团"
    val groupNoGroupsCompleted get() = if (isEnglishSync()) "No completed groups" else "没有已完结的团"
    val groupDetailTitle get() = if (isEnglishSync()) "Group Detail" else "团详情"
    val groupRelationshipTitle get() = if (isEnglishSync()) "Group Relationship Map" else "团关系图谱"
    val groupRelationshipCount get() = if (isEnglishSync()) " relationships" else "条关系"
    val groupRelationshipView get() = if (isEnglishSync()) "View Relationship Map" else "查看关系图谱"
    val groupRelationshipPrefix get() = if (isEnglishSync()) "Currently has " else "当前有 "

    // ==================== 模组库 ====================
    val moduleLibraryTitle get() = if (isEnglishSync()) "Module Library" else "模组库"
    val moduleImport get() = if (isEnglishSync()) "Import Module" else "导入模组"
    val moduleMyModules get() = if (isEnglishSync()) "My Modules" else "我的模组"
    val modulePublic get() = if (isEnglishSync()) "Public" else "公共模组"
    val moduleSearchPlaceholder get() = if (isEnglishSync()) "Search modules" else "搜索卷宗"
    val moduleImportZip get() = if (isEnglishSync()) "Import ZIP Module" else "导入 ZIP 卷宗"
    val moduleImportSingle get() = if (isEnglishSync()) "Import Single Document" else "导入单个文档"
    val moduleImportTitle get() = if (isEnglishSync()) "Import Module" else "导入卷宗"
    val moduleNameRequired get() = if (isEnglishSync()) "Module Name *" else "卷宗名称 *"
    val moduleNoModules get() = if (isEnglishSync()) "No modules yet" else "暂无卷宗"
    val moduleAll get() = if (isEnglishSync()) "All" else "全部"
    val moduleCustom get() = if (isEnglishSync()) "Custom" else "自定义"
    val moduleDeleteTitle get() = if (isEnglishSync()) "Delete Module" else "删除模组"
    val moduleDeleteConfirm get() = if (isEnglishSync()) "Are you sure you want to delete this module? This will also delete all highlights, annotations and bookmarks, and cannot be undone." else "确定要删除「${"\$"}{module.title}」吗？此操作将同时删除该模组的所有高亮、批注和书签，且不可撤销。"
    val moduleFavorite get() = if (isEnglishSync()) "Favorite" else "收藏"
    val moduleUnfavorite get() = if (isEnglishSync()) "Unfavorite" else "取消收藏"
    val moduleAuthor get() = if (isEnglishSync()) "Author: " else "作者: "
    val modulePlayers get() = if (isEnglishSync()) " players" else "人"
    val moduleSort get() = if (isEnglishSync()) "Sort" else "排序"

    // ==================== 团详情 ====================
    val groupOverview get() = if (isEnglishSync()) "Overview" else "概览"
    val groupPcLibrary get() = if (isEnglishSync()) "PC Library" else "PC角色库"
    val groupNpcArchive get() = if (isEnglishSync()) "NPC Archive" else "NPC档案"
    val groupModuleContent get() = if (isEnglishSync()) "Module Content" else "模组内容"
    val groupSessionRecord get() = if (isEnglishSync()) "Session Record" else "Session记录"
    val groupKpMemo get() = if (isEnglishSync()) "KP Memo" else "KP备忘录"

    // ==================== 备忘录 ====================
    val memoTitle get() = if (isEnglishSync()) "Memo" else "备忘录"
    val memoCreate get() = if (isEnglishSync()) "Create Memo" else "创建备忘"
    val memoSecret get() = if (isEnglishSync()) "Secret Note" else "暗线笔记"

    private fun isEnglishSync(): Boolean {
        return ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_ENGLISH ||
                (ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_SYSTEM &&
                    java.util.Locale.getDefault().language == "en")
    }
}
