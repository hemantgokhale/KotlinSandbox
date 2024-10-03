package unusedImages

import java.nio.file.FileVisitResult
import java.nio.file.Path
import java.text.NumberFormat
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.deleteRecursively
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString
import kotlin.io.path.readText
import kotlin.io.path.visitFileTree
import kotlin.io.path.walk
import kotlin.time.measureTime

@OptIn(ExperimentalPathApi::class)
private class Image(val path: Path) {
    val name = path.nameWithoutExtension
    val diskUsage = path.walk().filter { it.isRegularFile() }.sumOf { it.fileSize() }
    var occurrence: Path? = null
    val isUnused: Boolean
        get() = occurrence == null

    override fun toString(): String =
        "${path.absolutePathString()}, ${String.format("%.2f", diskUsage / 1000.0)} kB, ${if (isUnused) "None" else occurrence}"
}

@OptIn(ExperimentalPathApi::class)
fun findUnusedImages() {
    val timeTaken = measureTime {
        val projectRoot = Path("${System.getProperty("user.home")}/work/ios/")
        val images = projectRoot
            .walk(PathWalkOption.INCLUDE_DIRECTORIES)
            .filter { !it.pathString.contains("Pods") }
            .filter { it.extension == "imageset" }
            .map { Image(path = it) }
            .toList()

        projectRoot.visitFileTree {
            onPreVisitDirectory { directory, _ ->
                if (directory.name in listOf("Pods", "Images.xcassets") ||
                    directory.name.startsWith(".") ||
                    directory.name.endsWith(".framework")
                )
                    FileVisitResult.SKIP_SUBTREE
                else
                    FileVisitResult.CONTINUE
            }
            onVisitFile { file, _ ->
                if (!file.name.startsWith(".") && file.extension !in listOf("js", "ttf")) {
                    try {
                        val fileText = file.readText()
                        images.filter { it.isUnused }.forEach { if (fileText.contains("\"${it.name}\"")) it.occurrence = file }
                    } catch (e: Exception) {
                        // Ignore any exception caused by trying to open a binary file as text.
                        print("Exception while reading ${file.name}. e: ${e.message}")
                    }
                }
                FileVisitResult.CONTINUE
            }
        }

        val unusedImages = images.filter { it.isUnused }
        val diskUsage = NumberFormat.getInstance().format(unusedImages.sumOf { it.diskUsage })
        println("Total images: ${images.size}, unused: ${unusedImages.size}, occupying $diskUsage bytes on disk.")
        images
            .filter { it.isUnused }
            .forEach { it.path.deleteRecursively() }

    }
    println("Time taken: $timeTaken")
}