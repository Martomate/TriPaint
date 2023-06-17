package com.martomate.tripaint.model.image.format

import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}
import munit.FunSuite

abstract class StorageFormatTest extends FunSuite {
  def make: StorageFormat

  test("transformToStorage should be the inverse of transformFromStorage") {
    val f = make

    comp(0, 0)
    comp(10, 0)
    comp(0, 10)
    comp(10, 10)
    comp(20, 10)

    for (y <- 0 to 100) {
      for (x <- 0 to 100) {
        comp(x, y)
      }
    }

    def comp(c: (Int, Int)): Unit =
      assertEquals(f.transform(f.reverse(stCoords(c))), stCoords(c))
  }
  test("transformFromStorage should be the inverse of transformToStorage") {
    val f = make

    comp(0, 0)
    comp(0, 10)
    comp(10, 10)
    comp(20, 10)

    for (y <- 0 to 100) {
      for (x <- 0 to 2 * y) {
        comp(x, y)
      }
    }

    def comp(c: (Int, Int)): Unit =
      assertEquals(f.reverse(f.transform(trCoords(c))), trCoords(c))
  }

  def stCoords(c: (Int, Int)): StorageCoords = StorageCoords(c._1, c._2)
  def trCoords(c: (Int, Int)): TriangleCoords = TriangleCoords(c._1, c._2)
}
