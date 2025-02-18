package me.vripper.utilities

import me.vripper.exception.RenameException
import me.vripper.model.Settings
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.Path


object PathUtils {
    private val log by me.vripper.delegate.LoggerDelegate()
    private val imageExtensions = listOf("bmp", "gif", "jpg", "jpeg", "png", "webp")


    fun calculateDownloadPath(
        forum: String, threadTitle: String, settings: Settings
    ): Path {
        var downloadDirectory = if (settings.downloadSettings.forumSubDirectory) Path.of(
            settings.downloadSettings.downloadPath, sanitize(forum)
        ) else Path.of(
            settings.downloadSettings.downloadPath
        )
        downloadDirectory =
            if (settings.downloadSettings.threadSubLocation) downloadDirectory.resolve(threadTitle) else downloadDirectory
        return downloadDirectory
    }

    @Throws(RenameException::class)
    fun rename(downloadDirectory: String, oldFolder: String, newFolder: String) {
        val currentDownloadDirectory = Path(downloadDirectory, oldFolder)
        val newDownloadDirectory = Path(downloadDirectory, sanitize(newFolder))
        try {
            Files.move(
                currentDownloadDirectory,
                newDownloadDirectory,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (e: IOException) {
            throw RenameException(
                String.format(
                    "Failed to move files from %s to %s", currentDownloadDirectory, newDownloadDirectory
                ),
                e
            )
        }
    }

    /**
     * Will sanitize the image name and remove extension
     *
     * @param path
     * @return Sanitized local path string
     */
    fun sanitize(path: String): String {
        val sanitizedPath =
            path.replace("\\.|\\\\|/|\\||:|\\?|\\*|\"|<|>|\\p{Cntrl}".toRegex(), "_")
        log.debug(String.format("%s sanitized to %s", path, sanitizedPath))
        return sanitizedPath
    }

    fun getExtension(fileName: String): String {
        val extension = if (fileName.contains(".")) fileName.substring(fileName.lastIndexOf(".") + 1) else ""
        return if (!imageExtensions.contains(extension.lowercase())) {
            ""
        } else {
            extension
        }
    }

    fun getFileNameWithoutExtension(fileName: String): String {
        return if (fileName.contains(".")) fileName.substring(
            0,
            fileName.lastIndexOf(".")
        ) else fileName
    }
}