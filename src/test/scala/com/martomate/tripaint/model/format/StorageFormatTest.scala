package com.martomate.tripaint.model.format

import com.martomate.tripaint.model.coords.{StorageCoords, TriangleCoords}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

abstract class StorageFormatTest extends AnyFlatSpec with Matchers {
  def make: StorageFormat

  "transformToStorage" should "be the inverse of transformFromStorage" in {
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
      f.transformToStorage(f.transformFromStorage(stCoords(c))) shouldBe stCoords(c)
  }
  "transformFromStorage" should "be the inverse of transformToStorage" in {
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
      f.transformFromStorage(f.transformToStorage(trCoords(c))) shouldBe trCoords(c)
  }

  def stCoords(c: (Int, Int)): StorageCoords = StorageCoords(c._1, c._2)
  def trCoords(c: (Int, Int)): TriangleCoords = TriangleCoords(c._1, c._2)
}
