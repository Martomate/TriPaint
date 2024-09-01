package tripaint.app

import tripaint.image.RegularImage
import tripaint.util.EventDispatcher
import tripaint.util.Tracker
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO

class FileSystem private constructor (private val imageIO: ImageIOWrapper) {
    private val dispatcher = EventDispatcher<Event>()

    /** @param tracker the tracker to notify when an event occurs */
    fun trackChanges(tracker: Tracker<Event>) = dispatcher.track(tracker)

    /** @return Some(image) if it exists, None otherwise */
    fun readImage(file: File): RegularImage? {
        val im = runCatching { imageIO.read(file) }.getOrNull()
        return if (im != null) RegularImage.fromBufferedImage(im) else null
    }

    /** @return true if the format is supported and the image was written successfully */
    fun writeImage(image: RegularImage, file: File): Boolean {
        val success = imageIO.write(image.toBufferedImage(), getExtension(file).uppercase(Locale.getDefault()), file)
        if (success) {
            dispatcher.notify(Event.ImageWritten(image, file))
        }
        return success
    }

    private fun getExtension(file: File): String = file.getName().substring(file.getName().lastIndexOf('.') + 1)

    companion object {
        fun create(): FileSystem = FileSystem(RealImageIO)
        fun createNull(args: NullArgs = NullArgs()): FileSystem {
            val allSupportedFormats = args.supportedImageFormats union args.supportedImageFormats.map { it.uppercase(Locale.getDefault()) }
            val imageIO = NullImageIO(args.initialImages, allSupportedFormats)
            return FileSystem(imageIO)
        }
    }

    sealed interface Event {
        data class ImageWritten(val image: RegularImage, val file: File) : Event
    }

    data class NullArgs(
        val initialImages: Map<File, RegularImage> = mapOf(),
        val supportedImageFormats: Set<String> = setOf("png", "jpg")
    )

    private sealed interface ImageIOWrapper {
        fun read(file: File): BufferedImage
        fun write(image: BufferedImage, formatName: String, file: File): Boolean
    }

    private data object RealImageIO : ImageIOWrapper {
        override fun read(file: File): BufferedImage = ImageIO.read(file)

        override fun write(image: BufferedImage, formatName: String, file: File): Boolean =
            ImageIO.write(image, formatName, file)
    }

    private class NullImageIO(
        initialImages: Map<File, RegularImage>,
        val supportedFileFormats: Set<String>
    ) : ImageIOWrapper {
        private val images: MutableMap<File, RegularImage> =
            initialImages.mapValues { deepCopy(it.value) }.toMutableMap()

        override fun read(file: File): BufferedImage =
            images[file]?.toBufferedImage() ?: throw IOException("Can't read input file!")

        override fun write(image: BufferedImage, formatName: String, file: File): Boolean {
            if (!supportedFileFormats.contains(formatName.lowercase(Locale.getDefault()))) {
                return false
            }
            images[file] = RegularImage.fromBufferedImage(image)
            return true
        }

        private fun deepCopy(bi: RegularImage): RegularImage =
            RegularImage.fromBufferedImage(bi.toBufferedImage())
    }
}