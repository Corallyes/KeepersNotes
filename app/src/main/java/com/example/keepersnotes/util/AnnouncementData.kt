package com.example.keepersnotes.util

import com.example.keepersnotes.ui.screen.profile.Announcement
import com.example.keepersnotes.ui.screen.profile.AnnouncementType
import java.util.Locale

/**
 * 公告数据 — 只需编辑此文件即可更新公告内容。
 *
 * 最新公告：面向用户的通知、说明
 * 更新日志：版本更新记录
 */
object AnnouncementData {

    private fun isEnglish(): Boolean {
        return ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_ENGLISH ||
                (ThemePreferences.currentLanguage == ThemePreferences.LANGUAGE_SYSTEM &&
                    Locale.getDefault().language == "en")
    }

    /** 最新公告列表（最新的放前面） */
    val latestAnnouncements: List<Announcement> get() = if (isEnglish()) listOf(
        Announcement(
            id = "notice_2",
            title = "v1.1 Beta 2 Notes",
            content = "Current version is v1.1 Beta 2 — most issues from Beta 1 have been fixed. Inviting friends and family for testing:" +
                    "\n\nData backup (fixed)\n• Relationship graph import (fixed)\n• Highlight operations (fixed)\n• Word/TXT document extraction (fixed)\n• Notification settings (fixed)\n• Alarm notifications now properly use full-screen alerts, alarm sounds, and vibration" +
                    "\n\nFor issues or suggestions, please contact us via email or GitHub. Thank you for your understanding and support.",
            date = "2026-06-18",
            type = AnnouncementType.WARNING
        ),
        Announcement(
            id = "notice_1",
            title = "Welcome to Keeper's Notes",
            content = "This app was developed to provide Keepers with a dedicated session management tool. All data is stored locally — cloud sync will take significant time to develop." +
                    "\n\nAbout Document Reading: Currently only supports converting Word to Markdown format. Due to encryption protection on original Word documents, direct parsing is not yet available. It may take some time to get used to." +
                    "\n\nAbout Feature Completeness: The current version is a rapid iteration product. Some features have not been fully tested. For issues, please contact us via email or GitHub — we'll address them as soon as possible. Please provide clear problem descriptions when reporting." +
                    "\n\nThank you for using Keeper's Notes.",
            date = "2026-06-16",
            type = AnnouncementType.INFO
        )
    ) else listOf(
        Announcement(
            id = "notice_2",
            title = "v1.1 测试版说明2",
            content = "当前版本为 v1.1 测试版2，修复了测试版1的绝大部分功能，正在邀请亲友测试中：" +
                    "\n• 闹钟通知没有经过充分测试，可能还存在问题" +
                    "\n\n如有问题或建议，可通过邮箱或GitHub反馈。感谢您的理解与支持。",
            date = "2026-06-18",
            type = AnnouncementType.WARNING
        ),
        Announcement(
            id = "notice_1",
            title = "欢迎使用守密人笔记",
            content = "本应用的开发初衷是为KP提供一款专用的带团备忘工具。数据全都本地存储，云端上传功能开发暂未考虑" +
                    "\n\n关于文档阅读：目前只测试了20万字左右可以正常导入，更大内存的没有测试。" +
                    "\n\n关于功能完善度：部分功能尚未经过充分测试。GitHub上反馈时请尽量提供清晰的问题描述。我会尽快处理的（忙完期末周我就来）" +
                    "\n\n求求泥们别笑这个代码了...这个，这个是我拿命写出来的。jpg"+
                    "\n\nOrz总之感谢使用，如果能够帮助到就更好了",
            date = "2026-06-16",
            type = AnnouncementType.INFO
        )
    )

    /** 更新日志列表（最新的放前面） */
    val updateLogs: List<Announcement> get() = if (isEnglish()) listOf(
        Announcement(
            id = "update_v1.1",
            title = "v1.1 Beta 2 Update",
            content = "• Calendar event management\n• Bilingual system (Chinese/English)\n• Alarm notifications now use full-screen alerts, alarm sounds, and vibration patterns\n\nCurrent version is beta. Some features are still being refined. Thank you for using Keeper's Notes. Please report issues in the Help Center.",
            date = "2026-06-18",
            type = AnnouncementType.UPDATE
        )
    ) else listOf(
        Announcement(
            id = "update_v1.1",
            title = "v1.1 测试版更新",
            content = "• 日历日程管理和提醒通知系统已完成测试\n• 双语系统上线，不知道有什么用但是先用一下（）\n• 阅读word的时候可以双指放大和缩小字体哦！\n\n当前为测试版本，部分功能仍在完善中。感谢您的使用，如有问题请在帮助中心反馈。",
            date = "2026-06-18",
            type = AnnouncementType.UPDATE
        )
    )
}
