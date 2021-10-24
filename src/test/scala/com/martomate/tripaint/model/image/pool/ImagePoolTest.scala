package com.martomate.tripaint.model.image.pool

import com.martomate.tripaint.infrastructure.FileSystem
import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.{SimpleStorageFormat, StorageFormat}
import com.martomate.tripaint.model.image.save.ImageSaverToFile
import com.martomate.tripaint.model.image.storage.{ImageStorage, ImageStorageFactory, ImageStorageImpl}
import com.martomate.tripaint.model.image.{RegularImage, SaveLocation, pool}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import scalafx.scene.paint.{Color => FXColor}

import java.awt.image.BufferedImage
import java.io.File
import scala.util.{Failure, Success}

class ImagePoolTest extends AnyFlatSpec with Matchers with MockFactory {
  implicit val collisionHandler: ImageSaveCollisionHandler = mock[ImageSaveCollisionHandler]
  val storageFormat: StorageFormat = new SimpleStorageFormat

  def make(factory: ImageStorageFactory = null): ImagePool = new ImagePool(factory)

  "save" should "return false if the image doesn't exist" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    make().save(image, null, null) shouldBe false
  }

  it should "return false if the saver reports failure" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = make()
    f.move(image, location, info)

    val fs = FileSystem.createNull(supportedImageFormats = Set())
    f.save(image, new ImageSaverToFile, fs) shouldBe false
  }

  it should "notify listeners and return true if the saver reports success" in {
    val listener = mock[ImagePoolListener]
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(new File("a.png"))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val f = make()
    f.addListener(listener)
    f.move(image, location, info)

    val saver = new ImageSaverToFile
    listener.onImageSaved _ expects(image, saver)

    f.save(image, saver, FileSystem.createNull()) shouldBe true
  }

  it should "write image if it does not exist" in {
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val imagePool = make()
    imagePool.move(image, location, info)

    val saver = new ImageSaverToFile
    val fs = FileSystem.createNull()

    imagePool.save(image, saver, fs)

    val expectedImage = RegularImage.fill(2, 2, Color.Blue)

    val result = fs.readImage(new File(path))
    result.isDefined shouldBe true
    RegularImage.fromBufferedImage(result.get) shouldBe expectedImage
  }

  it should "overwrite image if it exists and has the same size" in {
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val location = SaveLocation(new File(path))
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val imagePool = make()
    imagePool.move(image, location, info)

    val saver = new ImageSaverToFile

    val existingImage = RegularImage.fill(2, 2, Color.Red).toBufferedImage
    val fs = FileSystem.createNull(initialImages = Map(new File(path) -> existingImage))

    imagePool.save(image, saver, fs)

    val expectedImage = RegularImage.fill(2, 2, Color.Blue)

    val result = fs.readImage(new File(path))
    result.isDefined shouldBe true
    RegularImage.fromBufferedImage(result.get) shouldBe expectedImage
  }

  it should "overwrite part of image if there already exists a bigger image" in {
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val imagePool = make()
    imagePool.move(image, location, info)

    val saver = new ImageSaverToFile

    val existingImage = RegularImage.fill(3, 5, Color.Red).toBufferedImage
    val fs = FileSystem.createNull(initialImages = Map(new File(path) -> existingImage))

    imagePool.save(image, saver, fs)

    val expectedImage = RegularImage.fill(3, 5, Color.Red)
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    val result = fs.readImage(new File(path))
    result.isDefined shouldBe true
    RegularImage.fromBufferedImage(result.get) shouldBe expectedImage
  }

  it should "overwrite part of image if there already exists an image even if it is too small" in {
    val image = ImageStorageImpl.fromBGColor(Color.Blue, 2)
    val path = "a.png"
    val offset = StorageCoords(1, 2)
    val location = SaveLocation(new File(path), offset)
    val format = new SimpleStorageFormat
    val info = pool.SaveInfo(format)

    val imagePool = make()
    imagePool.move(image, location, info)

    val saver = new ImageSaverToFile

    val existingImage = RegularImage.fill(3, 2, Color.Red).toBufferedImage
    val fs = FileSystem.createNull(initialImages = Map(new File(path) -> existingImage))

    imagePool.save(image, saver, fs)

    val expectedImage = RegularImage.fill(3, 4, Color.Black)
    expectedImage.pasteImage(StorageCoords(0, 0), RegularImage.fill(3, 2, Color.Red))
    expectedImage.pasteImage(offset, RegularImage.fill(2, 2, Color.Blue))

    val result = fs.readImage(new File(path))
    result.isDefined shouldBe true
    RegularImage.fromBufferedImage(result.get) shouldBe expectedImage
  }

  "locationOf" should "return None if the image doesn't exist" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    make().locationOf(image) shouldBe None
  }

  it should "return the location of the image if it exists" in {
    val image = ImageStorageImpl.fromBGColor(Color.Black, 2)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.locationOf(image) shouldBe Some(location)
  }

  "fromBGColor" should "return what the factory returns" in {
    val factory = mock[ImageStorageFactory]
    val bgColor = Color.Blue
    val imageSize = 16
    val returnImage = ImageStorageImpl.fromBGColor(FXColor.Orange, imageSize)

    factory.fromBGColor _ expects(bgColor, imageSize) returns returnImage

    make(factory).fromBGColor(bgColor, imageSize) shouldBe returnImage
  }

  "fromFile" should "return the image at that location if it exists" in {
    val image = ImageStorageImpl.fromBGColor(FXColor.Blue, 16)
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    val f = make()
    f.move(image, location, info)

    f.fromFile(location, storageFormat, 16, FileSystem.createNull()) shouldBe Success(image)
  }

  it should "return Failure if there is no image there and the loading failed" in {
    val location = SaveLocation(null)
    val imageSize = 16
    val fs = FileSystem.createNull(initialImages = Map.empty)

    val pool = make(ImageStorageImpl)

    pool.fromFile(location, storageFormat, imageSize, fs).isFailure shouldBe true
  }

  it should "save and return the newly loaded image if there was none already" in {
    val file = new File("path/to/image.png")
    val location = SaveLocation(file)
    val imageSize = 16

    val image = ImageStorageImpl.fromBGColor(FXColor.Orange, imageSize)
    val regularImage = image.toRegularImage(storageFormat)
    val fs = FileSystem.createNull(initialImages = Map(
      file -> regularImage.toBufferedImage
    ))

    val pool = make(ImageStorageImpl)

    pool.fromFile(location, storageFormat, imageSize, fs) match {
      case Success(actualImage) =>
        actualImage.toRegularImage(storageFormat) shouldBe regularImage
        pool.locationOf(actualImage) shouldBe Some(location)
      case Failure(_) => fail()
    }
  }

  it should "load image with offset" in {
    val file = new File("path/to/image.png")
    val offset = StorageCoords(2, 3)
    val location = SaveLocation(file, offset)
    val imageSize = 16

    val image = ImageStorageImpl.fromBGColor(FXColor.Orange, imageSize)
    val regularImage = image.toRegularImage(storageFormat)

    val storedImage = RegularImage.ofSize(imageSize + offset.x, imageSize + offset.y)
    storedImage.pasteImage(offset, regularImage)
    val fs = FileSystem.createNull(initialImages = Map(
      file -> storedImage.toBufferedImage
    ))

    val pool = make(ImageStorageImpl)

    pool.fromFile(location, storageFormat, imageSize, fs) match {
      case Success(actualImage) =>
        actualImage.toRegularImage(storageFormat) shouldBe regularImage
        pool.locationOf(actualImage) shouldBe Some(location)
      case Failure(_) => fail()
    }
  }

  "move" should "set the image and return true if the location is empty" in {
    val p = make()
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)
    p.move(image, location, info) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "simply return true if the image is already there" in {
    val p = make()
    val image = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)
    p.move(image, location, info)
    p.move(image, location, info) shouldBe true
    p.locationOf(image) shouldBe Some(location)
  }

  it should "return false if the handler doesn't accept the collision" in {
    val handler = collisionHandler
    val p = make()
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(None)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe false
  }

  it should "replace the current image, notify listeners, and return true if the handler wants to replace it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = make()
    p.addListener(listener)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(Some(true))
    (listener.onImageReplaced _).expects(currentImage, newImage, location)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe true
    p.locationOf(currentImage) shouldBe None
    p.locationOf(newImage) shouldBe Some(location)
  }

  it should "keep the current image, notify listeners, and return true if the handler wants to keep it" in {
    val handler = collisionHandler
    val listener = mock[ImagePoolListener]

    val p = make()
    p.addListener(listener)
    val currentImage = stub[ImageStorage]
    val newImage = stub[ImageStorage]
    val location = SaveLocation(null)
    val info = pool.SaveInfo(null)

    (handler.shouldReplaceImage _).expects(currentImage, newImage, location).returns(Some(false))
    (listener.onImageReplaced _).expects(newImage, currentImage, location)

    p.move(currentImage, location, info)
    p.move(newImage, location, info) shouldBe true
    p.locationOf(currentImage) shouldBe Some(location)
    p.locationOf(newImage) shouldBe None
  }
}
