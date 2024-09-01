package tripaint.app

import org.junit.jupiter.api.Assertions.assertEquals
import tripaint.color.Color
import tripaint.image.RegularImage
import tripaint.util.Tracker
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

class FileSystemTest {
    private val tempDir: String = System.getProperty("java.io.tmpdir")

    @Test
    fun `readImage should return None if the image does not exist`() {
        val fs = FileSystem.create()
        val file = File(tempDir, "a_non_existent_file_93784.png")
        assert(!file.exists())
        val image = fs.readImage(file)
        assertEquals(null, image)
    }

    @Test
    @Ignore
    fun `readImage should return the image if it exists`() {}

    @Test
    fun `writeImage should return false if the format is not supported`() {
        val fs = FileSystem.create()
        val image = RegularImage.ofSize(10, 10)
        val file = File(tempDir, "filename72454.xyz")

        try {
            val success = fs.writeImage(image, file)

            // The 'xyz' extension is not supported, so the write is aborted
            assert(!success)
            assert(!file.exists())
        } finally {
            // Clean up if needed
            file.delete()
        }
    }

    @Test
    fun `writeImage should return true if the image was written`() {
        val fs = FileSystem.create()
        val image = RegularImage.ofSize(10, 10)
        val file = File(tempDir, "filename38475.png")
        assert(!file.exists())

        try {
            val success = fs.writeImage(image, file)

            // The 'png' extension is supported, so the write can proceed
            assert(success)
            assert(file.exists())
        } finally {
            // Clean up after the test
            file.delete()
        }
    }

    @Test
    fun `writeImage should save an opaque version of the image`() {
        val fs = FileSystem.create()
        val file = File(tempDir, "filename72454.png")

        val image = RegularImage.fill(10, 10, Color.Cyan)
        image.setColor(3, 4, Color(0.1, 0.2, 0.3, 0.4))
        image.setColor(5, 6, Color.Black)
        image.setColor(4, 7, Color(0.0, 0.0, 0.0, 0.0))

        val opaqueImage = RegularImage.fill(10, 10, Color.Cyan)
        opaqueImage.setColor(3, 4, Color(0.1, 0.2, 0.3, 1.0))
        opaqueImage.setColor(5, 6, Color.Black)
        opaqueImage.setColor(4, 7, Color(0.0, 0.0, 0.0, 1.0))

        try {
            val success = fs.writeImage(image, file)
            assert(success)

            assertEquals(opaqueImage, fs.readImage(file))
        } finally {
            file.delete()
        }
    }

    @Test
    fun `writeImage should notify trackers about the file being written`() {
        val fs = FileSystem.create()
        val image = RegularImage.ofSize(10, 10)
        val file = File(tempDir, "filename38475.png")

        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        assertEquals(0, tracker.events.size)

        try {
            fs.writeImage(image, file)
        } finally {
            // Clean up after the test
            file.delete()
        }

        assertEquals(listOf(FileSystem.Event.ImageWritten(image, file)), tracker.events)
    }
}

class FileSystemNullTest {
    @Test
    fun `readImage returns None by default`() {
        assertEquals(null, FileSystem.createNull().readImage(File("file.png")))
    }

    @Test
    fun `readImage returns the pre-configured image if set`() {
        val image = RegularImage.fill(3, 4, Color.Cyan)
        val fs = FileSystem.createNull(FileSystem.NullArgs(initialImages = mapOf(Pair(File("image.png"), image))))
        assertEquals(image, fs.readImage(File("image.png")))
        assertEquals(null, fs.readImage(File("something_else.png")))
    }

    @Test
    fun `writeImage returns true after successfully writing an image`() {
        val image = RegularImage.fill(3, 4, Color.Cyan)
        assert(FileSystem.createNull().writeImage(image, File("a.png")))
    }

    @Test
    fun `writeImage does not actually write an image to disk`() {
        val tempDir: String = System.getProperty("java.io.tmpdir")

        val image = RegularImage.fill(3, 4, Color.Cyan)
        val file = File(tempDir, "filename23843.png")

        assert(FileSystem.createNull().writeImage(image, file))
        assert(!file.exists())
    }

    @Test
    fun `writeImage notifies trackers after successfully writing an image`() {
        val image = RegularImage.fill(3, 4, Color.Cyan)

        val fs = FileSystem.createNull()
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        fs.writeImage(image, File("a.png"))

        assertEquals(listOf(FileSystem.Event.ImageWritten(image, File("a.png"))), tracker.events)
    }

    @Test
    fun `writeImage returns false if the image could not be written`() {
        val image = RegularImage.fill(3, 4, Color.Cyan)

        val config = FileSystem.NullArgs(supportedImageFormats = setOf("jpg", "gif"))
        val fs = FileSystem.createNull(config)

        assert(!fs.writeImage(image, File("a.png")))
    }

    @Test
    fun `writeImage does not notify trackers if the image could not be written`() {
        val image = RegularImage.fill(3, 4, Color.Cyan)

        val config = FileSystem.NullArgs(supportedImageFormats = setOf("jpg", "gif"))
        val fs = FileSystem.createNull(config)
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        fs.writeImage(image, File("a.png"))

        assertEquals(listOf<FileSystem.Event>(), tracker.events)
    }

    @Test
    fun `writeImage overwrites the existing image if needed`() {
        val existingImage = RegularImage.fill(3, 4, Color.Yellow)
        val newImage = RegularImage.fill(3, 4, Color.Cyan)

        val config = FileSystem.NullArgs(initialImages = mapOf(Pair(File("a.png"), existingImage)))
        val fs = FileSystem.createNull(config)
        val tracker = Tracker.withStorage<FileSystem.Event>()
        fs.trackChanges(tracker)

        assert(fs.writeImage(newImage, File("a.png")))

        assertEquals(listOf(FileSystem.Event.ImageWritten(newImage, File("a.png"))), tracker.events)
    }
}