package com.martomate.tripaint.model.image.save

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.StorageCoords
import com.martomate.tripaint.model.image.format.SimpleStorageFormat
import com.martomate.tripaint.model.image.storage.ImageStorage
import com.martomate.tripaint.model.image.{RegularImage, SaveLocation}
import munit.FunSuite

import java.io.File

class ImageSaverToFileTest extends FunSuite {

  test("overwritePartOfImage should write image if it doesn't exist") {
    val file = new File("file.png")
    val location = SaveLocation(file)
    val format = new SimpleStorageFormat

    val storage = ImageStorage.fromBGColor(Color.Blue, 16)
    storage(format.transformFromStorage(StorageCoords(2, 3))) = Color.Cyan
    storage(format.transformFromStorage(StorageCoords(15, 0))) = Color.Magenta
    storage(format.transformFromStorage(StorageCoords(15, 15))) = Color.Yellow

    val expectedImage = RegularImage.fill(storage.imageSize, storage.imageSize, Color.Blue)
    expectedImage.pasteImage(StorageCoords(0, 0), storage.toRegularImage(format))

    val storedImage = ImageSaverToFile.overwritePartOfImage(storage, format, location.offset, None)

    assertEquals(storedImage, expectedImage)
  }

  test("overwritePartOfImage should overwrite part of image if it already exists") {
    val offset = StorageCoords(2, 3)
    val format = new SimpleStorageFormat

    val existingStorage = ImageStorage.fromBGColor(Color.Yellow, 8)

    val existingImage = RegularImage.ofSize(8, 8) // ???
    existingImage.pasteImage(StorageCoords(0, 0), existingStorage.toRegularImage(format))

    val image = ImageStorage.fromBGColor(Color.Cyan, 4)

    val storedImage =
      ImageSaverToFile.overwritePartOfImage(image, format, offset, Some(existingImage))

    val expectedImage = RegularImage.fill(8, 8, Color.Yellow)
    expectedImage.pasteImage(offset, RegularImage.fill(4, 4, Color.Cyan))

    assertEquals(storedImage, expectedImage)
  }

}
