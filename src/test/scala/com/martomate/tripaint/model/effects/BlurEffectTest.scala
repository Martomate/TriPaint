package com.martomate.tripaint.model.effects

import com.martomate.tripaint.model.ExtendedColor
import com.martomate.tripaint.model.content.ImageContent
import com.martomate.tripaint.model.coords.{GlobalPixCoords, PixelCoords, TriImageCoords, TriangleCoords}
import com.martomate.tripaint.model.grid.{ImageGrid, ImageGridColorLookup}
import com.martomate.tripaint.model.storage.ImageStorageImpl
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import scalafx.scene.paint.Color

class BlurEffectTest extends FlatSpec with Matchers with MockFactory {
  "name" should "be 'Blur'" in {
    new BlurEffect(3).name shouldBe "Blur"
  }

  "the effect" should "be symmetric far from border" in {
    val radius = 2
    val imageSize = 32
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2, imageSize / 2))
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2 + 2, imageSize / 2))
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2, imageSize / 2 - 2))
    checkSymmetrySimple(radius, imageSize, TriangleCoords(imageSize / 2 - 2, imageSize / 2 + 2))
  }

  private def checkSymmetrySimple(radius: Int, imageSize: Int, dotLocation: TriangleCoords): Unit = {
    val effect = new BlurEffect(radius)
    val thisImage = TriImageCoords(0, 0)
    val grid = stub[ImageGrid]
    val image = stub[ImageContent]
    val storage = ImageStorageImpl.fromBGColor(Color.Black, imageSize)

    grid.imageSize _ when() returns imageSize
    grid.apply _ when thisImage returns Some(image)
    grid.apply _ when * returns None

    image.storage _ when() returns storage

    storage(dotLocation) = Color.White

    effect.action(Seq(thisImage), grid)

    for (dx <- 0 to radius) {
      val look1 = TriangleCoords(dotLocation.x - dx, dotLocation.y)
      val look2 = TriangleCoords(dotLocation.x + dx, dotLocation.y)
      val col1 = ExtendedColor.colorToExtendedColor(storage(look1))
      val col2 = ExtendedColor.colorToExtendedColor(storage(look2))
      try {
        col1 shouldBe col2
      } catch {
        case e: Exception =>
          println(s"dx = $dx")
          throw e
      }
    }
  }

  it should "be symmetric on the border between images" in {
    val radius = 2
    val imageSize = 8
    checkSymmetryBorder(radius, imageSize, TriangleCoords(0, imageSize / 2), TriImageCoords(-1, 0))
    checkSymmetryBorder(radius, imageSize, TriangleCoords(imageSize, imageSize / 2), TriImageCoords(1, 0))
    checkSymmetryBorder(radius, imageSize, TriangleCoords(imageSize, imageSize - 1), TriImageCoords(1, -1))
  }

  private def checkSymmetryBorder(radius: Int, imageSize: Int, dotLocation: TriangleCoords, borderingImage: TriImageCoords): Unit = {
    val effect = new BlurEffect(radius)
    val thisImage = TriImageCoords(0, 0)
    val grid = stub[ImageGrid]
    val image = stub[ImageContent]
    val image2 = stub[ImageContent]
    val storage = ImageStorageImpl.fromBGColor(Color.Black, imageSize)
    val storage2 = ImageStorageImpl.fromBGColor(Color.Black, imageSize)

    grid.imageSize _ when() returns imageSize
    grid.apply _ when thisImage returns Some(image)
    grid.apply _ when * returns Some(image2)

    image.storage _ when() returns storage
    image2.storage _ when() returns storage2

    storage(dotLocation) = Color.White

    effect.action(Seq(thisImage, borderingImage), grid)

    val colorLookup = new ImageGridColorLookup(grid)
    val dotGlobal = PixelCoords(thisImage, dotLocation).toGlobal(imageSize)

    for (dx <- 0 to radius) {
      val look1 = GlobalPixCoords(dotGlobal.x - dx, dotGlobal.y)
      val look2 = GlobalPixCoords(dotGlobal.x + dx, dotGlobal.y)
      val col1 = ExtendedColor.colorToExtendedColor(colorLookup.lookup(look1).get)
      val col2 = ExtendedColor.colorToExtendedColor(colorLookup.lookup(look2).get)
      try {
        col1 shouldBe col2
      } catch {
        case e: Exception =>
          println(s"dx = $dx")
          throw e
      }
    }
  }

  it should "be additive for multiple sources"
}
