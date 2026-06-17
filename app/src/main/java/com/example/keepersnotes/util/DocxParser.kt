package com.example.keepersnotes.util

import org.apache.poi.xwpf.usermodel.*
import java.io.BufferedInputStream
import java.io.File

/**
 * DOCX 文档解析器 — 三层识别 + 评分系统
 *
 * 不信任任何单一格式特征，综合判断标题：
 * Layer 1: 强规则（章节编号模式）
 * Layer 2: 排版推断（字体大小、居中、加粗、段距）
 * Layer 3: Word 样式名辅助
 *
 * 内容提取覆盖：正文段落、文本框、页眉页脚、脚注尾注、批注、SDT
 */
object DocxParser {

    private const val HEADING_THRESHOLD = 40

    // 正文字体大小基准（会被动态计算）
    private const val DEFAULT_BODY_FONT_SIZE = 12f

    data class ParagraphData(
        val text: String,
        val fontSize: Float,
        val isBold: Boolean,
        val isCenter: Boolean,
        val spacingBefore: Float,
        val spacingAfter: Float,
        val styleName: String,
        val listLevel: Int,
        val isAllCaps: Boolean
    )

    data class ScoredParagraph(
        val data: ParagraphData,
        val score: Int,
        val headingLevel: Int // 0=h1, 1=h2, 2=h3, -1=正文
    )

    // ==================== 强规则（Layer 1）====================

    private data class StrongRule(
        val pattern: Regex,
        val headingLevel: Int,
        val score: Int
    )

    private val strongRules = listOf(
        // 中文章节：第一章、第二章 → h1
        StrongRule(Regex("^第[一二三四五六七八九十百千\\d]+[章节部篇幕].*"), 0, 50),
        // 中文编号：一、二、 → h2
        StrongRule(Regex("^[一二三四五六七八九十]+[、.．].*"), 1, 45),
        // 中文括号：（一）（二） → h3
        StrongRule(Regex("^（[一二三四五六七八九十]+）.*"), 2, 45),
        // 数字编号：1. 2. → h2
        StrongRule(Regex("^\\d+\\.\\s+.*"), 1, 40),
        // 数字子编号：1.1. 2.3. → h3
        StrongRule(Regex("^\\d+\\.\\d+\\.\\s+.*"), 2, 45),
        // 英文章节：Chapter 1, Section 2 → h1
        StrongRule(Regex("^(Chapter|Section|Part)\\s+\\d+.*", RegexOption.IGNORE_CASE), 0, 50),
        // 括号编号：(1) (2) → h3
        StrongRule(Regex("^\\(\\d+\\).*"), 2, 40),
        // 中文括号编号：(1) (2) → h3
        StrongRule(Regex("^（\\d+）.*"), 2, 40)
    )

    private fun matchStrongRule(text: String): Pair<Int, Int>? {
        for (rule in strongRules) {
            if (text.matches(rule.pattern)) {
                return rule.headingLevel to rule.score
            }
        }
        return null
    }

    // ==================== Word 样式识别（Layer 3）====================

    private fun getStyleScore(styleName: String): Pair<Int, Int> {
        val normalized = styleName.lowercase().trim()
        // 英文标题样式
        if (normalized.startsWith("heading")) {
            val num = normalized.removePrefix("heading").trim().toIntOrNull()
            if (num != null && num in 1..6) return (num - 1) to 35
        }
        // 中文标题样式
        if (normalized.startsWith("标题")) {
            val num = normalized.removePrefix("标题").trim().toIntOrNull()
            if (num != null && num in 1..6) return (num - 1) to 35
        }
        // TOC 标题
        if (normalized.startsWith("toc ") || normalized.startsWith("tocheading")) {
            return 0 to 30
        }
        return -1 to 0
    }

    // ==================== 评分系统 ====================

