package com.martomate.tripaint.model.grid

import com.martomate.tripaint.model.coords.TriImageCoords
import com.martomate.tripaint.model.image.content.ImageContent
import com.martomate.tripaint.model.image.storage.ImageStorage
import munit.FunSuite
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify}
import org.scalatestplus.mockito.MockitoSugar
import scalafx.scene.paint.Color

class ImageGridTest extends FunSuite with MockitoSugar {
  def make(): ImageGrid = new ImageGrid(16)

  def makeImage(x: Int, y: Int): ImageContent = {
    val storage = ImageStorage.fromBGColor(Color.Black, 4)
    new ImageContent(tc(x, y), storage)
  }

  private def tc(x: Int, y: Int): TriImageCoords = TriImageCoords(x, y)

  test("setImageSizeIfEmpty should set the image size and return true if the grid is new") {
    val f = make()
    val initSize = f.imageSize
    assertEquals(f.setImageSizeIfEmpty(initSize + 16), true)
    assertEquals(f.imageSize, initSize + 16)
  }

  test(
    "setImageSizeIfEmpty should not set the image size and return false if the grid contains images"
  ) {
    val f = make()
    val initSize = f.imageSize

    f.set(makeImage(0, 0))

    assertEquals(f.setImageSizeIfEmpty(initSize + 16), false)
    assertEquals(f.imageSize, initSize)
  }

  test(
    "setImageSizeIfEmpty should set the image size and return true if the grid no longer contains images"
  ) {
    val f = make()
    val initSize = f.imageSize

    f.set(makeImage(0, 0))
    f -= tc(0, 0)

    assertEquals(f.setImageSizeIfEmpty(initSize + 16), true)
    assertEquals(f.imageSize, initSize + 16)
  }

  test("apply should return None if there is no image there") {
    val f = make()
    val image = makeImage(1, 0)

    assertEquals(f(tc(0, 0)), None)
    f.set(image)
    assertEquals(f(tc(0, 0)), None)
  }

  test("apply should return the image at the given location") {
    val f = make()
    val image = makeImage(1, 0)

    assertEquals(f(tc(1, 0)), None)
    f.set(image)
    assertEquals(f(tc(1, 0)), Some(image))
    assertEquals(f(tc(0, 1)), None)
    assertEquals(f(tc(1, 0)), Some(image))
  }

  test("update should add the image if it doesn't already exist") {
    val f = make()
    val image = makeImage(1, 0)

    f.set(image)
    assertEquals(f(tc(1, 0)), Some(image))
  }

  test("update should replace the image if there is already one at that location") {
    val f = make()
    val image = makeImage(1, 0)
    val image2 = makeImage(1, 0)

    f.set(image)
    f.set(image2)
    assertEquals(f(tc(1, 0)), Some(image2))
  }

  test("update should notify listeners about image addition, and image removal if there was one") {
    val f = make()
    val image = makeImage(1, 0)
    val image2 = makeImage(1, 0)

    val listener = mock[ImageGridListener]
    f.addListener(listener)

    f.set(image)
    verify(listener).onAddImage(image)

    f.set(image2)
    verify(listener).onAddImage(image2)
    verify(listener).onRemoveImage(image)
  }

  test("-= should return null if there is no image there") {
    val f = make()
    assertEquals((f -= tc(1, 2)), null)
  }

  test("-= should remove the image and return it if it exists") {
    val f = make()
    val image = makeImage(1, 0)

    f.set(image)
    assertEquals((f -= tc(1, 0)), image)
    assertEquals(f(tc(1, 0)), None)
  }

  test("-= should notify listeners if there was a removal") {
    val f = make()
    val image = makeImage(1, 0)

    f.set(image)

    val listener = mock[ImageGridListener]
    f.addListener(listener)

    f -= tc(0, 0)
    verify(listener, never()).onRemoveImage(any())

    f -= tc(1, 0)
    verify(listener).onRemoveImage(image)
  }

  test("selectedImages should return all images that are currently selected") {
    val f = make()
    val image = makeImage(1, 0)
    val image2 = makeImage(2, 0)

    f.set(image)
    f.set(image2)

    assertEquals(f.selectedImages.sortBy(_.##), Seq(image, image2).sortBy(_.##))
    image.editableProperty() = false
    assertEquals(f.selectedImages.sortBy(_.##), Seq(image2).sortBy(_.##))
    image.editableProperty() = true
    image2.editableProperty() = false
    assertEquals(f.selectedImages.sortBy(_.##), Seq(image).sortBy(_.##))
    image2.editableProperty() = true
    assertEquals(f.selectedImages.sortBy(_.##), Seq(image, image2).sortBy(_.##))
  }
}
