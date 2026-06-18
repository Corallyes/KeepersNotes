package com.example.keepersnotes.data.importer

import android.content.Context
import android.net.Uri
import com.example.keepersnotes.data.local.entity.ArchiveEntity
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.repository.ArchiveRepository
import com.example.keepersnotes.data.repository.ImageRepository
import com.example.keepersnotes.util.FileReaderUtil
import com.example.keepersnotes.util.LocalizedStrings
import com.example.keepersnotes.util.StructuredDocxParser
import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ZipImportResult(
    val archives: List<ArchiveEntity>,
    val images: List<ImageEntity>,
    val collectionTitle: String,
    val combinedContent: String,
    val structuredNodes: List<StructuredDocxParser.DocumentNode> = emptyList()
)

@Singleton
class ZipImportManager @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val imageRepository: ImageRepository
) {
    companion object {
        private val DOC_EXTENSIONS = setOf("docx", "txt", "md")
        private val IMG_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
        private val ARCHIVE_EXTENSIONS = setOf("zip", "rar", "7z")
    }

    suspend fun importArchive(
        context: Context,
        uri: Uri,
        collectionId: String
    ): ZipImportResult {
        val fileName = getFileName(context, uri)
        val ext = fileName.substringAfterLast('.', "").lowercase()
        val tempDir = File(context.cacheDir, "archive_import_${System.currentTimeMillis()}")
        try {
            when (ext) {
                "rar" -> extractRar(context, uri, tempDir)
                "7z" -> extract7z(context, uri, tempDir)
                else -> extractZip(context, uri, tempDir)
            }
            val files = scanFiles(tempDir)

            val processed = processDocuments(context, files.documents, collectionId)
            val images = processImages(context, files.images, collectionId)

            // Combine all document contents into a single markdown string
            val combinedContent = processed.archives.joinToString("\n\n") { archive ->
                "# ${archive.title}\n\n${archive.contentMarkdown}"
            }

            val title = deriveCollectionTitle(files.documents, fileName)
            return ZipImportResult(
                archives = processed.archives,
                images = images,
                collectionTitle = title,
                combinedContent = combinedContent,
                structuredNodes = processed.structuredNodes
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    suspend fun insertImportResult(result: ZipImportResult) {
        archiveRepository.insertAll(result.archives)
        imageRepository.insertAll(result.images)
    }

    private fun extractZip(context: Context, uri: Uri, destDir: File) {
        destDir.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(destDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            zip.copyTo(out)
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        } ?: throw Exception(LocalizedStrings.cannotOpenZip)
    }

    private fun extractRar(context: Context, uri: Uri, destDir: File) {
        destDir.mkdirs()
        val tempRar = File(context.cacheDir, "temp_${System.currentTimeMillis()}.rar")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempRar.outputStream().use { output -> input.copyTo(output) }
            } ?: throw Exception(LocalizedStrings.cannotOpenRar)

            Archive(tempRar).use { archive ->
                var header: FileHeader? = archive.nextFileHeader()
                while (header != null) {
                    if (!header.isDirectory) {
                        val fileName = header.fileName.trim()
                        val outFile = File(destDir, fileName)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            archive.extractFile(header, out)
                        }
                    }
                    header = archive.nextFileHeader()
                }
            }
        } finally {
            tempRar.delete()
        }
    }

    private fun extract7z(context: Context, uri: Uri, destDir: File) {
        destDir.mkdirs()
        val temp7z = File(context.cacheDir, "temp_${System.currentTimeMillis()}.7z")
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                temp7z.outputStream().use { output -> input.copyTo(output) }
            } ?: throw Exception(LocalizedStrings.cannotOpen7z)

            SevenZFile(temp7z).use { sevenZ ->
                var entry: SevenZArchiveEntry? = sevenZ.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        val outFile = File(destDir, entry.name)
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out ->
                            val buffer = ByteArray(8192)
                            var len: Int
                            while (sevenZ.read(buffer).also { len = it } != -1) {
                                out.write(buffer, 0, len)
                            }
                        }
                    }
                    entry = sevenZ.nextEntry
                }
            }
        } finally {
            temp7z.delete()
        }
    }

    private fun scanFiles(dir: File): ScannedFiles {
        val documents = mutableListOf<File>()
        val images = mutableListOf<File>()

        dir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val ext = file.extension.lowercase()
                when {
                    ext in DOC_EXTENSIONS -> documents.add(file)
                    ext in IMG_EXTENSIONS -> images.add(file)
                }
            }
        }

        return ScannedFiles(documents, images)
    }

    private data class ProcessedDocuments(
        val archives: List<ArchiveEntity>,
        val structuredNodes: List<StructuredDocxParser.DocumentNode>
    )

    private suspend fun processDocuments(
        context: Context,
        files: List<File>,
        collectionId: String
    ): ProcessedDocuments {
        val archives = mutableListOf<ArchiveEntity>()
        val allNodes = mutableListOf<StructuredDocxParser.DocumentNode>()
        var nodeOrderOffset = 0

        files.forEachIndexed { index, file ->
            val ext = file.extension.lowercase()
            if (ext == "docx") {
                // New flow: structured parsing
                val nodes = try {
                    FileReaderUtil.readDocxStructured(context, file)
                } catch (e: Exception) {
                    emptyList()
                }
                if (nodes.isNotEmpty()) {
                    // Offset order so nodes from multiple files are sequential
                    val offsetNodes = nodes.map { it.copy(order = it.order + nodeOrderOffset) }
                    allNodes.addAll(offsetNodes)
                    nodeOrderOffset += nodes.size
                    // Also save markdown for ArchiveEntity compatibility
                    val markdown = try {
                        FileReaderUtil.readDocxToMarkdown(file)
                    } catch (e: Exception) {
                        nodes.joinToString("\n\n") { node ->
                            when (node.type) {
                                "heading" -> "#".repeat(node.level) + " " + node.content
                                "paragraph" -> node.content
                                "quote" -> "> " + node.content
                                else -> node.content
                            }
                        }
                    }
                    archives.add(ArchiveEntity(
                        archiveId = UUID.randomUUID().toString(),
                        collectionId = collectionId,
                        title = file.nameWithoutExtension,
                        contentMarkdown = markdown,
                        originalFileName = file.name,
                        fileType = ext,
                        sortOrder = index,
                        createTime = System.currentTimeMillis()
                    ))
                }
            } else if (ext == "txt") {
                // New flow: structured parsing for TXT files using Python
                val nodes = try {
                    FileReaderUtil.readTxtStructured(context, file)
                } catch (e: Exception) {
                    emptyList()
                }
                if (nodes.isNotEmpty()) {
                    // Convert StructuredTxtParser.DocumentNode to StructuredDocxParser.DocumentNode for compatibility
                    val docxCompatibleNodes = nodes.map { txtNode ->
                        StructuredDocxParser.DocumentNode(
                            type = txtNode.type,
                            level = txtNode.level,
                            content = txtNode.content,
                            order = txtNode.order + nodeOrderOffset
                        )
                    }
                    allNodes.addAll(docxCompatibleNodes)
                    nodeOrderOffset += nodes.size
                    // Also save markdown for ArchiveEntity compatibility
                    val markdown = nodes.joinToString("\n\n") { node ->
                        when (node.type) {
                            "heading" -> "#".repeat(node.level) + " " + node.content
                            "paragraph" -> node.content
                            "quote" -> "> " + node.content
                            "list_item" -> "- " + node.content
                            else -> node.content
                        }
                    }
                    archives.add(ArchiveEntity(
                        archiveId = UUID.randomUUID().toString(),
                        collectionId = collectionId,
                        title = file.nameWithoutExtension,
                        contentMarkdown = markdown,
                        originalFileName = file.name,
                        fileType = ext,
                        sortOrder = index,
                        createTime = System.currentTimeMillis()
                    ))
                } else {
                    // Fallback to old flow if Python parsing fails
                    val markdown = FileReaderUtil.readTextFileSmart(file)
                    archives.add(ArchiveEntity(
                        archiveId = UUID.randomUUID().toString(),
                        collectionId = collectionId,
                        title = file.nameWithoutExtension,
                        contentMarkdown = markdown,
                        originalFileName = file.name,
                        fileType = ext,
                        sortOrder = index,
                        createTime = System.currentTimeMillis()
                    ))
                }
            } else {
                // Old flow: MD → text
                val markdown = FileReaderUtil.readTextFileSmart(file)
                archives.add(ArchiveEntity(
                    archiveId = UUID.randomUUID().toString(),
                    collectionId = collectionId,
                    title = file.nameWithoutExtension,
                    contentMarkdown = markdown,
                    originalFileName = file.name,
                    fileType = ext,
                    sortOrder = index,
                    createTime = System.currentTimeMillis()
                ))
            }
        }

        return ProcessedDocuments(archives, allNodes)
    }

    private suspend fun processImages(
        context: Context,
        files: List<File>,
        collectionId: String
    ): List<ImageEntity> {
        val imageDir = File(context.filesDir, "images/$collectionId")
        imageDir.mkdirs()

        return files.mapIndexed { index, file ->
            val destFile = File(imageDir, "${UUID.randomUUID()}.${file.extension}")
            file.copyTo(destFile, overwrite = true)

            ImageEntity(
                imageId = UUID.randomUUID().toString(),
                collectionId = collectionId,
                title = file.nameWithoutExtension,
                filePath = destFile.absolutePath,
                originalFileName = file.name,
                sortOrder = index,
                createTime = System.currentTimeMillis()
            )
        }
    }

    private fun deriveCollectionTitle(
        documents: List<File>,
        fileName: String
    ): String {
        if (documents.isNotEmpty()) {
            return documents.first().nameWithoutExtension
        }
        return fileName.substringBeforeLast(".").ifBlank { LocalizedStrings.unnamedArchive }
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var fileName = ""
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                fileName = cursor.getString(nameIndex) ?: ""
            }
        }
        return fileName
    }

    private data class ScannedFiles(
        val documents: List<File>,
        val images: List<File>
    )
}