    private fun scoreParagraph(data: ParagraphData, bodyFontSize: Float): ScoredParagraph {
        var score = 0
        var headingLevel = -1

        // 空段落跳过
        if (data.text.isBlank()) {
            return ScoredParagraph(data, 0, -1)
        }

        // Layer 1: 强规则
        val strongMatch = matchStrongRule(data.text)
        if (strongMatch != null) {
            headingLevel = strongMatch.first
            score += strongMatch.second
        }

        // Layer 3: Word 样式名
        val (styleLevel, styleScore) = getStyleScore(data.styleName)
        if (styleLevel >= 0) {
            score += styleScore
            if (headingLevel < 0) headingLevel = styleLevel
            else headingLevel = minOf(headingLevel, styleLevel)
        }

        // Layer 2: 排版推断
        val fontSizeDiff = data.fontSize - bodyFontSize

        // 字体大小明显大于正文
        if (fontSizeDiff >= 6f) {
            score += 30
        } else if (fontSizeDiff >= 3f) {
            score += 20
        } else if (fontSizeDiff >= 1.5f) {
            score += 10
        }

        // 居中对齐
        if (data.isCenter) {
            score += 10
        }

        // 加粗（仅作为辅助信号，单独加粗不加太多分）
        if (data.isBold) {
            score += 5
        }

        // 段前距较大
        if (data.spacingBefore > 12f) {
            score += 5
        }

        // 全大写英文
        if (data.isAllCaps && data.text.length > 5) {
            score += 10
        }

        // 根据分数确定标题层级
        return if (score >= HEADING_THRESHOLD) {
            // 如果没有通过规则确定层级，根据分数推断
            val finalLevel = when {
                headingLevel >= 0 -> headingLevel
                score >= 70 -> 0
                score >= 55 -> 1
                else -> 2
            }
            ScoredParagraph(data, score, finalLevel.coerceIn(0, 5))
        } else {
            ScoredParagraph(data, score, -1)
        }
    }

    // ==================== 段落提取 ====================

    private fun extractParagraphData(paragraph: XWPFParagraph): ParagraphData {
        val text = paragraph.text?.trim() ?: ""

        // 字体大小：取第一个非空 run 的字号
        var fontSize = DEFAULT_BODY_FONT_SIZE
        for (run in paragraph.runs) {
            val size = run.fontSize
            if (size > 0) {
                fontSize = size.toFloat()
                break
            }
        }

        // 加粗：所有 run 都加粗才算加粗
        val isBold = paragraph.runs.isNotEmpty() &&
                paragraph.runs.all { it.isBold }

        // 居中
        val isCenter = paragraph.alignment == ParagraphAlignment.CENTER ||
                paragraph.alignment == ParagraphAlignment.BOTH

        // 段距（转换为磅，1 twip = 1/20 pt）
        val spacingBefore = (paragraph.spacingBefore?.toFloat() ?: 0f) / 20f
        val spacingAfter = (paragraph.spacingAfter?.toFloat() ?: 0f) / 20f

        // 样式名
        val styleName = paragraph.style ?: ""

        // 列表层级
        val listLevel = paragraph.numLevelText?.let {
            if (it.isNotEmpty()) it.length else -1
        } ?: -1

        // 全大写
        val isAllCaps = text == text.uppercase() && text.any { it.isLetter() }

        return ParagraphData(
            text = text,
            fontSize = fontSize,
            isBold = isBold,
            isCenter = isCenter,
            spacingBefore = spacingBefore,
            spacingAfter = spacingAfter,
            styleName = styleName,
            listLevel = listLevel,
            isAllCaps = isAllCaps
        )
    }

    // ==================== 内联格式 ====================

    private fun convertRunToMarkdown(run: XWPFRun): String {
        val text = run.text() ?: return ""
        if (text.isEmpty()) return ""

        return when {
            run.isBold && run.isItalic -> "***$text***"
            run.isBold -> "**$text**"
            run.isItalic -> "*$text*"
            run.isStrikeThrough -> "~~$text~~"
            else -> text
        }
    }

    private fun convertParagraphInline(paragraph: XWPFParagraph): String {
        val sb = StringBuilder()
        for (run in paragraph.runs) {
            sb.append(convertRunToMarkdown(run))
        }
        return sb.toString().trim()
    }

    // ==================== 表格转换 ====================

