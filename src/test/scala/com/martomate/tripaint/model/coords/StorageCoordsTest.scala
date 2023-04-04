package com.martomate.tripaint.model.coords

import munit.FunSuite

class StorageCoordsTest extends FunSuite {
  test("constructor should require x >= 0") {
    intercept[IllegalArgumentException](StorageCoords(-1, 0))
    intercept[IllegalArgumentException](StorageCoords(-10, 0))
    intercept[IllegalArgumentException](StorageCoords(-10, 100))
    StorageCoords(0, 10)
  }
  test("constructor should require y >= 0") {
    intercept[IllegalArgumentException](StorageCoords(0, -1))
    intercept[IllegalArgumentException](StorageCoords(0, -10))
    intercept[IllegalArgumentException](StorageCoords(100, -10))
    StorageCoords(10, 0)
  }
}
