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
    val clear get() = if (isEnglishSync()) "Clear" else "清除"
    val create get() = if (isEnglishSync()) "Create" else "创建"
    val add get() = if (isEnglishSync()) "Add" else "添加"
    val edit get() = if (isEnglishSync()) "Edit" else "编辑"
    val back get() = if (isEnglishSync()) "Back" else "返回"
    val search get() = if (isEnglishSync()) "Search" else "搜索"
    val loading get() = if (isEnglishSync()) "Loading..." else "加载中..."
    val importing get() = if (isEnglishSync()) "Importing, please wait..." else "正在导入，请稍候..."

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
    val languageChinese get() = if (isEnglishSync()) "Chinese" else "中文"
    val languageEnglish get() = if (isEnglishSync()) "English" else "English"
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

    val faq7Q get() = if (isEnglishSync()) "What is the Character Relationship Network?" else "人物关系网是什么？"
    val faq7A get() = if (isEnglishSync())
        "The Character Relationship Network visualizes connections between characters. In module settings, you can define NPC relationships. In group details, you can manage PC and NPC relationships, helping the Keeper track complex character connections."
    else
        "人物关系网用于可视化展示人物之间的关系。在模组设置中可以定义NPC之间的关系，在团详情中可以管理PC和NPC之间的关系，帮助KP理清复杂的人物关系，避免带团时混淆。"

    val faq8Q get() = if (isEnglishSync()) "What is the difference between Module and Group?" else "模组和团有什么区别？"
    val faq8A get() = if (isEnglishSync())
        "A Module is a scenario resource containing plot, NPCs, clues, and other reusable content. A Group is a specific game instance linked to a particular module, PCs, and session records. The same module can be used for multiple groups, each with different progress and experiences."
    else
        "模组是跑团的剧本素材，包含剧情、NPC、线索等通用内容，可以被多个团复用。团是具体的跑团实例，关联了特定的模组、PC和Session记录。同一个模组可以开多个团，每个团的进度和体验各不相同。"

    val faq9Q get() = if (isEnglishSync()) "Why does the home tab stop working after using quick actions?" else "从首页快速操作后，底部导航栏的首页失效了？"
    val faq9A get() = if (isEnglishSync())
        "This is normal behavior. Quick actions (import module, new group, add PC) will navigate to the corresponding page after completion. Just press the back button to return to the normal home page."
    else
        "这是正常行为。首页的快速操作（导入模组、新建团、新增PC）执行完成后会自动跳转到对应的页面，此时底部导航栏的「首页」看起来像是失效了。只需按一下系统返回键即可回到正常的首页。"

    val faq10Q get() = if (isEnglishSync()) "How do clues work?" else "线索功能怎么用？"
    val faq10A get() = if (isEnglishSync())
        "Clues are managed in two layers: 1) Module level: go to Module Settings → Clues to add default clues for the module. 2) Group level: when creating a new group and selecting a module, the module's default clues are automatically inherited as KP memos. You can also manually add, edit, or delete clues in the group's KP Memos tab."
    else
        "线索分为两层管理：1）模组层：进入模组设置 → 线索，可以为模组添加默认线索，方便KP管理通用线索。2）团层：新建团时选择模组后，模组中设置的默认线索会自动继承为团的KP备忘录（类型为「线索」）。在团详情的KP备忘录标签页中，也可以手动新增、编辑或删除线索。"

    val faq11Q get() = if (isEnglishSync()) "What memo types are available?" else "备忘录有哪些类型？"
    val faq11A get() = if (isEnglishSync())
        "KP Memos support 6 types: Todo (checklist), Reminder (with optional timed notification), Rule Notes, Plot Notes, Clue Notes, and Secret Notes (hidden from players). Use the filter tabs to quickly find specific types."
    else
        "KP备忘录支持6种类型：待办（可勾选的清单）、提醒（可设置定时通知）、规则笔记、剧情笔记、线索笔记、暗线笔记（标记为暗线后仅KP可见）。在KP备忘录页面顶部可以通过筛选标签快速查看特定类型的备忘。"

    val faq12Q get() = if (isEnglishSync()) "How to use the calendar?" else "日历功能怎么用？"
    val faq12A get() = if (isEnglishSync())
        "The home page calendar shows all scheduled events. When creating a group, you can set the start time, expected end time, and default session time. The system will automatically create calendar events for each day in the range. You can also manually add, edit, or delete events."
    else
        "首页日历会显示所有已安排的日程。新建团时可以设置开始时间、预计结束时间和默认开团时间，系统会自动为时间范围内的每一天创建日历事件。也可以在日历中手动新增、编辑或删除日程。"

    val faq13Q get() = if (isEnglishSync()) "What file formats are supported for import?" else "支持导入哪些文件格式？"
    val faq13A get() = if (isEnglishSync())
        "Supported document formats: .txt, .docx, .md. Supported archive formats: .zip, .rar, .7z (containing documents and images). The system auto-detects encoding for text files and recognizes chapter structure in Word documents.\n\n⚠️ Note: PDF format cannot be parsed. Even PDF-to-Word conversions mostly just embed pages as images into Word, which also cannot be parsed. To import PDF content, please copy and paste the text manually."
    else
        "支持的文档格式：.txt、.docx、.md。支持的压缩包格式：.zip、.rar、.7z（可包含文档和图片）。文本文件会自动检测编码（UTF-8/GBK/GB18030），Word文档会智能识别章节结构。\n\n⚠️ 注意：PDF格式无法识别解析。即使是PDF转Word的文件，大部分也只是将页面转为图片嵌入Word，同样无法解析文字内容。如需导入PDF内容，请手动复制文字后粘贴。"

    // ==================== 我的 ====================
    val profileTitle get() = if (isEnglishSync()) "Me" else "我的"
    val profileUserDesc get() = if (isEnglishSync()) "Keeper's Notes User" else "守密人笔记 用户"
    val profileBackup get() = if (isEnglishSync()) "Data Backup" else "数据备份"
    val profileBackupDesc get() = if (isEnglishSync()) "Export / Import all data" else "导出/导入全部数据"

    // ==================== 数据备份 ====================
    val backupExportTitle get() = if (isEnglishSync()) "Export Data" else "导出数据"
    val backupExportDesc get() = if (isEnglishSync()) "Export all groups, characters, modules, memos, highlights, annotations, images and settings as a backup file" else "将所有团、角色、模组、备忘录、高亮、批注、图片和设置导出为备份文件"
    val backupExportButton get() = if (isEnglishSync()) "Export Backup" else "导出备份"
    val backupExporting get() = if (isEnglishSync()) "Exporting..." else "导出中..."
    val backupExportSuccess get() = if (isEnglishSync()) "Backup exported successfully" else "备份导出成功"
    val backupExportFail get() = if (isEnglishSync()) "Export failed" else "导出失败"
    val backupImportTitle get() = if (isEnglishSync()) "Import Data" else "导入数据"
    val backupImportDesc get() = if (isEnglishSync()) "Restore data from a backup file (will overwrite all existing data)" else "从备份文件恢复数据（将覆盖所有现有数据）"
    val backupImportButton get() = if (isEnglishSync()) "Import Backup" else "导入备份"
    val backupImporting get() = if (isEnglishSync()) "Importing..." else "导入中..."
    val backupImportSuccess get() = if (isEnglishSync()) "Data restored successfully. Please restart the app." else "数据恢复成功，请重启应用以加载最新数据"
    val backupImportFail get() = if (isEnglishSync()) "Import failed" else "导入失败"
    val backupImportConfirmTitle get() = if (isEnglishSync()) "Confirm Import" else "确认导入"
    val backupImportConfirmMessage get() = if (isEnglishSync()) "Importing will overwrite ALL existing data including groups, characters, modules, and settings. This action cannot be undone. Continue?" else "导入将覆盖所有现有数据，包括团、角色、模组和设置。此操作不可撤销。是否继续？"
    val backupInfoText get() = if (isEnglishSync()) "Backup includes all local data: database, images, and settings. It is recommended to export a backup before updating the APP. Overwriting installation (without uninstalling) preserves data automatically, but backup is an extra safety measure." else "备份文件包含所有本地数据：数据库、图片和设置。更新 APP 前建议先导出备份。覆盖安装（不卸载）数据会自动保留，备份是额外的安全保障。"

    // ==================== 通知设置 ====================
    val notificationTitle get() = if (isEnglishSync()) "Notification Settings" else "通知提醒"
    val notificationAlarm get() = if (isEnglishSync()) "Alarm Reminder" else "闹钟提醒"
    val notificationAlarmDesc get() = if (isEnglishSync()) "Use system alarm for reminders, louder sound, for important events" else "使用系统闹钟进行提醒，声音较大，适合重要日程"
    val notificationAlarmEnable get() = if (isEnglishSync()) "Enable Alarm" else "开启闹钟提醒"
    val notificationSystem get() = if (isEnglishSync()) "System Notification" else "系统通知提醒"
    val notificationSystemDesc get() = if (isEnglishSync()) "Use notification bar for reminders, gentler" else "使用系统通知栏提醒，较为温和"
    val notificationSystemEnable get() = if (isEnglishSync()) "Enable Notification" else "开启通知提醒"
    val notificationAdvanceTime get() = if (isEnglishSync()) "Remind Before" else "提前提醒时间"
    val notificationAdvanceDesc get() = if (isEnglishSync()) "How many minutes before the event" else "开团前多少分钟提醒"
    fun notificationMinutes(m: Int) = if (isEnglishSync()) "${m}min" else "${m}分钟"
    val notificationInfoText get() = if (isEnglishSync())
        "Reminder notes:\n• Alarm reminder: Uses system alarm with sound and vibration\n• System notification: Shows silent notification in notification bar\n• Only events with 'Reminder' enabled will trigger reminders\n• Session start and end dates created with a group have reminders enabled by default"
    else
        "提醒说明：\n• 闹钟提醒：使用系统闹钟，会发出声音和震动\n• 系统通知：在通知栏显示静默通知\n• 只有开启了「提醒」的日程才会触发提醒\n• 创建团时设置的开团日和结束日默认开启提醒"
    val notificationPermissionTitle get() = if (isEnglishSync()) "Notification Permission Required" else "需要通知权限"
    val notificationPermissionDesc get() = if (isEnglishSync()) "To receive reminders, please grant notification permission in system settings" else "为了接收提醒通知，请在系统设置中授予通知权限"
    val notificationPermissionGrant get() = if (isEnglishSync()) "Grant Permission" else "授予权限"
    val notificationPermissionDenied get() = if (isEnglishSync()) "Notification permission denied. Reminders will not work." else "通知权限被拒绝，提醒功能将无法使用"
    val alarmPermissionTitle get() = if (isEnglishSync()) "Alarm Permission Required" else "需要闹钟权限"
    val alarmPermissionDesc get() = if (isEnglishSync()) "To use alarm reminders, please grant 'Alarms & reminders' permission in system settings" else "要使用闹钟提醒功能，请在系统设置中授予「闹钟和提醒」权限"
    val alarmPermissionGrant get() = if (isEnglishSync()) "Open Settings" else "去设置"

    val profileAnnouncement get() = if (isEnglishSync()) "Announcements" else "公告"
    val profileAnnouncementDesc get() = if (isEnglishSync()) "View latest announcements and changelog" else "查看最新公告和更新日志"
    val announcementTitle get() = if (isEnglishSync()) "Announcements" else "公告"
    val announcementLatest get() = if (isEnglishSync()) "Latest Announcements" else "最新公告"
    val announcementNoLatest get() = if (isEnglishSync()) "No announcements" else "暂无公告"
    val announcementChangelog get() = if (isEnglishSync()) "Changelog" else "更新日志"
    val announcementNoChangelog get() = if (isEnglishSync()) "No changelog" else "暂无更新日志"
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
    val homeUpcoming get() = if (isEnglishSync()) "Today's Sessions" else "今日开团计划"
    val homeNoUpcoming get() = if (isEnglishSync()) "No sessions today" else "今日暂无开团计划"
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
    val homeCompleted get() = if (isEnglishSync()) "Completed" else "已完成"
    val homeActive get() = if (isEnglishSync()) "Active" else "进行中"
    val homeThisWeek get() = if (isEnglishSync()) "This Week" else "本周"

    // ==================== 日程事件 ====================
    val eventDetail get() = if (isEnglishSync()) "Event Detail" else "日程详情"
    val eventName get() = if (isEnglishSync()) "Event Name" else "日程名称"
    val eventTime get() = if (isEnglishSync()) "Time" else "时间"
    val eventNotSet get() = if (isEnglishSync()) "Not set" else "未设置"
    val eventRemind get() = if (isEnglishSync()) "Remind" else "提醒"
    val eventTypeLabel get() = if (isEnglishSync()) "Type: " else "类型："
    val eventTypeSessionStart get() = if (isEnglishSync()) "Session Start" else "开团日"
    val eventTypeSessionEnd get() = if (isEnglishSync()) "Expected End" else "预计结束"
    val eventTypeSession get() = if (isEnglishSync()) "In Session" else "开团中"
    val eventTypeMemoReminder get() = if (isEnglishSync()) "Memo Reminder" else "备忘录提醒"
    val eventTypeCustom get() = if (isEnglishSync()) "Custom Event" else "自定义日程"
    val eventDeleteTitle get() = if (isEnglishSync()) "Confirm Delete" else "确认删除"
    val eventDeleteConfirm get() = if (isEnglishSync()) "Are you sure you want to delete this event?" else "确定要删除这个日程吗？"
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
    val groupCreateName get() = if (isEnglishSync()) "Group Name" else "团名称"
    val groupCreateModuleName get() = if (isEnglishSync()) "Module Name" else "模组名称"
    val groupCreateSystem get() = if (isEnglishSync()) "Game System" else "游戏系统"
    val groupCreateFormat get() = if (isEnglishSync()) "Session Format" else "开团方式"
    val groupCreateFormatPlaceholder get() = if (isEnglishSync()) "e.g. Online, Offline, Both" else "如 线上、线下、线上线下"
    val groupCreateScale get() = if (isEnglishSync()) "Scale" else "规模"
    val groupCreateScalePlaceholder get() = if (isEnglishSync()) "e.g. 3-5 players" else "如 3-5人"
    val groupCreateStartTime get() = if (isEnglishSync()) "Start Date" else "开团时间"
    val groupCreateEndTime get() = if (isEnglishSync()) "Expected End" else "预计结束"
    val groupCreateClickSelect get() = if (isEnglishSync()) "Click to select" else "点击选择"
    val groupCreateTime get() = if (isEnglishSync()) "Default Session Time" else "默认开团时间"
    val groupCreateTimePlaceholder get() = if (isEnglishSync()) "Click to select time" else "点击选择时间"
    val groupCreateTimeTitle get() = if (isEnglishSync()) "Select Default Session Time" else "选择默认开团时间"
    val groupCreateSubmit get() = if (isEnglishSync()) "Create" else "创建"
    val groupDeleteTitle get() = if (isEnglishSync()) "Delete Group" else "删除团"
    fun groupDeleteConfirm(name: String) = if (isEnglishSync()) "Are you sure you want to delete「$name」? This cannot be undone. All data in this group will be deleted." else "确定要删除「$name」吗？此操作不可撤销，团内所有数据将被删除。"
    val groupName get() = if (isEnglishSync()) "Group Name" else "团名"
    val groupStatusActive get() = if (isEnglishSync()) "Active" else "进行中"
    val groupStatusPaused get() = if (isEnglishSync()) "Paused" else "暂停"
    val groupStatusCompleted get() = if (isEnglishSync()) "Completed" else "已完结"
    val groupNoGroupsAll get() = if (isEnglishSync()) "No groups created yet" else "还没有创建任何团"
    val groupNoGroupsActive get() = if (isEnglishSync()) "No active groups" else "没有进行中的团"
    val groupNoGroupsPaused get() = if (isEnglishSync()) "No paused groups" else "没有暂停的团"
    val groupNoGroupsCompleted get() = if (isEnglishSync()) "No completed groups" else "没有已完结的团"
    val groupDetailTitle get() = if (isEnglishSync()) "Group Detail" else "团详情"
    val groupRelationshipTitle get() = if (isEnglishSync()) "Character Relationship Map" else "人物关系图谱"
    val groupRelationshipCount get() = if (isEnglishSync()) " relationships" else "条关系"
    val groupRelationshipView get() = if (isEnglishSync()) "View Relationship Map" else "查看人物关系图"
    val groupRelationshipPrefix get() = if (isEnglishSync()) "Currently has " else "当前有 "
    val groupRelationshipExport get() = if (isEnglishSync()) "Export as Image" else "导出为图片"
    val groupRelationshipExporting get() = if (isEnglishSync()) "Exporting image..." else "正在导出图片..."
    val groupRelationshipExportSuccess get() = if (isEnglishSync()) "Image saved to gallery" else "图片已保存到相册"
    val groupRelationshipExportFail get() = if (isEnglishSync()) "Export failed" else "导出失败"
    val groupRelationshipExportEmpty get() = if (isEnglishSync()) "No data to export" else "暂无数据可导出"
    val groupModuleName get() = if (isEnglishSync()) "Module" else "模组"
    val groupStatusResume get() = if (isEnglishSync()) "Resume" else "恢复进行"
    val groupStatusPause get() = if (isEnglishSync()) "Pause" else "暂停"
    val groupStatusComplete get() = if (isEnglishSync()) "Complete" else "完结"
    val groupSessions get() = if (isEnglishSync()) "Sessions" else "Session数"
    val groupTodos get() = if (isEnglishSync()) "Todos" else "待办"
    val groupLastSession get() = if (isEnglishSync()) "Last Session" else "上次Session"
    val session get() = if (isEnglishSync()) "Session" else "Session"
    val sessionDetail get() = if (isEnglishSync()) "Session Detail" else "Session详情"
    val sessionNoSessions get() = if (isEnglishSync()) "No session records yet" else "还没有Session记录"
    val sessionAdd get() = if (isEnglishSync()) "Add Session Record" else "添加Session记录"
    val sessionDate get() = if (isEnglishSync()) "Date" else "日期"
    val sessionDuration get() = if (isEnglishSync()) "Duration" else "时长"
    val sessionSummary get() = if (isEnglishSync()) "Session Summary" else "本场摘要"
    val sessionNextNotes get() = if (isEnglishSync()) "Next Session Preview" else "下局预告"
    val sessionAddRecord get() = if (isEnglishSync()) "Add Record" else "添加记录"
    val sessionDurationMinutes get() = if (isEnglishSync()) "Duration (minutes)" else "时长（分钟）"
    val sessionParticipants get() = if (isEnglishSync()) "Participating PCs" else "参与PC"
    val sessionNoPcs get() = if (isEnglishSync()) "No PC characters in this group" else "该团暂无PC角色"
    val sessionImportantEvents get() = if (isEnglishSync()) "Important Events" else "重要事件"
    val sessionAddEvent get() = if (isEnglishSync()) "Add important event..." else "添加重要事件..."
    val sessionCluesFound get() = if (isEnglishSync()) "Clues Found" else "发现线索"
    val sessionAddClue get() = if (isEnglishSync()) "Add clue found..." else "添加发现的线索..."
    val groupPendingTodos get() = if (isEnglishSync()) "Pending Todos" else "待办事项"
    fun groupMoreItems(count: Int) = if (isEnglishSync()) "...and $count more" else "...还有${count}项"

    // ==================== 模组库 ====================
    val moduleLibraryTitle get() = if (isEnglishSync()) "Module Library" else "模组库"
    val moduleImport get() = if (isEnglishSync()) "Import Module" else "导入模组"
    val moduleMyModules get() = if (isEnglishSync()) "My Modules" else "我的模组"
    val modulePublic get() = if (isEnglishSync()) "Public" else "公共模组"
    val moduleSearchPlaceholder get() = if (isEnglishSync()) "Search modules" else "搜索卷宗"
    val moduleImportZip get() = if (isEnglishSync()) "Import ZIP Module" else "导入 ZIP 卷宗"
    val moduleImportArchive get() = if (isEnglishSync()) "Import Archive (ZIP/RAR/7Z)" else "导入压缩包 (ZIP/RAR/7Z)"
    val moduleImportSingle get() = if (isEnglishSync()) "Import Single Document" else "导入单个文档"
    val moduleImportTitle get() = if (isEnglishSync()) "Import Module" else "导入卷宗"
    val moduleNameRequired get() = if (isEnglishSync()) "Module Name *" else "卷宗名称 *"
    val moduleNoModules get() = if (isEnglishSync()) "No modules yet" else "暂无卷宗"
    val moduleAll get() = if (isEnglishSync()) "All" else "全部"
    val moduleCustom get() = if (isEnglishSync()) "Custom" else "自定义"
    val moduleDeleteTitle get() = if (isEnglishSync()) "Delete Module" else "删除模组"
    fun moduleDeleteConfirm(title: String) = if (isEnglishSync()) "Are you sure you want to delete「$title」? This will also delete all highlights, annotations and bookmarks, and cannot be undone." else "确定要删除「$title」吗？此操作将同时删除该模组的所有高亮、批注和书签，且不可撤销。"
    val moduleFavorite get() = if (isEnglishSync()) "Favorite" else "收藏"
    val moduleUnfavorite get() = if (isEnglishSync()) "Unfavorite" else "取消收藏"
    val moduleAuthor get() = if (isEnglishSync()) "Author: " else "作者: "
    val modulePlayers get() = if (isEnglishSync()) " players" else "人"
    val moduleSort get() = if (isEnglishSync()) "Sort" else "排序"
    val moduleDetail get() = if (isEnglishSync()) "Details" else "详情"
    val moduleImages get() = if (isEnglishSync()) "Images" else "图片"
    val moduleNotes get() = if (isEnglishSync()) "Notes" else "笔记"
    val moduleSettings get() = if (isEnglishSync()) "Module Settings" else "模组设置"
    val moduleNotFound get() = if (isEnglishSync()) "Module not found" else "模组不存在"
    val moduleStartReading get() = if (isEnglishSync()) "Start Reading" else "开始阅读"
    val moduleAddImage get() = if (isEnglishSync()) "Add Image" else "添加图片"
    val moduleImageLost get() = if (isEnglishSync()) "Image lost" else "图片丢失"
    val moduleNoImages get() = if (isEnglishSync()) "No images yet. Click + to add" else "暂无图片，点击右下角 + 添加"
    val moduleNoNotes get() = if (isEnglishSync()) "No reading notes" else "暂无阅读笔记"
    val moduleSynopsis get() = if (isEnglishSync()) "Synopsis" else "简介"
    val moduleReadingStats get() = if (isEnglishSync()) "Reading Statistics" else "阅读统计"
    val moduleReadingTime get() = if (isEnglishSync()) "Reading Time" else "阅读时间"
    val moduleHighlights get() = if (isEnglishSync()) "Highlights" else "高亮"
    val moduleAnnotations get() = if (isEnglishSync()) "Annotations" else "批注"
    val moduleBookmarks get() = if (isEnglishSync()) "Bookmarks" else "书签"
    val moduleTags get() = if (isEnglishSync()) "Tags" else "标签"
    val moduleReadingNotes get() = if (isEnglishSync()) "Reading Notes" else "阅读笔记"
    val moduleChapterList get() = if (isEnglishSync()) "Chapter List" else "章节目录"
    val moduleChapterCount get() = if (isEnglishSync()) " chapters" else " 章"
    val moduleDefaultNpc get() = if (isEnglishSync()) "Default NPCs" else "默认NPC"
    val moduleLocations get() = if (isEnglishSync()) "Locations" else "地点"
    val moduleOrganizations get() = if (isEnglishSync()) "Organizations" else "组织"
    val moduleClues get() = if (isEnglishSync()) "Clues" else "线索"
    val moduleDeleteImage get() = if (isEnglishSync()) "Delete Image" else "删除图片"
    fun moduleDeleteImageConfirm(title: String) = if (isEnglishSync()) "Are you sure you want to delete「$title」?" else "确定要删除「$title」吗？"
    val moduleSelectFromLibrary get() = if (isEnglishSync()) "Pick from Library" else "从库中选取"
    val moduleNoModulesImport get() = if (isEnglishSync()) "No modules. Please import first." else "暂无模组，请先导入"
    val moduleConfirmSelect get() = if (isEnglishSync()) "Confirm Module Selection" else "确认选择模组"
    fun moduleConfirmSelectDesc(title: String) = if (isEnglishSync()) "Select「$title」as this group's module?\n\nOnce confirmed, future module edits won't affect this group. This group will inherit the module's default PCs, NPCs, and relationships." else "确定要选择「$title」作为本团模组吗？\n\n模组一旦确定，后续对模组的修改不会影响到此团。本团将继承当前模组的默认PC、NPC和人物关系网。"
    val moduleSelectTitle get() = if (isEnglishSync()) "Select Module" else "选择模组"
    val moduleSearchName get() = if (isEnglishSync()) "Search module name" else "搜索模组名称"

    // ==================== 团详情 ====================
    val groupOverview get() = if (isEnglishSync()) "Overview" else "概览"
    val groupPcLibrary get() = if (isEnglishSync()) "PC Library" else "PC角色库"
    val groupNpcArchive get() = if (isEnglishSync()) "NPC Archive" else "NPC档案"
    val groupSessionRecord get() = if (isEnglishSync()) "Session Record" else "Session记录"
    val groupKpMemo get() = if (isEnglishSync()) "KP Memo" else "KP备忘录"
    val groupCharacters get() = if (isEnglishSync()) "Characters" else "人物"
    val groupRelationships get() = if (isEnglishSync()) "Relationships" else "人物关系"
    val groupDeleteConfirmAll get() = if (isEnglishSync()) "Are you sure you want to delete this group? All PCs, NPCs, session records and other data will be deleted. This cannot be undone." else "确定要删除这个团吗？团内所有PC、NPC、Session记录等数据都会被删除，此操作不可撤销。"
    val groupEditTitle get() = if (isEnglishSync()) "Edit Group" else "编辑团"
    val groupStatus get() = if (isEnglishSync()) "Status" else "状态"
    val groupRemarks get() = if (isEnglishSync()) "Remarks" else "备注"
    val groupScheduleOverwrite get() = if (isEnglishSync()) "Schedule Will Be Overwritten" else "日程将被覆盖"
    val groupScheduleOverwriteDesc get() = if (isEnglishSync()) "Changing the start date, end date or default session time will reset the calendar schedule for this group." else "修改了开团时间、预计结束或默认开团时间，该团在日历上的日程将被重置为当前设置。"
    val groupSelectTime get() = if (isEnglishSync()) "Select Default Session Time" else "选择默认开团时间"
    val groupLoading get() = if (isEnglishSync()) "Loading..." else "加载中..."

    // ==================== 备忘录 ====================
    val memoTypes = mapOf(
        "todo" to Pair("Todo", "待办"),
        "reminder" to Pair("Reminder", "提醒"),
        "rule" to Pair("Rule Notes", "规则笔记"),
        "plot" to Pair("Plot Notes", "剧情笔记"),
        "clue" to Pair("Clue Notes", "线索笔记"),
        "hidden" to Pair("Secret Notes", "暗线笔记")
    )
    fun memoType(type: String) = memoTypes[type]?.let { if (isEnglishSync()) it.first else it.second } ?: type
    val memoAdd get() = if (isEnglishSync()) "Add Memo" else "添加备忘"
    val memoDetail get() = if (isEnglishSync()) "Memo Detail" else "备忘详情"
    val memoEdit get() = if (isEnglishSync()) "Edit Memo" else "编辑备忘"
    val memoTypeLabel get() = if (isEnglishSync()) "Type" else "类型"
    val memoFilterAll get() = if (isEnglishSync()) "All" else "全部"
    val memoNoMemos get() = if (isEnglishSync()) "No memos yet" else "还没有备忘录"
    fun memoNoMemosType(type: String) = if (isEnglishSync()) "No $type memos" else "没有${type}类型的备忘"
    val memoTitle get() = if (isEnglishSync()) "Title" else "标题"
    val memoContent get() = if (isEnglishSync()) "Content" else "内容"
    val memoContentPlaceholder get() = if (isEnglishSync()) "Enter memo content, supports Markdown..." else "输入备忘内容，支持Markdown格式..."
    val memoLinkedChapter get() = if (isEnglishSync()) "Linked Chapter (optional)" else "关联章节（可选）"
    val memoSelectChapter get() = if (isEnglishSync()) "Select linked chapter" else "选择关联的章节"
    val memoPriority get() = if (isEnglishSync()) "Priority" else "优先级"
    val memoPriorityNormal get() = if (isEnglishSync()) "Normal" else "普通"
    val memoPriorityImportant get() = if (isEnglishSync()) "Important" else "重要"
    val memoPriorityUrgent get() = if (isEnglishSync()) "Urgent" else "紧急"
    val memoNotification get() = if (isEnglishSync()) "Timed Notification" else "定时通知"
    val memoNotificationEnable get() = if (isEnglishSync()) "Enable timed reminder" else "开启定时提醒"
    val memoSelectDate get() = if (isEnglishSync()) "Select date" else "选择日期"
    val memoSelectTime get() = if (isEnglishSync()) "Select Time" else "选择时间"
    val memoDeleteTitle get() = if (isEnglishSync()) "Delete Memo" else "删除备忘"
    fun memoDeleteConfirm(title: String) = if (isEnglishSync()) "Are you sure you want to delete「$title」? This cannot be undone." else "确定要删除「$title」吗？此操作不可撤销。"
    val memoLinkedContent get() = if (isEnglishSync()) "Linked Content" else "关联内容"
    val memoTags get() = if (isEnglishSync()) "Tags" else "标签"
    val memoTagsPlaceholder get() = if (isEnglishSync()) "Tags (comma separated)" else "标签（逗号分隔）"
    val memoCompleted get() = if (isEnglishSync()) "Completed" else "已完成"
    val memoNotCompleted get() = if (isEnglishSync()) "Not completed" else "未完成"
    val memoSecret get() = if (isEnglishSync()) "Secret" else "暗线"
    val memoSecretOnlyKp get() = if (isEnglishSync()) "Secret note (KP only)" else "暗线笔记（仅KP可见）"
    val memoUncomplete get() = if (isEnglishSync()) "Mark incomplete" else "取消完成"
    val memoMarkComplete get() = if (isEnglishSync()) "Mark complete" else "标记完成"

    // ==================== PC ====================
    val pcAdd get() = if (isEnglishSync()) "Add PC" else "添加PC"
    val pcEdit get() = if (isEnglishSync()) "Edit PC" else "编辑PC"
    val pcPlayer get() = if (isEnglishSync()) "Player: " else "玩家: "
    val pcSystem get() = if (isEnglishSync()) "System: " else "系统: "
    val pcGender get() = if (isEnglishSync()) "Gender: " else "性别: "
    val pcAttributes get() = if (isEnglishSync()) "Attributes" else "属性面板"
    val pcLuck get() = if (isEnglishSync()) "Luck" else "幸运"
    val pcSkills get() = if (isEnglishSync()) "Skills" else "技能列表"
    val pcNoSkills get() = if (isEnglishSync()) "No skill data" else "暂无技能数据"
    val pcItems get() = if (isEnglishSync()) "Items" else "物品清单"
    val pcNoItems get() = if (isEnglishSync()) "No items" else "暂无物品"
    val pcBackstory get() = if (isEnglishSync()) "Backstory" else "背景故事"
    val pcPlayerNickname get() = if (isEnglishSync()) "Player Nickname" else "玩家昵称"
    val pcCharName get() = if (isEnglishSync()) "Character Name" else "角色名称"
    val pcStatus get() = if (isEnglishSync()) "Status" else "状态"
    val pcStatusNormal get() = if (isEnglishSync()) "Normal" else "正常"
    val pcStatusWounded get() = if (isEnglishSync()) "Wounded" else "重伤"
    val pcStatusInsane get() = if (isEnglishSync()) "Insane" else "疯狂"
    val pcStatusDead get() = if (isEnglishSync()) "Dead" else "死亡"
    val pcLuckValue get() = if (isEnglishSync()) "Luck Value" else "幸运值"
    val pcSkillsJson get() = if (isEnglishSync()) "Skills JSON (e.g. {\"Shooting\":60,\"Listen\":70})" else "技能JSON (如 {\"射击\":60,\"聆听\":70})"
    val pcItemsJson get() = if (isEnglishSync()) "Items JSON (e.g. [\"Rope\",\"Flashlight\"])" else "物品JSON (如 [\"绳索\",\"手电筒\"])"
    val pcDetail get() = if (isEnglishSync()) "PC Detail" else "PC详情"
    val pcCurrent get() = if (isEnglishSync()) "Current" else "当前"
    val pcMax get() = if (isEnglishSync()) "Max" else "上限"
    val pcSearchPlaceholder get() = if (isEnglishSync()) "Search character or player name" else "搜索角色名或玩家名"
    val pcNoPcs get() = if (isEnglishSync()) "No PC characters added yet" else "还没有添加PC角色"
    val pcNoMatch get() = if (isEnglishSync()) "No matching characters" else "没有匹配的角色"

    // ==================== NPC ====================
    val npcDetail get() = if (isEnglishSync()) "NPC Detail" else "NPC详情"
    val npcAdd get() = if (isEnglishSync()) "Add NPC" else "添加NPC"
    val npcEdit get() = if (isEnglishSync()) "Edit NPC" else "编辑NPC"
    val npcName get() = if (isEnglishSync()) "Name" else "名称"
    val npcAlias get() = if (isEnglishSync()) "Alias" else "别名"
    val npcOccupation get() = if (isEnglishSync()) "Occupation" else "职业"
    val npcDescription get() = if (isEnglishSync()) "Description" else "描述"
    val npcTruePurpose get() = if (isEnglishSync()) "True Purpose (Secret)" else "真实目的（暗线）"
    val npcPcRelation get() = if (isEnglishSync()) "Relationship with PCs" else "与PC关系"
    val npcStatus get() = if (isEnglishSync()) "Status" else "状态"
    val npcStatusAlive get() = if (isEnglishSync()) "Alive" else "存活"
    val npcStatusDead get() = if (isEnglishSync()) "Dead" else "死亡"
    val npcStatusMissing get() = if (isEnglishSync()) "Missing" else "失踪"
    val npcStatusUnknown get() = if (isEnglishSync()) "Unknown" else "未知"
    val npcSearchPlaceholder get() = if (isEnglishSync()) "Search NPC name or alias" else "搜索NPC名称或别名"
    val npcNoNpcs get() = if (isEnglishSync()) "No NPCs added yet" else "还没有添加NPC"
    val npcNoMatch get() = if (isEnglishSync()) "No matching NPCs" else "没有匹配的NPC"

    // ==================== 阅读器 ====================
    // ==================== 阅读器 ====================
    val readerToc get() = if (isEnglishSync()) "Table of Contents" else "目录"
    val readerModuleReading get() = if (isEnglishSync()) "Module Reading" else "模组阅读"
    val readerCloseSearch get() = if (isEnglishSync()) "Close Search" else "关闭搜索"
    val readerSearch get() = if (isEnglishSync()) "Search" else "搜索"
    val readerUnfavorite get() = if (isEnglishSync()) "Unfavorite" else "取消收藏"
    val readerFavorite get() = if (isEnglishSync()) "Favorite" else "收藏"
    val readerMore get() = if (isEnglishSync()) "More" else "更多"
    val readerEditModuleInfo get() = if (isEnglishSync()) "Edit Module Info" else "编辑模组信息"
    val readerDeleteModule get() = if (isEnglishSync()) "Delete Module" else "删除模组"
    val readerEditSectionContent get() = if (isEnglishSync()) "Edit Section Content" else "编辑本节内容"
    val readerShare get() = if (isEnglishSync()) "Share" else "分享"
    val readerClearChapterHighlights get() = if (isEnglishSync()) "Clear Chapter Highlights" else "清除本章高亮"
    val readerClearChapterAnnotations get() = if (isEnglishSync()) "Clear Chapter Annotations" else "清除本章批注"
    val readerClearAllAnnotations get() = if (isEnglishSync()) "Clear All Annotations" else "清除全部标注"
    val readerContentEmpty get() = if (isEnglishSync()) "Module content is empty" else "模组内容为空"
    val readerSearchPlaceholder get() = if (isEnglishSync()) "Search module content..." else "搜索模组内容..."
    val readerNoMatch get() = if (isEnglishSync()) "No matching content found" else "未找到匹配内容"
    fun readerMatchCount(count: Int) = if (isEnglishSync()) "Found $count chapters" else "找到 $count 个章节"
    val readerPrevSection get() = if (isEnglishSync()) "Previous Section" else "上一节"
    val readerNextSection get() = if (isEnglishSync()) "Next Section" else "下一节"
    val readerHighlights get() = if (isEnglishSync()) "highlights" else "高亮"
    val readerAnnotations get() = if (isEnglishSync()) "annotations" else "批注"
    val readerBookmarks get() = if (isEnglishSync()) "bookmarks" else "书签"
    val readerMinutes get() = if (isEnglishSync()) "min" else "分钟"
    val readerManageBookmarks get() = if (isEnglishSync()) "Manage Bookmarks" else "管理书签"
    val readerManageAnnotations get() = if (isEnglishSync()) "Manage Annotations" else "管理标注"
    val readerSectionEmpty get() = if (isEnglishSync()) "No content in this section" else "本节无内容"
    val readerSelectFromToc get() = if (isEnglishSync()) "Select a chapter from TOC to start reading" else "从目录中选择一个章节开始阅读"
    val readerClearAnnotations get() = if (isEnglishSync()) "Clear Annotations" else "清除标注"
    val readerClearConfirm get() = if (isEnglishSync()) "Are you sure you want to clear all highlights and annotations? This cannot be undone." else "确定要清除所有高亮和批注吗？此操作不可撤销。"
    val readerAddBookmark get() = if (isEnglishSync()) "Add Bookmark" else "添加书签"
    val readerChapter get() = if (isEnglishSync()) "Chapter" else "章节"
    val readerNoteOptional get() = if (isEnglishSync()) "Note (optional)" else "备注（可选）"
    val readerDeleteModuleTitle get() = if (isEnglishSync()) "Delete Module" else "删除模组"
    fun readerDeleteModuleConfirm(title: String) = if (isEnglishSync()) "Are you sure you want to delete「$title」? This will also delete all highlights, annotations and bookmarks, and cannot be undone." else "确定要删除「$title」吗？此操作将同时删除该模组的所有高亮、批注和书签，且不可撤销。"
    val readerEdit get() = if (isEnglishSync()) "Edit" else "编辑"
    val readerAnnotate get() = if (isEnglishSync()) "Annotate" else "批注"
    val readerBookmark get() = if (isEnglishSync()) "Bookmark" else "书签"
    val readerErase get() = if (isEnglishSync()) "Erase" else "擦除"
    val readerPureMode get() = if (isEnglishSync()) "Pure Reading" else "纯享阅读"
    val readerExitPureMode get() = if (isEnglishSync()) "Exit Pure Reading" else "退出纯享模式"
    val readerSelectText get() = if (isEnglishSync()) "Select Text" else "选中文本"

    // ==================== 模组实体 ====================
    val entityPc get() = if (isEnglishSync()) "Suggested PC" else "推荐PC"
    val entityNpc get() = if (isEnglishSync()) "Default NPC" else "默认NPC"
    val entityLocation get() = if (isEnglishSync()) "Location" else "地点"
    val entityOrganization get() = if (isEnglishSync()) "Organization" else "组织"
    val entityClue get() = if (isEnglishSync()) "Clue" else "线索"
    val entityNoItems get() = if (isEnglishSync()) "No items" else "暂无项目"
    val entityMarkColor get() = if (isEnglishSync()) "Mark Color" else "标记颜色"
    val entityNoColor get() = if (isEnglishSync()) "No Color" else "无颜色"
    val entityColorYellow get() = if (isEnglishSync()) "Yellow" else "黄色"
    val entityColorGreen get() = if (isEnglishSync()) "Green" else "绿色"
    val entityColorBlue get() = if (isEnglishSync()) "Blue" else "蓝色"
    val entityColorOrange get() = if (isEnglishSync()) "Orange" else "橙色"
    val entityColorPink get() = if (isEnglishSync()) "Pink" else "粉色"
    val entityAddNpc get() = if (isEnglishSync()) "Add NPC" else "添加NPC"
    val entityEditNpc get() = if (isEnglishSync()) "Edit NPC" else "编辑NPC"
    val entityAddLocation get() = if (isEnglishSync()) "Add Location" else "添加地点"
    val entityEditLocation get() = if (isEnglishSync()) "Edit Location" else "编辑地点"
    val entityAddOrganization get() = if (isEnglishSync()) "Add Organization" else "添加组织"
    val entityEditOrganization get() = if (isEnglishSync()) "Edit Organization" else "编辑组织"
    val entityAddClue get() = if (isEnglishSync()) "Add Clue" else "添加线索"
    val entityEditClue get() = if (isEnglishSync()) "Edit Clue" else "编辑线索"
    val entityName get() = if (isEnglishSync()) "Name *" else "名称 *"
    val entityAlias get() = if (isEnglishSync()) "Alias" else "别名"
    val entityGender get() = if (isEnglishSync()) "Gender" else "性别"
    val entityOccupation get() = if (isEnglishSync()) "Occupation" else "职业"
    val entityDescription get() = if (isEnglishSync()) "Description" else "描述"
    val entityTruePurpose get() = if (isEnglishSync()) "True Purpose" else "真实目的"
    val entityType get() = if (isEnglishSync()) "Type" else "类型"
    val entityClues get() = if (isEnglishSync()) "Clues" else "线索"
    val entityInhabitants get() = if (isEnglishSync()) "Inhabitants" else "居民"
    val entityMembers get() = if (isEnglishSync()) "Members" else "成员"
    val entityGoals get() = if (isEnglishSync()) "Goals" else "目标"
    val entitySource get() = if (isEnglishSync()) "Source" else "来源"
    val entityMale get() = if (isEnglishSync()) "Male" else "男"
    val entityFemale get() = if (isEnglishSync()) "Female" else "女"
    val entityAlien get() = if (isEnglishSync()) "Alien" else "祂"
    val entityOther get() = if (isEnglishSync()) "Other" else "其他"

    // ==================== 团卡片 ====================
    val groupSessionStart get() = if (isEnglishSync()) "Session: " else "开团: "
    val groupExpectedEnd get() = if (isEnglishSync()) "Expected end: " else "预计结束: "
    val groupLastPlayed get() = if (isEnglishSync()) "Last played: " else "上次开团: "
    val groupEdit get() = if (isEnglishSync()) "Edit" else "编辑"
    val groupDelete get() = if (isEnglishSync()) "Delete" else "删除"
    val groupJustNow get() = if (isEnglishSync()) "Just now" else "刚刚"
    val groupMinutesAgo get() = if (isEnglishSync()) "m ago" else "分钟前"
    val groupHoursAgo get() = if (isEnglishSync()) "h ago" else "小时前"
    val groupDaysAgo get() = if (isEnglishSync()) "d ago" else "天前"
    val groupMonthsAgo get() = if (isEnglishSync()) "mo ago" else "个月前"

    // ==================== NPC卡片 ====================
    val npcAliasLabel get() = if (isEnglishSync()) "Alias: " else "别名: "

    // ==================== 开源许可 ====================
    val licenseTitle get() = if (isEnglishSync()) "Open Source Licenses" else "开源许可"
    val licenseAppName get() = if (isEnglishSync()) "Keeper's Notes" else "守密人笔记"
    val licenseCcByNcSa get() = if (isEnglishSync()) "Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International" else "知识共享 署名-非商业性使用-相同方式共享 4.0 国际许可协议"
    val licenseYouCan get() = if (isEnglishSync()) "You are free to:" else "您可以自由地："
    val licenseShare get() = if (isEnglishSync()) "Share — copy and redistribute the material in any medium or format\nAdapt — remix, transform, and build upon the material" else "共享 — 在任何媒介以任何形式复制、发行本作品\n演绎 — 修改、转换或以本作品为基础进行创作"
    val licenseConditions get() = if (isEnglishSync()) "Under the following terms:" else "惟须遵守下列条件："
    val licenseAttribution get() = if (isEnglishSync()) "Attribution — You must give appropriate credit\nNonCommercial — You may not use the material for commercial purposes\nShareAlike — If you remix, you must distribute under the same license" else "署名 — 您必须给出适当的署名\n非商业性使用 — 您不得将本作品用于商业目的\n相同方式共享 — 修改后的作品必须以相同许可证发布"
    val licenseThirdParty get() = if (isEnglishSync()) "Third-party Open Source Libraries" else "第三方开源库"

    // ==================== 全局搜索 ====================
    val searchTitle get() = if (isEnglishSync()) "Global Search" else "全局搜索"
    val searchPlaceholder get() = if (isEnglishSync()) "Search modules, notes, highlights, annotations..." else "搜索模组、笔记、高亮、批注..."
    val searchAll get() = if (isEnglishSync()) "All" else "全部"
    val searchModules get() = if (isEnglishSync()) "Modules" else "模组"
    val searchMemos get() = if (isEnglishSync()) "Memos" else "笔记"
    val searchHighlights get() = if (isEnglishSync()) "Highlights" else "高亮"
    val searchAnnotations get() = if (isEnglishSync()) "Annotations" else "批注"
    val searchHistory get() = if (isEnglishSync()) "Search History" else "搜索历史"
    val searchInputHint get() = if (isEnglishSync()) "Enter keywords to search" else "输入关键词搜索"
    val searchNoResults get() = if (isEnglishSync()) "No matching results" else "未找到匹配内容"
    val searchResultCount get() = if (isEnglishSync()) "Found %d results" else "找到 %d 个结果"
    val searchTypeModule get() = if (isEnglishSync()) "Module" else "模组"
    val searchTypeMemo get() = if (isEnglishSync()) "Memo" else "笔记"
    val searchTypeHighlight get() = if (isEnglishSync()) "Highlight" else "高亮"
    val searchTypeAnnotation get() = if (isEnglishSync()) "Annotation" else "批注"
    val searchTypeBookmark get() = if (isEnglishSync()) "Bookmark" else "书签"
    val searchTypeOther get() = if (isEnglishSync()) "Other" else "其他"

    // ==================== 个人资料 ====================
    val profileEditTitle get() = if (isEnglishSync()) "Edit Profile" else "编辑个人信息"
    val profileChangeAvatar get() = if (isEnglishSync()) "Change Avatar" else "更换头像"
    val profileClickToChange get() = if (isEnglishSync()) "Click avatar to change" else "点击头像更换"
    val profileNickname get() = if (isEnglishSync()) "Nickname" else "昵称"

    // ==================== 日历 ====================
    val calendarPrevMonth get() = if (isEnglishSync()) "Previous month" else "上月"
    val calendarNextMonth get() = if (isEnglishSync()) "Next month" else "下月"
    val calendarDaySun get() = if (isEnglishSync()) "Sun" else "日"
    val calendarDayMon get() = if (isEnglishSync()) "Mon" else "一"
    val calendarDayTue get() = if (isEnglishSync()) "Tue" else "二"
    val calendarDayWed get() = if (isEnglishSync()) "Wed" else "三"
    val calendarDayThu get() = if (isEnglishSync()) "Thu" else "四"
    val calendarDayFri get() = if (isEnglishSync()) "Fri" else "五"
    val calendarDaySat get() = if (isEnglishSync()) "Sat" else "六"
    val calendarHasReminder get() = if (isEnglishSync()) "Has reminder" else "已设置提醒"

    // ==================== 编辑模组对话框 ====================
    val editModuleTitle get() = if (isEnglishSync()) "Edit Module Info" else "编辑模组信息"
    val editModuleName get() = if (isEnglishSync()) "Module Name *" else "模组名称 *"
    val editModuleAuthor get() = if (isEnglishSync()) "Author" else "作者"
    val editModuleSystem get() = if (isEnglishSync()) "Game System" else "游戏系统"
    val editModuleNone get() = if (isEnglishSync()) "None" else "无"
    val editModuleCustom get() = if (isEnglishSync()) "Custom" else "自定义"
    val editModuleDifficulty get() = if (isEnglishSync()) "Difficulty" else "难度"
    val editModuleBeginner get() = if (isEnglishSync()) "Beginner" else "新手"
    val editModuleIntermediate get() = if (isEnglishSync()) "Intermediate" else "进阶"
    val editModuleAdvanced get() = if (isEnglishSync()) "Advanced" else "高难"
    val editModulePlayerCount get() = if (isEnglishSync()) "Player Count (e.g. 3-5)" else "玩家人数（如 3-5）"
    val editModuleDuration get() = if (isEnglishSync()) "Duration (e.g. 4-6h)" else "时长（如 4-6h）"
    val editModuleSummary get() = if (isEnglishSync()) "Summary" else "简介"
    val editModuleTags get() = if (isEnglishSync()) "Tags (comma separated)" else "标签（逗号分隔）"

    // ==================== 富文本编辑器 ====================
    val editorPlaceholder get() = if (isEnglishSync()) "Enter content..." else "输入内容..."
    val editorHeading get() = if (isEnglishSync()) "Heading" else "标题"
    val editorBold get() = if (isEnglishSync()) "Bold" else "加粗"
    val editorItalic get() = if (isEnglishSync()) "Italic" else "斜体"
    val editorStrikethrough get() = if (isEnglishSync()) "Strikethrough" else "删除线"
    val editorBulletList get() = if (isEnglishSync()) "Bullet List" else "无序列表"
    val editorOrderedList get() = if (isEnglishSync()) "Ordered List" else "有序列表"
    val editorQuote get() = if (isEnglishSync()) "Quote" else "引用"
    val editorCode get() = if (isEnglishSync()) "Code" else "代码"
    val editorEdit get() = if (isEnglishSync()) "Edit" else "编辑"
    val editorPreview get() = if (isEnglishSync()) "Preview" else "预览"

    // ==================== 书签面板 ====================
    val bookmarkPanelTitle get() = if (isEnglishSync()) "Bookmarks" else "书签"
    val bookmarkNoBookmarks get() = if (isEnglishSync()) "No bookmarks" else "暂无书签"
    val bookmarkAddHint get() = if (isEnglishSync()) "Click bookmark button in toolbar to add" else "点击工具栏的书签按钮添加"

    // ==================== 批注面板 ====================
    val annotationPanelTitle get() = if (isEnglishSync()) "Annotations" else "批注"
    val annotationNoAnnotations get() = if (isEnglishSync()) "No annotations" else "暂无批注"
    val annotationAddHint get() = if (isEnglishSync()) "Select text to add annotation" else "选中文本添加批注"

    // ==================== 批注对话框 ====================
    val annotationDialogTitle get() = if (isEnglishSync()) "Add Annotation" else "添加批注"
    val annotationContent get() = if (isEnglishSync()) "Annotation content" else "批注内容"
    val readerOriginalText get() = if (isEnglishSync()) "Original text" else "原文"
    val readerNoHighlights get() = if (isEnglishSync()) "No highlights" else "暂无高亮"
    val readerNoAnnotations get() = if (isEnglishSync()) "No annotations" else "暂无批注"
    val readerPosition get() = if (isEnglishSync()) "Position" else "位置"
    val close get() = if (isEnglishSync()) "Close" else "关闭"

    // ==================== 关系图谱 ====================
    val relationshipNoData get() = if (isEnglishSync()) "No relationship data" else "暂无关系数据"
    val relationshipAddNpcHint get() = if (isEnglishSync()) "Please add default NPCs in module settings first" else "请先在模组设置中添加默认NPC"
    val relationshipTab get() = if (isEnglishSync()) "Relations" else "关系"
    val relationshipCharacters get() = if (isEnglishSync()) "Characters" else "人物"
    val relationshipNoRelations get() = if (isEnglishSync()) "No relations" else "暂无关系"
    val relationshipUnpin get() = if (isEnglishSync()) "Unpin" else "取消固定"
    val relationshipPin get() = if (isEnglishSync()) "Pin" else "固定"
    val relationshipEditTitle get() = if (isEnglishSync()) "Edit Relation" else "编辑关系"
    val relationshipType get() = if (isEnglishSync()) "Relation Type" else "关系类型"
    val relationshipNoDescription get() = if (isEnglishSync()) "No description" else "暂无描述"
    val relationshipAddTitle get() = if (isEnglishSync()) "Add Relation" else "添加关系"
    val relationshipSourceEntity get() = if (isEnglishSync()) "Source Entity" else "源实体"
    val relationshipTargetEntity get() = if (isEnglishSync()) "Target Entity" else "目标实体"

    // ==================== 团关系图谱 ====================
    val groupRelationshipNoData get() = if (isEnglishSync()) "No relationship data" else "暂无关系数据"
    val groupRelationshipAddCharacterHint get() = if (isEnglishSync()) "Please add PC or NPC characters in group details first" else "请先在团详情中添加PC或NPC角色"
    val groupRelationshipSourceCharacter get() = if (isEnglishSync()) "Source Character" else "源角色"
    val groupRelationshipTargetCharacter get() = if (isEnglishSync()) "Target Character" else "目标角色"

    // ==================== 备忘录 ====================
    val memoTitleOrContentRequired get() = if (isEnglishSync()) "Please enter title or content" else "请输入标题或内容"
    val memoReminderTitle get() = if (isEnglishSync()) "Memo Reminder" else "备忘录提醒"

    // ==================== 表单验证 ====================
    val npcNameRequired get() = if (isEnglishSync()) "Please enter NPC name" else "请输入NPC名称"
    val pcPlayerNameRequired get() = if (isEnglishSync()) "Please enter player name" else "请输入玩家昵称"
    val pcCharacterNameRequired get() = if (isEnglishSync()) "Please enter character name" else "请输入角色名称"
    val groupNameRequired get() = if (isEnglishSync()) "Please enter group name" else "请输入团名称"
    val clueSource get() = if (isEnglishSync()) "Source" else "来源"

    // ==================== 导入/导出错误 ====================
    val unnamedModule get() = if (isEnglishSync()) "Unnamed Module" else "未命名模组"
    val docxParseFailed get() = if (isEnglishSync()) "DOCX parse failed" else "DOCX 解析失败"
    val zipImportFailed get() = if (isEnglishSync()) "ZIP import failed" else "ZIP 导入失败"
    val unnamedArchive get() = if (isEnglishSync()) "Unnamed Archive" else "未命名卷宗"
    val unsupportedDocFormat get() = if (isEnglishSync()) ".doc format not supported, please convert to .docx" else "不支持 .doc 格式，请转换为 .docx 后重试"
    val unsupportedFileFormat get() = if (isEnglishSync()) "Unsupported file format, only .txt and .docx are supported" else "不支持的文件格式，仅支持 .txt 和 .docx"
    val readFileFailed get() = if (isEnglishSync()) "Failed to read file" else "读取文件失败"
    val cannotOpenFile get() = if (isEnglishSync()) "Cannot open file" else "无法打开文件"
    val cannotOpenZip get() = if (isEnglishSync()) "Cannot open ZIP file" else "无法打开 ZIP 文件"
    val cannotOpenRar get() = if (isEnglishSync()) "Cannot open RAR file" else "无法打开 RAR 文件"
    val cannotOpen7z get() = if (isEnglishSync()) "Cannot open 7Z file" else "无法打开 7Z 文件"

    // ==================== 通知/闹钟 ====================
    val alarmReminderTitle get() = if (isEnglishSync()) "Alarm Reminder" else "闹钟提醒"
    val systemNotificationTitle get() = if (isEnglishSync()) "System Notification" else "系统通知"
    fun alarmReminderMessage(eventTitle: String, minutes: Int) = if (isEnglishSync()) "$eventTitle starts in $minutes minutes" else "$eventTitle 将在 $minutes 分钟后开始"
    val channelNameSystem get() = if (isEnglishSync()) "System Notification" else "系统通知"
    val channelDescSystem get() = if (isEnglishSync()) "Memo reminders and system notifications" else "备忘录提醒与系统通知"
    val defaultAlarmTitle get() = if (isEnglishSync()) "Session Reminder" else "开团提醒"
    val defaultAlarmMessage get() = if (isEnglishSync()) "Session starting soon" else "即将开团"

    // ==================== 日历日程标题 ====================
    fun calendarSessionStart(name: String) = if (isEnglishSync()) "$name Session Start" else "$name 开团"
    fun calendarSessionStartDate(name: String) = if (isEnglishSync()) "$name Session Start" else "$name 开团日"
    fun calendarSessionEndDate(name: String) = if (isEnglishSync()) "$name Expected End" else "$name 预计结束"
    fun calendarSessionInProgress(name: String) = if (isEnglishSync()) "$name In Progress" else "$name 开团中"

    // ==================== 备份验证 ====================
    val backupMissingManifest get() = if (isEnglishSync()) "Backup file missing manifest.json, may not be a valid backup" else "备份文件缺少 manifest.json，可能不是有效的备份文件"
    fun backupVersionTooOld(version: Int, minVersion: Int) = if (isEnglishSync()) "Backup version v$version is too old, minimum supported is v$minVersion" else "备份版本 v$version 太旧，最低支持 v$minVersion"
    fun backupVersionTooNew(version: Int, maxVersion: Int) = if (isEnglishSync()) "Backup version v$version is newer than app supported v$maxVersion, please update the app" else "备份版本 v$version 高于当前 APP 支持的 v$maxVersion，请更新 APP"
    val backupMissingJson get() = if (isEnglishSync()) "Backup file missing backup.json" else "备份文件缺少 backup.json"
    val backupChecksumFailed get() = if (isEnglishSync()) "Backup file checksum failed, file may be corrupted or tampered" else "备份文件校验失败，文件可能已损坏或被篡改"
    fun backupIncompleteFiles(expected: Int, actual: Int) = if (isEnglishSync()) "Backup incomplete: expected $expected attachments, found $actual" else "备份文件不完整：预期 $expected 个附件，实际 $actual 个"

    // ==================== 阅读器工具栏 ====================
    val readerExitPureReading get() = if (isEnglishSync()) "Exit pure reading" else "退出纯享模式"
    val readerPureReading get() = if (isEnglishSync()) "Pure reading" else "纯享阅读"

    // ==================== 单位/格式 ====================
    fun playerCountLabel(count: String) = if (isEnglishSync()) "${count}p" else "${count}人"
    val unitHours get() = if (isEnglishSync()) "h" else "小时"
    val unitMinutes get() = if (isEnglishSync()) "min" else "分钟"
    val unitSeconds get() = if (isEnglishSync()) "s" else "秒"
    fun charCountLabel(count: Int) = if (isEnglishSync()) "${count} chars" else "${count}字"
    val dateFormatYearMonth get() = if (isEnglishSync()) "MMM yyyy" else "yyyy年M月"

    private fun isEnglishSync(): Boolean {
        return ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_ENGLISH ||
                (ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_SYSTEM &&
                    java.util.Locale.getDefault().language == "en")
    }
}
