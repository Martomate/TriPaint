package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.image.storage.ImageStorageImpl
import com.martomate.tripaint.model.image.{RegularImage, SaveLocation}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File

class ImageSaverToFileTest extends AnyFlatSpec with Matchers {

  "overwritePartOfImage" should "write image if it doesn't exist" in {
    val file = new File("file.png")
    val location = SaveLocation(file)
    val format = new SimpleStorageFormat

    val storage = ImageStorageImpl.fromBGColor(Color.Blue, 16)
    storage(format.transformFromStorage(StorageCoords(2, 3))) = Color.Cyan
    storage(format.transformFromStorage(StorageCoords(15, 0))) = Color.Magenta
    storage(format.transformFromStorage(StorageCoords(15, 15))) = Color.Yellow

    val expectedImage = RegularImage.fill(storage.imageSize, storage.imageSize, Color.Blue)
    expectedImage.pasteImage(StorageCoords(0, 0), storage.toRegularImage(format))

    val saver = new ImageSaverToFile
    val storedImage = saver.overwritePartOfImage(storage, format, location.offset, None)

    storedImage shouldBe expectedImage
  }

  it should "overwrite part of image if it already exists" in {
    val offset = StorageCoords(2, 3)
    val format = new SimpleStorageFormat

    val existingStorage = ImageStorageImpl.fromBGColor(Color.Yellow, 8)

    val existingImage = RegularImage.ofSize(8, 8) // ???
    existingImage.pasteImage(StorageCoords(0, 0), existingStorage.toRegularImage(format))

    val image = ImageStorageImpl.fromBGColor(Color.Cyan, 4)

    val saver = new ImageSaverToFile

    val storedImage = saver.overwritePartOfImage(image, format, offset, Some(existingImage))

    val expectedImage = RegularImage.fill(8, 8, Color.Yellow)
    expectedImage.pasteImage(offset, RegularImage.fill(4, 4, Color.Cyan))

    storedImage shouldBe expectedImage
  }

}
