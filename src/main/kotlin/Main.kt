import java.nio.file.Path
import java.text.NumberFormat
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.PathWalkOption
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.walk
import kotlin.time.measureTime

data class ImageOccurrence(val count: Int, val path: Path)
data class Image(val name: String, val path: Path, val diskUsage: Long, val occurrences: MutableList<ImageOccurrence> = mutableListOf())

/**
 * Count the occurrences of a search string in a file.
 *
 * @receiver Path The path of the file to search.
 * @param searchString The string to search for in the file.
 * @return The number of occurrences of the search string in the file.
 */fun Path.occurrences(searchString: String): Int = if (isRegularFile()) readText().split(searchString).size - 1 else 0

/**
 * Find files with a specific extension in a directory and its subdirectories.
 *
 * @receiver Path The root directory to start the search from.
 * @param extension The file extension to search for.
 * @return A list of paths of the files with the specified extension.
 */
@OptIn(ExperimentalPathApi::class)
fun Path.findFiles(extension: String): List<Path> = walk(PathWalkOption.INCLUDE_DIRECTORIES).filter { it.extension == extension }.toList()

/**
 * Calculate the total disk usage of regular files in a directory and its subdirectories.
 *
 * @receiver Path The root directory to start the calculation from.
 * @return The total disk usage of regular files in bytes.
 */
@OptIn(ExperimentalPathApi::class)
fun Path.diskUsage(): Long = walk().filter { it.isRegularFile() }.sumOf { it.fileSize() }

fun main() {
    val timeTaken = measureTime {
        val projectRoot = Path("${System.getProperty("user.home")}/work/ios/")
        val imagesRoot = projectRoot.resolve("Course Hero/Course Hero Rebranded/Assets/Supporting Files/Images.xcassets")
        val images = imagesRoot
            .findFiles("imageset")
            .map { Image(name = it.nameWithoutExtension, path = it.relativeTo(imagesRoot), diskUsage = it.diskUsage()) }

        val swiftFiles = projectRoot.findFiles("swift")
        val objectiveCFiles = projectRoot.findFiles("m")
        println("Searching for ${images.count()} images in ${swiftFiles.count()} Swift and ${objectiveCFiles.count()} Objective-C files.")
        for (path in swiftFiles + objectiveCFiles) {
            for (image in images) {
                val occurrenceCount = path.occurrences(""""${image.name}"""")
                if (occurrenceCount > 0) {
                    image.occurrences.add(ImageOccurrence(occurrenceCount, path))
                }
            }
        }

        val unusedImages = images.filter { it.occurrences.isEmpty() }.sortedWith(compareBy<Image> { it.path }.thenBy { it.name })
        val diskUsage = unusedImages.sumOf { it.diskUsage }
        println("Found ${unusedImages.count()} unused images occupying ${NumberFormat.getInstance().format(diskUsage)} bytes on disk.")
        unusedImages.forEach { println(it) }
    }
    println("Time taken: $timeTaken")
}