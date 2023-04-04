package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.Color
import com.martomate.tripaint.model.coords.{
  GlobalPixCoords,
  PixelCoords,
  TriImageCoords,
  TriangleCoords
}
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridColorLookup}
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.storage.ImageStorage
import munit.FunSuite

class BlurEffectTest extends FunSuite {
  test("name should be 'Blur'") {
    assertEquals(new BlurEffect(3).name, "Blur")
  }

  test("the effect should be symmetric far from border") {
    val radius = 2
    val imageSize = 32
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2, imageSize / 2))
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2 + 2, imageSize / 2))
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2, imageSize / 2 - 2))
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2 - 2, imageSize / 2 + 2))
  }

  private def checkSymmetrySimple(
      radius: Int,
      imageSize: Int,
      dotLocation: TriangleCoords
  ): Unit = {
    val effect = new BlurEffect(radius)
    val thisImage = TriImageCoords(0, 0)

    val storage = ImageStorage.fromBGColor(Color.Black, imageSize)
    storage(dotLocation) = Color.White

    val grid = new ImageGrid(imageSize)
    grid.set(new ImageContent(thisImage, storage))

    effect.action(Seq(thisImage), grid)

    for (dx <- 0 to radius) {
      val look1 = TriangleCoords(dotLocation.x - dx, dotLocation.y)
      val look2 = TriangleCoords(dotLocation.x + dx, dotLocation.y)
      val col1 = storage(look1)
      val col2 = storage(look2)
      try {
        assertEquals(col1, col2)
      } catch {
        case e: Exception =>
          println(s"dx = $dx")
          throw e
      }
    }
  }

  test("the effect should be symmetric on the border between images") {
    val radius = 2
    val imageSize = 16 // has to be high enough to not limit the search
    checkSymmetryBorder(radius, imageSize, TriangleCoords(0, imageSize / 2), TriImageCoords(-1, 0))
    checkSymmetryBorder(
      radius,
      imageSize,
      TriangleCoords(imageSize, imageSize / 2),
      TriImageCoords(1, 0)
    )
    checkSymmetryBorder(
      radius,
      imageSize,
      TriangleCoords(imageSize, imageSize - 1),
      TriImageCoords(1, -1)
    )
  }

  private def checkSymmetryBorder(
      radius: Int,
      imageSize: Int,
      dotLocation: TriangleCoords,
      borderingImage: TriImageCoords
  ): Unit = {
    val effect = new BlurEffect(radius)
    val thisImage = TriImageCoords(0, 0)

    val storage = ImageStorage.fromBGColor(Color.Black, imageSize)
    val storage2 = ImageStorage.fromBGColor(Color.Black, imageSize)
    storage(dotLocation) = Color.White

    val grid = new ImageGrid(imageSize)
    grid.set(new ImageContent(thisImage, storage))
    grid.set(new ImageContent(borderingImage, storage2))

    effect.action(Seq(thisImage, borderingImage), grid)

    val colorLookup = new ImageGridColorLookup(grid)
    val dotGlobal = PixelCoords(thisImage, dotLocation).toGlobal(imageSize)

    for (dx <- 0 to radius) {
      val look1 = GlobalPixCoords(dotGlobal.x - dx, dotGlobal.y)
      val look2 = GlobalPixCoords(dotGlobal.x + dx, dotGlobal.y)
      val col1 = colorLookup.lookup(look1).get
      val col2 = colorLookup.lookup(look2).get
      try {
        assertEquals(col1, col2)
      } catch {
        case e: Exception =>
          println(s"dx = $dx")
          throw e
      }
    }
  }

  test("the effect should be additive for multiple sources".ignore) {}
}
