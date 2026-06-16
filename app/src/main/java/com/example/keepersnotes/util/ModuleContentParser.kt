package com.example.keepersnotes.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * Chapter type classification for TRPG modules.
 */
enum class ChapterType(val label: String) {
    CHAPTER("章节"),
    SCENE("场景"),
    COMBAT("战斗"),
    DIALOGUE("对话"),
    CLUE("线索"),
    NPC("NPC"),
    LOCATION("地点"),
    RULE("规则"),
    APPENDIX("附录"),
    OTHER("其他")
}

data class Chapter(
    val id: String,
    val title: String,
    val content: String,
    val children: List<Chapter> = emptyList(),
    val type: ChapterType = ChapterType.OTHER,
    val keywords: List<String> = emptyList()
)

/**
 * Search index entry for a chapter.
 */
data class IndexEntry(
    val chapterId: String,
    val chapterTitle: String,
    val content: String,
    val keywords: List<String>
)

object ModuleContentParser {

    /**
     * Parse raw text content into structured chapters.
     */
    fun parseTextToChapters(text: String): List<Chapter> {
        val lines = text.lines()
        val rawSections = mutableListOf<RawSection>()
        var currentTitle = ""
        var currentLevel = 0
        var currentContent = StringBuilder()
        var sectionIndex = 0

        for (line in lines) {
            val headingLevel = detectHeadingLevel(line)
            if (headingLevel >= 0) {
                if (currentTitle.isNotBlank() || currentContent.isNotBlank()) {
                    rawSections.add(
                        RawSection(
                            index = sectionIndex++,
                            title = currentTitle.ifBlank { "未命名段落" },
                            content = currentContent.toString().trim(),
                            level = currentLevel
                        )
                    )
                }
                currentTitle = extractHeadingTitle(line.trim())
                currentLevel = headingLevel
                currentContent = StringBuilder()
            } else {
                currentContent.appendLine(line)
            }
        }
        if (currentTitle.isNotBlank() || currentContent.isNotBlank()) {
            rawSections.add(
                RawSection(
                    index = sectionIndex,
                    title = currentTitle.ifBlank { "未命名段落" },
                    content = currentContent.toString().trim(),
                    level = currentLevel
                )
            )
        }

        if (rawSections.isEmpty()) {
            return listOf(
                Chapter(
                    id = "1",
                    title = "正文",
                    content = text.trim(),
                    type = classifyChapter("正文", text),
                    keywords = extractKeywords(text)
                )
            )
        }

        return buildChapterTree(rawSections)
    }

    /**
     * Convert chapters to JSON string for storage.
     */
    fun chaptersToJson(chapters: List<Chapter>): String {
        val arr = JSONArray()
        chapters.forEach { chapter ->
            arr.put(chapterToJson(chapter))
        }
        return JSONObject().put("chapters", arr).toString()
    }

