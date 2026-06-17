package com.example.keepersnotes.util

import com.example.keepersnotes.ui.screen.profile.Announcement
import com.example.keepersnotes.ui.screen.profile.AnnouncementType

/**
 * 公告数据 — 只需编辑此文件即可更新公告内容。
 *
 * 最新公告：面向用户的通知、说明
 * 更新日志：版本更新记录
 */
object AnnouncementData {

    /** 最新公告列表（最新的放前面） */
    val latestAnnouncements: List<Announcement> = listOf(
        Announcement(
            id = "notice_2",
            title = "v1.1 测试版说明",
            content = "当前版本为 v1.1 测试版，部分功能尚不完善，请注意以下已知问题：" +
                    "\n\n 数据备份功能未经充分测试，请谨慎使用，建议仅作为功能体验。" +
                    "\n\n 已知问题：" +
                    "\n• 人物关系图谱导入不稳定" +
                    "\n• 文章高亮操作不够方便" +
                    "\n• Word/TXT 文档内容提取可能不完整（尤其对复杂排版的文档）" +
                    "\n• 通知设置没有经过测试，可能无法发挥作用" +
                    "\n\n如有问题或建议，可通过邮箱或GitHub反馈。感谢您的理解与支持。",
            date = "2026-06-17",
            type = AnnouncementType.WARNING
        ),
        Announcement(
            id = "notice_1",
            title = "欢迎使用守密人笔记",
            content = "本应用的开发初衷是为KP提供一款专用的带团备忘工具。数据全都本地存储，云端上传功能开发需要很长时间" +
                    "\n\n关于文档阅读：目前仅支持将Word转换为Markdown格式呈现。由于原始Word文档存在加密保护，暂时无法直接解析，初次使用可能需要一定适应。" +
                    "\n\n关于功能完善度：当前版本为快速迭代的产物，部分功能尚未经过充分测试。如有问题，可通过邮箱或GitHub反馈，我会尽快处理。反馈时请尽量提供清晰的问题描述。" +
                    "\n\n感谢使用。",
            date = "2026-06-16",
            type = AnnouncementType.INFO
        )
    )

    /** 更新日志列表（最新的放前面） */
    val updateLogs: List<Announcement> = listOf(
        Announcement(
            id = "update_v1.1",
            title = "v1.1 测试版更新",
            content = "• 日历日程管理\n• 通知提醒功能（闹钟提醒和系统通知）\n• 团关系网络\n• 模组实体管理（NPC、地点、组织、线索）\n• 数据备份/恢复（实验性功能）\n\n当前为测试版本，部分功能仍在完善中。感谢您的使用，如有问题请在帮助中心反馈。",
            date = "2026-06-17",
            type = AnnouncementType.UPDATE
        )
    )
}
