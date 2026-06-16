package com.example.keepersnotes.data.importer

import android.content.Context
import android.net.Uri
import com.example.keepersnotes.data.local.entity.ArchiveEntity
import com.example.keepersnotes.data.local.entity.ImageEntity
import com.example.keepersnotes.data.repository.ArchiveRepository
import com.example.keepersnotes.data.repository.ImageRepository
import com.example.keepersnotes.util.FileReaderUtil
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

data class ZipImportResult(
    val archives: List<ArchiveEntity>,
    val images: List<ImageEntity>,
    val collectionTitle: String
)

@Singleton
class ZipImportManager @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val imageRepository: ImageRepository
) {
    companion object {
        private val DOC_EXTENSIONS = setOf("docx", "txt", "md")
        private val IMG_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
    }

    suspend fun importZip(
        context: Context,
        uri: Uri,
        collectionId: String
    ): ZipImportResult {
        val tempDir = File(context.cacheDir, "zip_import_${System.currentTimeMillis()}")
        try {
            extractZip(context, uri, tempDir)
            val files = scanFiles(tempDir)

            val archives = processDocuments(context, files.documents, collectionId)
            val images = processImages(context, files.images, collectionId)

            archiveRepository.insertAll(archives)
            imageRepository.insertAll(images)

            val title = deriveCollectionTitle(files.documents, uri, context)
            return ZipImportResult(archives, images, title)
        } finally {
            tempDir.deleteRecursively()
        }
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
        } ?: throw Exception("无法打开 ZIP 文件")
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

    private suspend fun processDocuments(
        context: Context,
        files: List<File>,
        collectionId: String
    ): List<ArchiveEntity> {
        return files.mapIndexed { index, file ->
            val ext = file.extension.lowercase()
            val markdown = when (ext) {
                "docx" -> FileReaderUtil.readDocxToMarkdown(file)
                "txt" -> file.readText()
                "md" -> file.readText()
                else -> file.readText()
            }

            ArchiveEntity(
                archiveId = UUID.randomUUID().toString(),
                collectionId = collectionId,
                title = file.nameWithoutExtension,
                contentMarkdown = markdown,
                originalFileName = file.name,
                fileType = ext,
                sortOrder = index,
                createTime = System.currentTimeMillis()
            )
        }
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
        zipUri: Uri,
        context: Context
    ): String {
        if (documents.isNotEmpty()) {
            return documents.first().nameWithoutExtension
        }
        val fileName = getFileName(context, zipUri)
        return fileName.substringBeforeLast(".").ifBlank { "未命名卷宗" }
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
