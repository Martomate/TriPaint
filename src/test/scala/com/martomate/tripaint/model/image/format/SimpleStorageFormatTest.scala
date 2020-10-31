package com.martomate.tripaint.model.image.format

class SimpleStorageFormatTest extends StorageFormatTest {
  "transformToStorage" should "transform correctly" in {
    triToStorage(0, 0)(0, 0)
    triToStorage(0, 10)(0, 10)
    triToStorage(10, 10)(10, 10)
    triToStorage(20, 10)(10, 0)
  }

  "transformFromStorage" should "transform correctly" in {
    storageToTri(0, 0)(0, 0)
    storageToTri(0, 10)(0, 10)
    storageToTri(10, 10)(10, 10)
    storageToTri(10, 0)(20, 10)
  }

  def triToStorage(from: (Int, Int))(to: (Int, Int)): Unit = {
    make.transformToStorage(trCoords(from)) shouldBe stCoords(to)
  }

  def storageToTri(from: (Int, Int))(to: (Int, Int)): Unit = {
    make.transformFromStorage(stCoords(from)) shouldBe trCoords(to)
  }

  def make: StorageFormat = new SimpleStorageFormat
}
