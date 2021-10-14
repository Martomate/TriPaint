package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}
import com.martomate.tripaint.model.image.SaveLocation
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.storage.{ImageStorage, ImageStorageImpl}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.awt.image.BufferedImage
import java.io.File

class ImageSaverToFileTest extends AnyFlatSpec with Matchers {

  "save" should "return false if file format is unsupported" in {
    val file = new File("file.abc")
    val location = SaveLocation(file)
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 16)

    val fs = FileSystem.createNull(supportedImageFormats = Set("png", "jpg"))
    val saver = new ImageSaverToFile

    val success = saver.save(image, new SimpleStorageFormat, location, fs)

    success shouldBe false
  }

  it should "write image if it doesn't exist (old)" in {
    val file = new File("file.png")
    val location = SaveLocation(file)
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 16)

    val fs = FileSystem.createNull()
    val saver = new ImageSaverToFile

    val success = saver.save(image, new SimpleStorageFormat, location, fs)

    success shouldBe true
  }

  it should "write image if it doesn't exist" in {
    val file = new File("file.png")
    val location = SaveLocation(file)
    val format = new SimpleStorageFormat

    val dot1 = StorageCoords(2, 3)
    val dot2 = StorageCoords(15, 0)
    val dot3 = StorageCoords(15, 15)

    val storage = ImageStorageImpl.fromBGColor(Color.Blue, 16)
    storage(format.transformFromStorage(dot1)) = Color.Cyan
    storage(format.transformFromStorage(dot2)) = Color.Magenta
    storage(format.transformFromStorage(dot3)) = Color.Yellow
    val image = makeNewImage(storage.imageSize, storage.imageSize)
    writeImage(image, storage, (0, 0), format)

    val saver = new ImageSaverToFile
    val storedImage = saver.save(storage, format, location, None)

    storedImage.getRGB(dot1.x, dot1.y) shouldBe image.getRGB(dot1.x, dot1.y)
    storedImage.getRGB(dot2.x, dot2.y) shouldBe image.getRGB(dot2.x, dot2.y)
    storedImage.getRGB(dot3.x, dot3.y) shouldBe image.getRGB(dot3.x, dot3.y)
  }

  it should "overwrite part of image if it already exists (old)" in {
    val file = new File("file.png")
    val location = SaveLocation(file, (2, 3))
    val format = new SimpleStorageFormat

    val existingStorage = ImageStorageImpl.fromBGColor(Color.Yellow, 8)
    val existingImage = makeNewImage(8, 8)
    writeImage(existingImage, existingStorage, (0, 0), format)

    val image = ImageStorageImpl.fromBGColor(Color.Cyan, 4)

    val fs = FileSystem.createNull(initialImages = Map(file -> existingImage))
    val saver = new ImageSaverToFile

    val success = saver.save(image, format, location, fs)
    success shouldBe true

    val storedImage = fs.readImage(file).getOrElse(fail())

    storedImage.getRGB(1, 4) shouldBe Color.Yellow.toInt
    storedImage.getRGB(2, 4) shouldBe Color.Cyan.toInt
    storedImage.getRGB(5, 4) shouldBe Color.Cyan.toInt
    storedImage.getRGB(6, 4) shouldBe Color.Yellow.toInt
    storedImage.getRGB(4, 2) shouldBe Color.Yellow.toInt
    storedImage.getRGB(4, 3) shouldBe Color.Cyan.toInt
    storedImage.getRGB(4, 6) shouldBe Color.Cyan.toInt
    storedImage.getRGB(4, 7) shouldBe Color.Yellow.toInt
  }

  it should "overwrite part of image if it already exists" in {
    val file = new File("file.png")
    val location = SaveLocation(file, (2, 3))
    val format = new SimpleStorageFormat

    val existingStorage = ImageStorageImpl.fromBGColor(Color.Yellow, 8)
    val existingImage = makeNewImage(8, 8)
    writeImage(existingImage, existingStorage, (0, 0), format)

    val image = ImageStorageImpl.fromBGColor(Color.Cyan, 4)

    val saver = new ImageSaverToFile

    val storedImage = saver.save(image, format, location, Some(existingImage))

    storedImage.getRGB(1, 4) shouldBe Color.Yellow.toInt
    storedImage.getRGB(2, 4) shouldBe Color.Cyan.toInt
    storedImage.getRGB(5, 4) shouldBe Color.Cyan.toInt
    storedImage.getRGB(6, 4) shouldBe Color.Yellow.toInt
    storedImage.getRGB(4, 2) shouldBe Color.Yellow.toInt
    storedImage.getRGB(4, 3) shouldBe Color.Cyan.toInt
    storedImage.getRGB(4, 6) shouldBe Color.Cyan.toInt
    storedImage.getRGB(4, 7) shouldBe Color.Yellow.toInt
  }

  private def makeNewImage(width: Int, height: Int): BufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)

  private def writeImage(dest: BufferedImage, source: ImageStorage, offset: (Int, Int), format: StorageFormat): Unit = {
    for (y <- 0 until source.imageSize) {
      for (x <- 0 until 2 * y + 1) {
        val tCoords = TriangleCoords(x, y)
        val sCoords = format.transformToStorage(tCoords)

        dest.setRGB(sCoords.x + offset._1, sCoords.y + offset._2, source(tCoords).toInt)
      }
    }
  }
}
