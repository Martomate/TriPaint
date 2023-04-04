package com.martomate.tripaint.infrastructure

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.image.RegularImage
import com.martomate.tripaint.util.Tracker
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.image.BufferedImage
import java.io.File

class FileSystemTest extends AnyFlatSpec with Matchers {

  private val tempDir: String = System.getProperty("java.io.tmpdir")

  "readImage" should "return None if the image does not exist" in {
    val fs = FileSystem.create()
    val file = new File(tempDir, "a_non_existent_file_93784.png")
    file.exists() shouldBe false
    val image = fs.readImage(file)
    image shouldBe None
  }

  it should "return the image if it exists"

  "writeImage" should "return false if the format is not supported" in {
    val fs = FileSystem.create()
    val image = RegularImage.ofSize(10, 10)
    val file = new File(tempDir, "filename72454.xyz")

    try {
      val success = fs.writeImage(image, file)

      // The 'xyz' extension is not supported, so the write is aborted
      success shouldBe false
      file.exists() shouldBe false
    } catch {
      case _: Exception => fail()
    } finally {
      // Clean up if needed
      file.delete()
    }
  }

  it should "return true if the image was written" in {
    val fs = FileSystem.create()
    val image = RegularImage.ofSize(10, 10)
    val file = new File(tempDir, "filename38475.png")
    file.exists() shouldBe false

    try {
      val success = fs.writeImage(image, file)

      // The 'png' extension is supported, so the write can proceed
      success shouldBe true
      file.exists() shouldBe true
    } catch {
      case _: Exception => fail()
    } finally {
      // Clean up after the test
      file.delete()
    }
  }

  it should "save an opaque version of the image" in {
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
      success shouldBe true

      fs.readImage(file) shouldBe Some(opaqueImage)
    } finally {
      file.delete()
    }
  }

  it should "notify trackers about the file being written" in {
    val fs = FileSystem.create()
    val image = RegularImage.ofSize(10, 10)
    val file = new File(tempDir, "filename38475.png")

    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    tracker.events.size shouldBe 0

    try {
      fs.writeImage(image, file)
    } finally {
      // Clean up after the test
      file.delete()
    }

    tracker.events shouldBe Seq(FileSystem.Event.ImageWritten(image, file))
  }

  "null version" should "return no image when reading by default" in {
    FileSystem.createNull().readImage(new File("file.png")) shouldBe None
  }

  it should "return the correct pre-configured image when reading" in {
    val image = RegularImage.fill(3, 4, Color.Cyan)
    val fs = FileSystem.createNull(
      new FileSystem.NullArgs(
        initialImages = Map(
          new File("image.png") -> image
        )
      )
    )
    fs.readImage(new File("image.png")) shouldBe Some(image)
    fs.readImage(new File("something_else.png")) shouldBe None
  }

  it should "return true after successfully writing an image" in {
    val image = RegularImage.fill(3, 4, Color.Cyan)
    FileSystem.createNull().writeImage(image, new File("a.png")) shouldBe true
  }

  it should "not actually write an image to disk" in {
    val image = RegularImage.fill(3, 4, Color.Cyan)
    val file = new File(tempDir, "filename23843.png")

    FileSystem.createNull().writeImage(image, file) shouldBe true
    file.exists() shouldBe false
  }

  it should "notify trackers after successfully writing an image" in {
    val image = RegularImage.fill(3, 4, Color.Cyan)

    val fs = FileSystem.createNull()
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    fs.writeImage(image, new File("a.png"))

    tracker.events shouldBe Seq(FileSystem.Event.ImageWritten(image, new File("a.png")))
  }

  it should "return false if the image cannot be written" in {
    val image = RegularImage.fill(3, 4, Color.Cyan)

    val config = new FileSystem.NullArgs(supportedImageFormats = Set("jpg", "gif"))
    val fs = FileSystem.createNull(config)

    fs.writeImage(image, new File("a.png")) shouldBe false
  }

  it should "not notify trackers after failing to write an image" in {
    val image = RegularImage.fill(3, 4, Color.Cyan)

    val config = new FileSystem.NullArgs(supportedImageFormats = Set("jpg", "gif"))
    val fs = FileSystem.createNull(config)
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    fs.writeImage(image, new File("a.png"))

    tracker.events shouldBe Seq()
  }

  it should "overwrite existing images" in {
    val existingImage = RegularImage.fill(3, 4, Color.Yellow)
    val newImage = RegularImage.fill(3, 4, Color.Cyan)

    val config = new FileSystem.NullArgs(initialImages = Map(new File("a.png") -> existingImage))
    val fs = FileSystem.createNull(config)
    val tracker = Tracker.withStorage[FileSystem.Event]
    fs.trackChanges(tracker)

    fs.writeImage(newImage, new File("a.png")) shouldBe true

    tracker.events shouldBe Seq(FileSystem.Event.ImageWritten(newImage, new File("a.png")))
  }
}