    /**
     * Parse JSON string back to chapters.
     */
    fun jsonToChapters(json: String): List<Chapter> {
        if (json.isBlank() || json == "{}") return emptyList()
        return try {
            val obj = JSONObject(json)
            val arr = obj.getJSONArray("chapters")
            (0 until arr.length()).map { jsonToChapter(arr.getJSONObject(it)) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Build a flat search index from all chapters.
     */
    fun buildSearchIndex(chapters: List<Chapter>): List<IndexEntry> {
        val result = mutableListOf<IndexEntry>()
        fun collect(ch: Chapter) {
            result.add(
                IndexEntry(
                    chapterId = ch.id,
                    chapterTitle = ch.title,
                    content = ch.content,
                    keywords = ch.keywords
                )
            )
            ch.children.forEach { collect(it) }
        }
        chapters.forEach { collect(it) }
        return result
    }

    /**
     * Update a single chapter's content by ID.
     * Returns the updated chapters list.
     */
    fun updateChapterContent(chapters: List<Chapter>, chapterId: String, newContent: String): List<Chapter> {
        return chapters.map { chapter ->
            if (chapter.id == chapterId) {
                chapter.copy(content = newContent)
            } else if (chapter.children.isNotEmpty()) {
                chapter.copy(children = updateChapterContent(chapter.children, chapterId, newContent))
            } else {
                chapter
            }
        }
    }

    /**
     * Search chapters by query string.
     * Returns matching chapter IDs sorted by relevance.
     */
    fun searchChapters(chapters: List<Chapter>, query: String): List<Chapter> {
        if (query.isBlank()) return emptyList()
        val queryLower = query.lowercase()
        val allIndex = buildSearchIndex(chapters)

        val scored = allIndex.mapNotNull { entry ->
            var score = 0
            // Title match (highest weight)
            if (entry.chapterTitle.lowercase().contains(queryLower)) score += 10
            // Keyword match
            if (entry.keywords.any { it.lowercase().contains(queryLower) }) score += 5
            // Content match
            val contentLower = entry.content.lowercase()
            if (contentLower.contains(queryLower)) score += 3
            // Count occurrences in content
            var idx = 0
            while (contentLower.indexOf(queryLower, idx).also { idx = it } >= 0) {
                score += 1
                idx += queryLower.length
            }
            if (score > 0) entry.chapterId to score else null
        }.sortedByDescending { it.second }

        val matchedIds = scored.map { it.first }.toSet()
        return findChaptersByIds(chapters, matchedIds)
    }

    /**
     * Extract structured TRPG data blocks from content.
     * Returns map of block type to list of entries.
     */
    fun extractStructuredBlocks(content: String): Map<String, List<String>> {
        val blocks = mutableMapOf<String, MutableList<String>>()
        val lines = content.lines()
        var currentBlockType: String? = null
        var currentBlock = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            val blockType = detectBlockType(trimmed)
            if (blockType != null) {
                // Save previous block
                if (currentBlockType != null && currentBlock.isNotBlank()) {
                    blocks.getOrPut(currentBlockType!!) { mutableListOf() }
                        .add(currentBlock.toString().trim())
                }
                currentBlockType = blockType
                currentBlock = StringBuilder()
                // Don't include the marker line itself
            } else {
                if (currentBlockType != null) {
                    currentBlock.appendLine(line)
                }
            }
        }
        // Save last block
        if (currentBlockType != null && currentBlock.isNotBlank()) {
            blocks.getOrPut(currentBlockType!!) { mutableListOf() }
                .add(currentBlock.toString().trim())
        }
        return blocks
    }

    // --- Private helpers ---

    private fun chapterToJson(chapter: Chapter): JSONObject {
        val obj = JSONObject()
        obj.put("id", chapter.id)
        obj.put("title", chapter.title)
        obj.put("content", chapter.content)
        obj.put("type", chapter.type.name)
        if (chapter.keywords.isNotEmpty()) {
            obj.put("keywords", JSONArray(chapter.keywords))
        }
        if (chapter.children.isNotEmpty()) {
            val childrenArr = JSONArray()
            chapter.children.forEach { childrenArr.put(chapterToJson(it)) }
            obj.put("children", childrenArr)
        }
        return obj
    }

    private fun jsonToChapter(obj: JSONObject): Chapter {
        val children = mutableListOf<Chapter>()
        if (obj.has("children")) {
            val arr = obj.getJSONArray("children")
            (0 until arr.length()).forEach { children.add(jsonToChapter(arr.getJSONObject(it))) }
        }
        val type = try {
            ChapterType.valueOf(obj.optString("type", "OTHER"))
        } catch (e: Exception) {
            ChapterType.OTHER
        }
        val keywords = if (obj.has("keywords")) {
            val arr = obj.getJSONArray("keywords")
            (0 until arr.length()).map { arr.getString(it) }
        } else emptyList()

        return Chapter(
            id = obj.getString("id"),
            title = obj.getString("title"),
            content = obj.optString("content", ""),
            children = children,
            type = type,
            keywords = keywords
        )
    }

    private fun classifyChapter(title: String, content: String): ChapterType {
        val combined = "$title $content".lowercase()
        return when {
            combined.contains("战斗") || combined.contains("遭遇") || combined.contains("combat") -> ChapterType.COMBAT
            combined.contains("对话") || combined.contains("交谈") || combined.contains("dialogue") -> ChapterType.DIALOGUE
            combined.contains("线索") || combined.contains("证据") || combined.contains("clue") -> ChapterType.CLUE
            combined.contains("npc") || combined.contains("角色") || combined.contains("人物") -> ChapterType.NPC
            combined.contains("地点") || combined.contains("场景") || combined.contains("location") -> ChapterType.LOCATION
            combined.contains("规则") || combined.contains("rule") || combined.contains("检定") -> ChapterType.RULE
            combined.contains("附录") || combined.contains("appendix") -> ChapterType.APPENDIX
            combined.contains("章") || combined.contains("chapter") || combined.contains("节") -> ChapterType.CHAPTER
            else -> ChapterType.SCENE
        }
    }

    private fun extractKeywords(text: String): List<String> {
        val keywords = mutableSetOf<String>()
        // Extract quoted strings as keywords (using Unicode escapes for Chinese quotes)
        val quotedPattern = Regex("[\"「『](.*?)[\"」』]")
        quotedPattern.findAll(text).forEach { keywords.add(it.groupValues[1]) }
        // Extract NPC names (common patterns)
        val npcPattern = Regex("(?:NPC|角色|人物)[：:]\\s*(\\S+)")
        npcPattern.findAll(text).forEach { keywords.add(it.groupValues[1]) }
        // Extract location names
        val locationPattern = Regex("(?:地点|场景|房间|建筑)[：:]\\s*(\\S+)")
        locationPattern.findAll(text).forEach { keywords.add(it.groupValues[1]) }
        // Extract skill check references
        val skillPattern = Regex("(聊听|侦查|说服|恐吓|闪避|格斗|射击|急救|医学|图书馆|信用|克苏鲁|神秘学|灵感)[（(]?\\d+%?[）)]?")
        skillPattern.findAll(text).forEach { keywords.add(it.value) }
        return keywords.take(20) // Limit keywords
    }

    private fun detectBlockType(line: String): String? {
        val trimmed = line.trim()
        return when {
            trimmed.matches(Regex("^【(线索|证据|发现)】.*")) -> "clue"
            trimmed.matches(Regex("^【(NPC|角色|人物)】.*")) -> "npc"
            trimmed.matches(Regex("^【(地点|场景|房间)】.*")) -> "location"
            trimmed.matches(Regex("^【(战斗|遭遇|敌人)】.*")) -> "combat"
            trimmed.matches(Regex("^【(规则|检定|判定)】.*")) -> "rule"
            trimmed.matches(Regex("^\\*\\*\\*(线索|NPC|地点|战斗|规则)\\*\\*\\*.*")) -> "block"
            else -> null
        }
    }

    private fun extractHeadingTitle(line: String): String {
        val trimmed = line.trim()
        if (trimmed.matches(Regex("^#{1,6}\\s+.+"))) {
            return trimmed.removePrefix("#").trim()
        }
        if (trimmed.matches(Regex("^[-=*_—]{3,}$"))) {
            return "分隔线"
        }
        return trimmed
    }

    private fun detectHeadingLevel(line: String): Int {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return -1

        // Markdown headings
        if (trimmed.matches(Regex("^#{1,6}\\s+.+"))) {
            return trimmed.takeWhile { it == '#' }.length - 1
        }
        // Chinese chapter markers
        if (trimmed.matches(Regex("^第[一二三四五六七八九十百千\\d]+[章节部篇幕].*"))) return 0
        // Numbered headings with depth
        if (trimmed.matches(Regex("^\\d+(\\.\\d+)*\\.\\s+.+"))) {
            val depth = trimmed.count { it == '.' }
            return (depth - 1).coerceIn(0, 3)
        }
        // Chinese numbered
        if (trimmed.matches(Regex("^[一二三四五六七八九十]+[、.．].+"))) return 0
        if (trimmed.matches(Regex("^（[一二三四五六七八九十]+）.+"))) return 1
        // English chapter markers
        if (trimmed.matches(Regex("^(Chapter|Section|Part)\\s+\\d+.*", RegexOption.IGNORE_CASE))) return 0
        // TRPG scene markers
        if (trimmed.matches(Regex("^【[\\u4e00-\\u9fa5]+】.*"))) return 1
        if (trimmed.matches(Regex("^\\[[\\u4e00-\\u9fa5]+].*"))) return 1
        // Separator lines
        if (trimmed.matches(Regex("^[-=*_—]{3,}$"))) return 0
        // All-caps English headings
        if (trimmed.matches(Regex("^[A-Z][A-Z\\s]{5,}$"))) return 1
        // Bold markers (**Title**)
        if (trimmed.matches(Regex("^\\*\\*[^*]+\\*\\*$"))) return 1
        return -1
    }

    private fun buildChapterTree(sections: List<RawSection>): List<Chapter> {
        if (sections.isEmpty()) return emptyList()

        if (sections.all { it.level == 0 }) {
            return sections.mapIndexed { index, section ->
                Chapter(
                    id = "${index + 1}",
                    title = section.title,
                    content = section.content,
                    type = classifyChapter(section.title, section.content),
                    keywords = extractKeywords(section.content)
                )
            }
        }

        val result = mutableListOf<Chapter>()
        val stack = mutableListOf<ChapterBuilder>()

        sections.forEach { section ->
            val builder = ChapterBuilder(
                title = section.title,
                content = section.content,
                level = section.level
            )

            while (stack.isNotEmpty() && stack.last().level >= section.level) {
                stack.removeAt(stack.size - 1)
            }

            if (stack.isEmpty()) {
                result.add(builder.toChapter("${result.size + 1}"))
                stack.add(builder)
            } else {
                stack.last().addChild(builder)
                stack.add(builder)
            }
        }

        return result
    }

    private fun findChaptersByIds(chapters: List<Chapter>, ids: Set<String>): List<Chapter> {
        val result = mutableListOf<Chapter>()
        for (chapter in chapters) {
            if (chapter.id in ids) {
                result.add(chapter)
            }
            result.addAll(findChaptersByIds(chapter.children, ids))
        }
        return result
    }

    private data class RawSection(
        val index: Int,
        val title: String,
        val content: String,
        val level: Int
    )

    private class ChapterBuilder(
        val title: String,
        val content: String,
        val level: Int,
        private val children: MutableList<ChapterBuilder> = mutableListOf()
    ) {
        fun addChild(child: ChapterBuilder) {
            children.add(child)
        }

        fun toChapter(id: String): Chapter {
            return Chapter(
                id = id,
                title = title,
                content = content,
                type = classifyChapter(title, content),
                keywords = extractKeywords(content),
                children = children.mapIndexed { index, child ->
                    child.toChapter("$id-${index + 1}")
                }
            )
        }
    }
}
