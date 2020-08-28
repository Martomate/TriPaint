package com.martomate.tripaint.model.coords

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StorageCoordsTest extends AnyFlatSpec with Matchers {
  "constructor" should "require x >= 0" in {
    assertThrows[IllegalArgumentException](StorageCoords(-1, 0))
    assertThrows[IllegalArgumentException](StorageCoords(-10, 0))
    assertThrows[IllegalArgumentException](StorageCoords(-10, 100))
    StorageCoords(0, 10)
  }
  "constructor" should "require y >= 0" in {
    assertThrows[IllegalArgumentException](StorageCoords(0, -1))
    assertThrows[IllegalArgumentException](StorageCoords(0, -10))
    assertThrows[IllegalArgumentException](StorageCoords(100, -10))
    StorageCoords(10, 0)
  }
}
