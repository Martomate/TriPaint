package com.martomate.tripaint.model.format

class RecursiveStorageFormatTest extends StorageFormatTest {
  "transformToStorage" should "transform correctly" in {
    // upper triangle
    triToStorage(0, 0)(0, 0)
    triToStorage(0, 1)(0, 1)
    triToStorage(1, 1)(1, 1)
    triToStorage(2, 1)(1, 0)

    // left triangle
    triToStorage(0, 2)(0, 2)
    triToStorage(0, 3)(0, 3)
    triToStorage(1, 3)(1, 3)
    triToStorage(2, 3)(1, 2)

    // right triangle
    triToStorage(4, 2)(2, 0)
    triToStorage(4, 3)(2, 1)
    triToStorage(5, 3)(3, 1)
    triToStorage(6, 3)(3, 0)

    // center triangle
    triToStorage(3, 3)(3, 3)
    triToStorage(3, 2)(3, 2)
    triToStorage(2, 2)(2, 2)
    triToStorage(1, 2)(2, 3)

    // next row
    triToStorage(0, 4)(0, 4)
    triToStorage(1, 4)(4, 7)
    triToStorage(2, 4)(4, 6)
    triToStorage(3, 4)(5, 6)
    triToStorage(4, 4)(4, 4)
    triToStorage(5, 4)(6, 5)
    triToStorage(6, 4)(6, 4)
    triToStorage(7, 4)(7, 4)
    triToStorage(8, 4)(4, 0)
  }

  def triToStorage(from: (Int, Int))(to: (Int, Int)): Unit = {
    make.transformToStorage(trCoords(from)) shouldBe stCoords(to)
  }

  override def make: StorageFormat = new RecursiveStorageFormat
}
