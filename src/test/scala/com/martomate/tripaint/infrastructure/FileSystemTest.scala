package com.martomate.tripaint.infrastructure

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.util.Tracker

import munit.FunSuite

import java.io.File

class FileSystemTest extends FunSuite {

  private val tempDir: String = System.getProperty("java.io.tmpdir")

  test("readImage should return None if the image does not exist") {
    val fs = FileSystem.create()
    val file = new File(tempDir, "a_non_existent_file_93784.png")
    assert(!file.exists())
    val image = fs.readImage(file)
    assertEquals(image, None)
  }

  test("readImage should return the image if it exists".ignore) {}

  test("writeImage should return false if the format is not supported") {
    val fs = FileSystem.create()
    val image = RegularImage.ofSize(10, 10)
    val file = new File(tempDir, "filename72454.xyz")

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

  test("writeImage should return true if the image was written") {
    val fs = FileSystem.create()
    val image = RegularImage.ofSize(10, 10)
    val file = new File(tempDir, "filename38475.png")
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

  test("writeImage should save an opaque version of the image") {
    val fs = FileSystem.create()
    val file = new File(tempDir, "filename72454.png")

    val image = RegularImage.fill(10, 10, Color.Cyan)
    image.setColor(3, 4, Color(0.1, 0.2, 0.3, 0.4))
    image.setColor(5, 6, Color.Black)
    image.setColor(4, 7, Color(0, 0, 0, 0))

    val opaqueImage = RegularImage.fill(10, 10, Color.Cyan)
    opaqueImage.setColor(3, 4, Color(0.1, 0.2, 0.3, 1.0))
    opaqueImage.setColor(5, 6, Color.Black)
    opaqueImage.setColor(4, 7, Color(0, 0, 0, 1.0))

    try {
      val success = fs.writeImage(image, file)
      assert(success)

      assertEquals(fs.readImage(file), Some(opaqueImage))
    } finally {
      file.delete()
    }
  }

  test("writeImage should notify trackers about the file being written") {
    val fs = FileSystem.create()
    val image = RegularImage.ofSize(10, 10)
    val file = new File(tempDir, "filename38475.png")

    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    assertEquals(tracker.events.size, 0)

    try {
      fs.writeImage(image, file)
    } finally {
      // Clean up after the test
      file.delete()
    }

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(image, file)))
  }
}

class FileSystemNullTest extends FunSuite {
  test("readImage returns None by default") {
    assertEquals(FileSystem.createNull().readImage(new File("file.png")), None)
  }

  test("readImage returns the pre-configured image if set") {
    val image = RegularImage.fill(3, 4, Color.Cyan)
    val fs = FileSystem.createNull(
      FileSystem.NullArgs(
        initialImages = Map(
          new File("image.png") -> image
        )
      )
    )
    assertEquals(fs.readImage(new File("image.png")), Some(image))
    assertEquals(fs.readImage(new File("something_else.png")), None)
  }

  test("writeImage returns true after successfully writing an image") {
    val image = RegularImage.fill(3, 4, Color.Cyan)
    assert(FileSystem.createNull().writeImage(image, new File("a.png")))
  }

  test("writeImage does not actually write an image to disk") {
    val tempDir: String = System.getProperty("java.io.tmpdir")

    val image = RegularImage.fill(3, 4, Color.Cyan)
    val file = new File(tempDir, "filename23843.png")

    assert(FileSystem.createNull().writeImage(image, file))
    assert(!file.exists())
  }

  test("writeImage notifies trackers after successfully writing an image") {
    val image = RegularImage.fill(3, 4, Color.Cyan)

    val fs = FileSystem.createNull()
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    fs.writeImage(image, new File("a.png"))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(image, new File("a.png"))))
  }

  test("writeImage returns false if the image could not be written") {
    val image = RegularImage.fill(3, 4, Color.Cyan)

    val config = FileSystem.NullArgs(supportedImageFormats = Set("jpg", "gif"))
    val fs = FileSystem.createNull(config)

    assert(!fs.writeImage(image, new File("a.png")))
  }

  test("writeImage does not notify trackers if the image could not be written") {
    val image = RegularImage.fill(3, 4, Color.Cyan)

    val config = FileSystem.NullArgs(supportedImageFormats = Set("jpg", "gif"))
    val fs = FileSystem.createNull(config)
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    fs.writeImage(image, new File("a.png"))

    assertEquals(tracker.events, Seq())
  }

  test("writeImage overwrites the existing image if needed") {
    val existingImage = RegularImage.fill(3, 4, Color.Yellow)
    val newImage = RegularImage.fill(3, 4, Color.Cyan)

    val config = FileSystem.NullArgs(initialImages = Map(new File("a.png") -> existingImage))
    val fs = FileSystem.createNull(config)
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    assert(fs.writeImage(newImage, new File("a.png")))

    assertEquals(tracker.events, Seq(FileSystem.Event.ImageWritten(newImage, new File("a.png"))))
  }
}