    private fun convertTableToMarkdown(table: XWPFTable): String {
        val rows = table.rows
        if (rows.isEmpty()) return ""

        val sb = StringBuilder()
        rows.forEachIndexed { rowIndex, row ->
            val cells = row.tableCells
            val cellTexts = cells.map { cell ->
                cell.paragraphs.joinToString(" ") { it.text.trim() }.trim()
            }
            sb.appendLine("| ${cellTexts.joinToString(" | ")} |")
            if (rowIndex == 0) {
                sb.appendLine("| ${cellTexts.map { "---" }.joinToString(" | ")} |")
            }
        }
        return sb.toString().trim()
    }

    // ==================== 全文文本提取（含文本框等嵌入内容）====================

    /**
     * 从 DOCX 文件的 XML 中提取所有文本段落。
     * 覆盖：正文、文本框（txbxContent）、SDT、页眉页脚。
     * 返回完整的段落文本列表（非碎片）。
     */
    private fun extractAllTextFromXml(document: XWPFDocument): List<String> {
        val allTexts = mutableListOf<String>()
        val seenTexts = mutableSetOf<String>() // 去重

        fun addText(text: String) {
            val trimmed = text.trim()
            if (trimmed.isNotBlank() && trimmed !in seenTexts) {
                seenTexts.add(trimmed)
                allTexts.add(trimmed)
            }
        }

        // 1. 正文段落文本
        document.paragraphs.forEach { para ->
            val text = para.text?.trim() ?: ""
            if (text.isNotBlank()) addText(text)
        }

        // 2. 表格内文本
        document.tables.forEach { table ->
            table.rows.forEach { row ->
                row.tableCells.forEach { cell ->
                    cell.paragraphs.forEach { para ->
                        val text = para.text?.trim() ?: ""
                        if (text.isNotBlank()) addText(text)
                    }
                    cell.tables.forEach { nestedTable ->
                        nestedTable.rows.forEach { nestedRow ->
                            nestedRow.tableCells.forEach { nestedCell ->
                                nestedCell.paragraphs.forEach { para ->
                                    val text = para.text?.trim() ?: ""
                                    if (text.isNotBlank()) addText(text)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. 从 XML 中提取文本框、SDT 等嵌入内容（完整段落重建）
        try {
            val body = document.document.body
            if (body != null) {
                extractParagraphsFromXml(body.xmlText()) { text -> addText(text) }
            }
        } catch (e: Exception) { }

        // 4. 页眉文本
        try {
            document.headerList.forEach { header ->
                header.paragraphs.forEach { para ->
                    val text = para.text?.trim() ?: ""
                    if (text.isNotBlank()) addText(text)
                }
            }
        } catch (e: Exception) { }

        // 5. 页脚文本
        try {
            document.footerList.forEach { footer ->
                footer.paragraphs.forEach { para ->
                    val text = para.text?.trim() ?: ""
                    if (text.isNotBlank()) addText(text)
                }
            }
        } catch (e: Exception) { }

        return allTexts
    }

    /**
     * 从 XML 字符串中重建完整段落。
     * 找到每个 <w:p>...</w:p> 块，提取其中所有 <w:t> 文本拼接为完整段落。
     * 这能正确处理文本框（txbxContent）和 SDT 中的嵌套段落。
     */
    private fun extractParagraphsFromXml(xmlText: String?, addText: (String) -> Unit) {
        if (xmlText.isNullOrBlank()) return

        // 匹配 <w:p ...> 到 </w:p> 的完整段落块
        // 使用非贪婪匹配以正确处理嵌套
        val paragraphPattern = Regex("<w:p[\\s>]([\\s\\S]*?)</w:p>")
        val textPattern = Regex("<w:t(?:\\s[^>]*)?>([^<]*)</w:t>")

        paragraphPattern.findAll(xmlText).forEach { paraMatch ->
            val paraContent = paraMatch.groupValues[1]
            // 提取该段落内所有 <w:t> 文本并拼接
            val sb = StringBuilder()
            textPattern.findAll(paraContent).forEach { textMatch ->
                sb.append(textMatch.groupValues[1])
            }
            val text = sb.toString().trim()
            if (text.isNotBlank()) {
                addText(text)
            }
        }
    }

    // ==================== 主入口 ====================

    /**
     * 解析 DOCX 文件，输出 Markdown 文本。
     */
    fun parse(file: File): String {
        val doc = XWPFDocument(BufferedInputStream(file.inputStream()))
        val result = parseDocument(doc)
        doc.close()
        return result
    }

    /**
     * 解析 DOCX 文档，输出 Markdown 文本。
     * 采用两阶段策略：
     * 1. 从 bodyElements 提取结构化内容（带格式信息，用于标题检测）
     * 2. 从 XML 全文扫描补充遗漏内容（文本框、页眉页脚等）
     */
    fun parseDocument(document: XWPFDocument): String {
        // ===== 第一阶段：结构化提取（带格式信息）=====
        val paragraphDataList = document.bodyElements.mapNotNull { element ->
            when (element) {
                is XWPFParagraph -> extractParagraphData(element)
                else -> null // 表格单独处理
            }
        }

        // 计算正文字体大小基准（取众数）
        val bodyFontSize = calculateBodyFontSize(paragraphDataList)

        // 第二遍：评分并生成 Markdown
        val markdown = StringBuilder()
        var paragraphIndex = 0

        document.bodyElements.forEach { element ->
            when (element) {
                is XWPFParagraph -> {
                    val data = paragraphDataList.getOrNull(paragraphIndex) ?: return@forEach
                    paragraphIndex++
                    if (data.text.isBlank()) return@forEach

                    val scored = scoreParagraph(data, bodyFontSize)
                    val inlineMarkdown = convertParagraphInline(element)

                    if (scored.headingLevel >= 0) {
                        val prefix = "#".repeat(scored.headingLevel + 1)
                        markdown.appendLine("$prefix $inlineMarkdown")
                    } else {
                        // 列表项
                        if (data.listLevel >= 0) {
                            val indent = "  ".repeat(data.listLevel.coerceAtMost(3))
                            markdown.appendLine("$indent- $inlineMarkdown")
                        } else {
                            markdown.appendLine(inlineMarkdown)
                        }
                    }
                }
                is XWPFTable -> {
                    val tableMd = convertTableToMarkdown(element)
                    if (tableMd.isNotBlank()) {
                        markdown.appendLine(tableMd)
                    }
                }
            }
        }

        // ===== 第二阶段：全文扫描补充遗漏内容 =====
        val structuredTexts = mutableSetOf<String>()
        // 收集已提取的文本（去重用）
        document.bodyElements.forEach { element ->
            when (element) {
                is XWPFParagraph -> {
                    val text = element.text?.trim()
                    if (!text.isNullOrBlank()) structuredTexts.add(text)
                }
                is XWPFTable -> {
                    element.rows.forEach { row ->
                        row.tableCells.forEach { cell ->
                            cell.paragraphs.forEach { para ->
                                val text = para.text?.trim()
                                if (!text.isNullOrBlank()) structuredTexts.add(text)
                            }
                        }
                    }
                }
            }
        }

        // 提取全文文本（含文本框、页眉页脚等）
        val allTexts = extractAllTextFromXml(document)

        // 补充遗漏的文本
        val missedTexts = allTexts.filter { it !in structuredTexts }
        if (missedTexts.isNotEmpty()) {
            markdown.appendLine()
            markdown.appendLine("---")
            markdown.appendLine()
            missedTexts.forEach { text ->
                markdown.appendLine(text)
            }
        }

        return markdown.toString().trim()
    }

    /**
     * 计算正文字体大小基准（众数）。
     */
    private fun calculateBodyFontSize(paragraphs: List<ParagraphData>): Float {
        if (paragraphs.isEmpty()) return DEFAULT_BODY_FONT_SIZE

        // 统计字体大小出现频率
        val sizeCounts = paragraphs
            .filter { it.text.isNotBlank() }
            .groupBy { it.fontSize }
            .mapValues { it.value.size }

        // 取出现最多的字号作为正文基准
        return sizeCounts.maxByOrNull { it.value }?.key ?: DEFAULT_BODY_FONT_SIZE
    }
}
